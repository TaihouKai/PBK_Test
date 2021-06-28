package com.example.pbk_test;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import it.unisa.dia.gas.jpbc.Element;

public class DatabaseActivity extends AppCompatActivity {

    public final String ATTR_EXAMPLE = "altitude-latitude-longitude;mm-dd-hh-mm-ss;range-unit";

    public User user;
    public Doctor doctor;
    public long timeTotal;

    /**
     * Required by Android Studio.
     * @param savedInstanceState Required by Android Studio
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        try {
            user = new User(getApplicationContext());
            doctor = new Doctor(getApplicationContext());
        } catch (IOException e) {}
        timeTotal = 0;

        initializeTable();
        initializeTableCompressed();
    }

    /**
     * Generate key pair.
     * This should NOT run in parallel since keypair is required in every single operation.
     * @param view          Required by Android Studio
     */
    public void generateKey(View view) {
        long startTime = System.currentTimeMillis();
        user.keyGen();
        long timeTakenNum = System.currentTimeMillis() - startTime;
        String timeTaken = "Time taken - keyGen: " + timeTakenNum + "ms";

        // timeTotal = 0;
        timeTotal += timeTakenNum;
        display(timeTaken);
        displayTotal(String.valueOf(timeTotal));
    }

    /**
     * (With concurrency) Meet a person -- generate user's assertion.
     * @param view Required by Android Studio
     */
    public void meetGenConCur(View view) {
        ApplicationExecutors exec = new ApplicationExecutors();
        exec.getBackground().execute(
                () -> {
                    long startTime = System.currentTimeMillis();
                    Assertion assertion = user.generateAssertion(ATTR_EXAMPLE, true, true);
                    long timeTakenNum = System.currentTimeMillis() - startTime;
                    String timeTaken = "Time taken - meetGen: " + timeTakenNum + "ms";

                    appendTable(findViewById(R.id.tableLayout), assertion.msg);
                    timeTotal += timeTakenNum;
                    display(timeTaken);
                    displayTotal(String.valueOf(timeTotal));
                }
        );
        exec.getBackground().shutdown();
    }

    public void meetGenConcurRepeat(View view) throws InterruptedException {
        ApplicationExecutors exec = new ApplicationExecutors();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i<Integer.parseInt(((EditText)findViewById(R.id.fieldTimes)).getText().toString()); i++) {
            exec.getBackground().execute(
                    () -> user.generateAssertion(ATTR_EXAMPLE, true, true)
            );
        }
        exec.getBackground().shutdown();
        exec.getBackground().awaitTermination(1, TimeUnit.HOURS);

