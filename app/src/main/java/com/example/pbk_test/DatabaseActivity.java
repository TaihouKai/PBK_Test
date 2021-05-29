package com.example.pbk_test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.Application;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class DatabaseActivity extends AppCompatActivity {

    public final String ATTR_EXAMPLE = "altitude-latitude-longitude;mm-dd-hh-mm-ss;range-unit";

    public User user;
    public long timeTotal;

    /**
     * Required by Android Studio.
     * @param savedInstanceState Required by Android Studio
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        user = new User(getApplicationContext());
        timeTotal = 0;

        initializeTable();
    }

    /**
     * Generate key pair.
     * This should NOT run in parallel since keypair is required in every single operation.
     * @param view          Required by Android Studio
     * @throws IOException  Error when a.properties is not found
     */
    public void generateKey(View view) throws IOException {
        long startTime = System.currentTimeMillis();
        user.keyGen();
        long timeTakenNum = System.currentTimeMillis() - startTime;
        String timeTaken = "Time taken - keyGen: " + timeTakenNum + "ms";

        timeTotal += timeTakenNum;
        display(timeTaken);
        displayTotal(String.valueOf(timeTotal));
    }

    /**
     * (Without concurrency) Meet a person -- generate user's assertion.
     * You should NEVER use this method unless you modify the database to be manipulate-able in UI.
     * @param view Required by Android Studio
     */
    @Deprecated
    public void meetGen(View view) {
        long startTime = System.currentTimeMillis();
        Assertion assertion = user.generateAssertion(ATTR_EXAMPLE, true);
        appendTable(findViewById(R.id.tableLayout), assertion.msg);
        long timeTakenNum = System.currentTimeMillis() - startTime;
        String timeTaken = "Time taken - meetGen: " + timeTakenNum + "ms";

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
                    Assertion assertion = user.generateAssertion(ATTR_EXAMPLE, true);
                    appendTable(findViewById(R.id.tableLayout), assertion.msg);
                    long timeTakenNum = System.currentTimeMillis() - startTime;
                    String timeTaken = "Time taken - meetGen: " + timeTakenNum + "ms";

                    timeTotal += timeTakenNum;
                    display(timeTaken);
                    displayTotal(String.valueOf(timeTotal));
                }
        );
    }

    /**
     * (Without concurrency) Meet a person -- verify assertion received.
     * @param view          Required by Android Studio
     * @throws IOException  Error when a.properties is not found
     */
    @Deprecated
    public void meetVer(View view) throws IOException {
        // Create a new user and generate assertion
        User user_alter = new User(getApplicationContext());
        user_alter.keyGen();
        Assertion assertion_alter = user_alter.generateAssertion(ATTR_EXAMPLE, false);

        long startTime = System.currentTimeMillis();
        user.verifyAssertion(assertion_alter, getApplicationContext());
        long timeTakenNum = System.currentTimeMillis() - startTime;
        String timeTaken = "Time taken - meetVer: " + timeTakenNum + "ms";

        timeTotal += timeTakenNum;
        display(timeTaken);
        displayTotal(String.valueOf(timeTotal));
    }

    /**
     * (With concurrency) Meet a person -- verify assertion received.
     * @param view          Required by Android Studio
     * @throws IOException  Error when a.properties is not found
     */
    public void meetVerConcur(View view) {
        ApplicationExecutors exec = new ApplicationExecutors();
        exec.getBackground().execute(
                () -> {
                    try {
                        // Create a new user and generate assertion
                        User user_alter = new User(getApplicationContext());
                        user_alter.keyGen();
                        Assertion assertion_alter = user_alter.generateAssertion(ATTR_EXAMPLE, false);

                        long startTime = System.currentTimeMillis();
                        boolean res = user.verifyAssertion(assertion_alter, getApplicationContext());
                        long timeTakenNum = System.currentTimeMillis() - startTime;
                        String timeTaken = "Time taken - meetVer: " + timeTakenNum + "ms";

                        timeTotal += timeTakenNum;
                        display(timeTaken);
                        displayTotal(String.valueOf(timeTotal));

                        if (res)
                            Log.d("PBK_Test - Verification", "Success");
                        else
                            Log.d("PBK_Test - Verification", "Fail");
                    } catch (Exception e) {}
                }
        );
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
                    clearTable(findViewById(R.id.tableLayout));
                    timeTotal = 0;
                }
        );
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
        runOnUiThread(() -> tl.removeAllViews());
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