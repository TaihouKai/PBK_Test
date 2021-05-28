package com.example.pbk_test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class DatabaseActivity extends AppCompatActivity {

    public final String ATTR_EXAMPLE = "altitude-latitude-longitude;mm-dd-hh-mm-ss;range-unit";

    public User user;
    public Database db;

    public long timeTotal;

    /**
     * Required by Android Studio.
     * @param savedInstanceState Required by Android Studio
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        db = Room.databaseBuilder(getApplicationContext(), Database.class, "database-name").allowMainThreadQueries().build();
        user = new User(getApplicationContext());
        timeTotal = 0;

        initializeTable();
    }

    /**
     * Generate key pair.
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
     * Meet a person -- generate user's assertion.
     * @param view Required by Android Studio
     */
    public void meetGen(View view) {
        long startTime = System.currentTimeMillis();
        Assertion assertion = user.generateAssertion(ATTR_EXAMPLE, db);
        appendTable((TableLayout) findViewById(R.id.tableLayout), assertion.msg);
        long timeTakenNum = System.currentTimeMillis() - startTime;
        String timeTaken = "Time taken - meetGen: " + timeTakenNum + "ms";

        timeTotal += timeTakenNum;
        display(timeTaken);
        displayTotal(String.valueOf(timeTotal));
    }

    /**
     * Meet a person -- verify assertion received.
     * @param view          Required by Android Studio
     * @throws IOException  Error when a.properties is not found
     */
    public void meetVer(View view) throws IOException {
        // Create a new user and generate assertion
        User user_alter = new User(getApplicationContext());
        user_alter.keyGen();
        Assertion assertion_alter = user_alter.generateAssertion(ATTR_EXAMPLE);

        long startTime = System.currentTimeMillis();
        user.verifyAssertion(assertion_alter, getApplicationContext());
        long timeTakenNum = System.currentTimeMillis() - startTime;
        String timeTaken = "Time taken - meetVer: " + timeTakenNum + "ms";

        timeTotal += timeTakenNum;
        display(timeTaken);
        displayTotal(String.valueOf(timeTotal));
    }

    /**
     * Delete all data in the database.
     * @param view Required by Android Studio
     */
    public void deleteAll(View view) {
        db.assertionDao().delete();
    }

    /**
     * Initialize and display the table.
     */
    public void initializeTable() {
        TableLayout tl = (TableLayout) findViewById(R.id.tableLayout);
        List<Assertion> list = db.assertionDao().getAll();
        for (int i=0; i<list.size();i++) {
            appendTable(tl, list.get(i).msg);
        }
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

        tl.addView(newRow);
    }

    /**
     * Display string in textView "resultDB"
     * @param str String to be displayed
     */
    public void display(String str) {
        TextView res = findViewById(R.id.resultDB);
        res.setText(str);
    }

    /**
     * Display string in textView "resultTotal"
     * @param str String to be displayed
     */
    public void displayTotal(String str) {
        TextView res = findViewById(R.id.resultTotal);
        res.setText("Total time taken: " + str + "ms");
    }
}