        long timeTakenNum = System.currentTimeMillis() - startTime;
        String timeTaken = "Time taken - meetGenRepeat: " + timeTakenNum + "ms";
        timeTotal = timeTakenNum;
        display(timeTaken);
        displayTotal(String.valueOf(timeTotal));
    }

    /**
     * (With concurrency) Meet a person -- verify assertion received. Save to database if yes.
     * @param view          Required by Android Studio
     */
    public void meetVerConcur(View view) {
        ApplicationExecutors exec = new ApplicationExecutors();
        exec.getBackground().execute(
                () -> {
                    try {
                        // Create a new user and generate assertion
                        User user_alter = new User(getApplicationContext());
                        user_alter.keyGen();
                        Assertion assertion_alter = user_alter.generateAssertion(ATTR_EXAMPLE, false, true);

                        long startTime = System.currentTimeMillis();
                        boolean res = user.verifyAssertion(assertion_alter, getApplicationContext());
                        if (res)
                            user.db.assertionDao().insert(assertion_alter);
                        long timeTakenNum = System.currentTimeMillis() - startTime;
                        String timeTaken = "Time taken - meetVer: " + timeTakenNum + "ms";

                        timeTotal += timeTakenNum;
                        display(timeTaken);
                        displayTotal(String.valueOf(timeTotal));
                        if (res) {
                            Log.d("PBK_Test - VerifyAssertion", "Success");
                            appendTable(findViewById(R.id.tableLayout), assertion_alter.msg);
                        }
                        else
                            Log.d("PBK_Test - VerifyAssertion", "Fail");
                    } catch (Exception ignored) {}
                }
        );
        exec.getBackground().shutdown();
    }

    public void meetVerConcurRepeat(View view) throws InterruptedException {
        ApplicationExecutors exec = new ApplicationExecutors();
        AtomicLong totalTimeTaken = new AtomicLong();
        for (int i = 0; i<Integer.parseInt(((EditText)findViewById(R.id.fieldTimes)).getText().toString()); i++) {
            exec.getBackground().execute(
                    () -> {
                        try {
                            // Create a new user and generate assertion
                            User user_alter = new User(getApplicationContext());
                            user_alter.keyGen();
                            Assertion assertion_alter = user_alter.generateAssertion(ATTR_EXAMPLE, false, true);

                            long startTime = System.currentTimeMillis();
                            boolean res = user.verifyAssertion(assertion_alter, getApplicationContext());
                            if (res)
                                user.db.assertionDao().insert(assertion_alter);
                            long timeTakenNum = System.currentTimeMillis() - startTime;
                            totalTimeTaken.addAndGet(timeTakenNum);

                            if (res) {
                                Log.d("PBK_Test - VerifyAssertion", "Success");
                                appendTable(findViewById(R.id.tableLayout), assertion_alter.msg);
                            } else
                                Log.d("PBK_Test - VerifyAssertion", "Fail");
                        } catch (Exception ignored) {}
                    }
            );
        }

        exec.getBackground().shutdown();
        exec.getBackground().awaitTermination(1, TimeUnit.HOURS);

        String timeTaken = "Time taken - meetVerRepeat: " + totalTimeTaken + "ms";
        timeTotal = totalTimeTaken.longValue();
        display(timeTaken);
        displayTotal(String.valueOf(timeTotal));
    }

    public void saveConcur(View view) {
        ApplicationExecutors exec = new ApplicationExecutors();
        exec.getBackground().execute(
                () -> {
                    long startTime = System.currentTimeMillis();
                    user.save();
                    long timeTakenNum = System.currentTimeMillis() - startTime;
                    String timeTaken = "Time taken - save: " + timeTakenNum + "ms";

                    clearTable(findViewById(R.id.tableLayoutCompressed));
                    initializeTableCompressed();

                    timeTotal = timeTakenNum;
                    display(timeTaken);
                    displayTotal(String.valueOf(timeTotal));
                }
        );
    }

    public void saveConcurRepeat(View view) throws InterruptedException {
        ApplicationExecutors exec = new ApplicationExecutors();
        AtomicLong totalTimeTaken = new AtomicLong();
        for (int i = 0; i<Integer.parseInt(((EditText)findViewById(R.id.fieldTimes)).getText().toString()); i++) {
            exec.getBackground().execute(
                    () -> {
                        // Reset db
                        deleteAllConcur(view);

                        // Generate assertions based on fieldCount
                        for (int j = 0; j<Integer.parseInt(((EditText)findViewById(R.id.fieldCount)).getText().toString()); j++) {
                            user.generateAssertion(ATTR_EXAMPLE, true, false);
                        }
                        // Save
                        long startTime = System.currentTimeMillis();
                        user.save();
                        long timeTakenNum = System.currentTimeMillis() - startTime;
                        totalTimeTaken.addAndGet(timeTakenNum);
                    }
            );
        }

        exec.getBackground().shutdown();
        exec.getBackground().awaitTermination(1, TimeUnit.HOURS);

        String timeTaken = "Time taken - saveRepeat: " + totalTimeTaken + "ms";
        timeTotal = totalTimeTaken.longValue();
        display(timeTaken);
        displayTotal(String.valueOf(timeTotal));
    }

    public void showConcur(View view) {
        ApplicationExecutors exec = new ApplicationExecutors();
        exec.getBackground().execute(
                () -> {
                    long startTime = System.currentTimeMillis();

                    // Get all CompressedAssertion's
                    List<CompressedAssertion> compressedList = user.db.compressedAssertionDao().getAll();
                    // Record list to be sent to doctor
                    List<Record> recordList = new ArrayList<>();
                    // For every CompressedAssertion...
                    for (CompressedAssertion ca: compressedList) {
                        // Get all Assertions linked with this CompressedAssertion
                        List<Assertion> assertionList = user.db.assertionDao().findAllByIDs(ca.ids);
                        // Record associated with this CompressedAssertion
                        Element rand = user.pkrbls.sampleEleZr(user.parameters);
                        Record r = new Record(ca.signature, user.pkrbls, user.parameters, rand);
                        // For every Assertion in the list
                        for (Assertion assertion: assertionList) {
                            try {
                                // Update PK and add to record
                                r.nyms.add(user.pkrbls.updatePK(MainActivity.getCipherFromBytes(assertion.nym, assertion.g, getApplicationContext()), user.parameters, rand));
                                // Add msg to record
                                r.msgs.add(assertion.msg);
                                // Add g^r to record
                                r.gPowRs.add(assertion.gPowR);
                                // Add isBLE to record
                                r.isBLEs.add(assertion.isBLE);
                            } catch (IOException e) {}
                        }
                        // Add this record to record list
                        recordList.add(r);
                    }
                    // Send recordList to doctor ...
                    if (this.doctor.verifyShow(recordList))
                        Log.d("PBK_Test - VerifyShow", "Success");
                    else
                        Log.d("PBK_Test - VerifyShow", "Fail");

                    long timeTakenNum = System.currentTimeMillis() - startTime;
                    String timeTaken = "Time taken - show: " + timeTakenNum + "ms";

                    timeTotal = timeTakenNum;
                    display(timeTaken);
                    displayTotal(String.valueOf(timeTotal));
                }
        );
    }

    public void updateConcur(View view) {
        ApplicationExecutors exec = new ApplicationExecutors();
        exec.getBackground().execute(
                () -> {
                    long startTime = System.currentTimeMillis();

                    this.user.updateNym();

                    long timeTakenNum = System.currentTimeMillis() - startTime;
                    String timeTaken = "Time taken - updateNym: " + timeTakenNum + "ms";

                    timeTotal += timeTakenNum;
                    display(timeTaken);
                    displayTotal(String.valueOf(timeTotal));
                }
        );
    }

    public void discreteMode(View view) {
        ApplicationExecutors exec = new ApplicationExecutors();
        try {
            long startTime = System.currentTimeMillis();
            for (int i = 0; i<Integer.parseInt(((EditText)findViewById(R.id.fieldTimes)).getText().toString()); i++) {
                exec.getBackground().execute(
                        () -> user.generateAssertion(ATTR_EXAMPLE, true, false)
                );
            }
            exec.getBackground().shutdown();
            exec.getBackground().awaitTermination(1, TimeUnit.HOURS);

            long timeTakenNum = System.currentTimeMillis() - startTime;
            String timeTaken = "Time taken - discreteMode: " + timeTakenNum + "ms";

            timeTotal = timeTakenNum;
            display(timeTaken);
            displayTotal(String.valueOf(timeTotal));
        } catch (Exception e) {
            // When fieldTimes is not properly filled
        }
    }

    public void bleMode(View view) {
        ApplicationExecutors exec = new ApplicationExecutors();
        try {
            long startTime = System.currentTimeMillis();
            User user_alter = new User(getApplicationContext());
            user_alter.keyGen();
            Assertion assertion_alter = user_alter.generateAssertion(ATTR_EXAMPLE, false, true);
            for (int i = 0; i<Integer.parseInt(((EditText)findViewById(R.id.fieldTimes)).getText().toString()); i++) {
                exec.getBackground().execute(
                        () -> {
                            try {
                                user.generateAssertion(ATTR_EXAMPLE, true, true);
                                boolean res = user.verifyAssertion(assertion_alter, getApplicationContext());
                                if (res)
                                    user.db.assertionDao().insert(assertion_alter);
                            } catch (Exception e) {}
                        }
                );
            }
            exec.getBackground().shutdown();
            exec.getBackground().awaitTermination(1, TimeUnit.HOURS);

            long timeTakenNum = System.currentTimeMillis() - startTime;
            String timeTaken = "Time taken - bleMode: " + timeTakenNum + "ms";

            timeTotal = timeTakenNum;
            display(timeTaken);
            displayTotal(String.valueOf(timeTotal));
        } catch (Exception e) {
            // When fieldTimes is not properly filled
            // ... or interrupted
        }
    }

    /**
     * (Without concurrency) Delete all data in the database.
     * You should NEVER use this method unless you modify the database to be manipulate-able in UI.
     * @param view Required by Android Studio
     */
    @Deprecated
    public void deleteAll(View view) {
        user.db.assertionDao().delete();
        clearTable(findViewById(R.id.tableLayout));
        timeTotal = 0;
    }

    /**
     * (With concurrency) Delete all data in the database.
     * @param view Required by Android Studio
     */
    public void deleteAllConcur(View view) {
        ApplicationExecutors exec = new ApplicationExecutors();
        exec.getBackground().execute(
                () -> {
                    user.db.assertionDao().delete();
                    user.db.compressedAssertionDao().delete();
                    clearTable(findViewById(R.id.tableLayout));
                    clearTable(findViewById(R.id.tableLayoutCompressed));
                    timeTotal = 0;
                }
        );
        exec.getBackground().shutdown();
    }

    /**
     * Initialize and display the table.
     */
    public void initializeTable() {
        ApplicationExecutors exec = new ApplicationExecutors();
        exec.getBackground().execute(
                () -> {
                    TableLayout tl = findViewById(R.id.tableLayout);
                    List<Assertion> list = user.db.assertionDao().getAll();
                    for (int i = 0; i < list.size(); i++) {
                        appendTable(tl, list.get(i).msg);
                    }
                }
        );
        exec.getBackground().shutdown();
    }

    public void initializeTableCompressed() {
        ApplicationExecutors exec = new ApplicationExecutors();
        exec.getBackground().execute(
                () -> {
                    TableLayout tl = findViewById(R.id.tableLayoutCompressed);
                    List<CompressedAssertion> list = user.db.compressedAssertionDao().getAll();
                    for (int i = 0; i < list.size(); i++) {
                        appendTable(tl, list.get(i).ids.size() + "|" + list.get(i).ids.toString());
                    }
                }
        );
        exec.getBackground().shutdown();
    }

    /**
     * Append a row to the table.
     * @param tl    Table to be appended to
     * @param msg   Info in this row
     */
    public void appendTable(TableLayout tl, String msg) {
        TableRow newRow = new TableRow(this);
        newRow.setBackgroundColor(Color.GRAY);
        newRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TextView label_id = new TextView(this);
        label_id.setText(msg);
        label_id.setTextColor(Color.WHITE);
        label_id.setPadding(5, 5, 5, 5);
        newRow.addView(label_id);

        runOnUiThread(() -> tl.addView(newRow));
    }

    /**
     * Clear the table.
     * @param tl Table to be cleared
     */
    public void clearTable(TableLayout tl) {
        runOnUiThread(tl::removeAllViews);
    }

    /**
     * Display string in textView "resultDB"
     * @param str String to be displayed
     */
    public void display(String str) {
        runOnUiThread(() -> {
            TextView res = findViewById(R.id.resultDB);
            res.setText(str);
        });
    }

    /**
     * Display string in textView "resultTotal"
     * @param str String to be displayed
     */
    public void displayTotal(String str) {
        runOnUiThread(() -> {
            TextView res = findViewById(R.id.resultTotal);
            res.setText("Total time taken: " + str + "ms");
        });
    }
}