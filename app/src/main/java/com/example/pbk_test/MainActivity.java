package com.example.pbk_test;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    // public static String propertiesPath = new String(); // deprecated
    private static boolean isFirstTime = true;

    /**
     * Run the algorithm.
     * @param   view            Required by Android Studio.
     * @throws  IOException     Error occurred when a.properties is not found.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void initiate(View view) throws IOException {
        TextView res = findViewById(R.id.result);

        // First KeyGen takes more time
        long startTime1 = System.currentTimeMillis();
        BLS01 bls01 = new BLS01(this);
        AsymmetricCipherKeyPair keyPair = bls01.keyGen(bls01.setup()); // Setup
        String timeTaken1 = "Time taken - KeyGen: " + (System.currentTimeMillis() -startTime1) + "ms";

        if (isFirstTime)
            isFirstTime = false;
        else {
            long startTime2 = System.currentTimeMillis();
            String message = "Hello, world!";
            bls01.verify(bls01.sign(message, keyPair.getPrivate()), message, keyPair.getPublic());
            String timeTaken2 = "Time taken - Verify: " + (System.currentTimeMillis() - startTime2) + "ms";
            res.setText(timeTaken1 + "\n" + timeTaken2);
        }
    }

    /**
     * Clear the text shown.
     * @param view Required by Android Studio.
     */
    public void clear(View view) {
        TextView res = findViewById(R.id.result);
        res.setText(getString(R.string.display_name));
    }

    /**
     * Required by Android Studio.
     * @param savedInstanceState Required by Android Studio.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Pre-do KeyGen to make it faster
        try {
            initiate(getWindow().getDecorView());
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* deprecated
        long startTime = System.currentTimeMillis();
        try {
            propertiesPath = getCacheFile("a.properties", getApplicationContext()).toPath().toString();
            Log.i("PBK_Test", propertiesPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("PBK_Test", String.valueOf(System.currentTimeMillis() - startTime));
         */
    }

    /**
     * Get path of the given file in assets.
     * @param   path        Path to file.
     * @param   context     Context of the activity.
     * @return              File object containing file path.
     * @throws  IOException Error occurred when file is not found.
     */
    public static File getCacheFile(String path, Context context) throws IOException {
        File cacheFile = new File(context.getCacheDir(), path);
        try {
            InputStream inputStream = context.getAssets().open(path);
            try {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new IOException("Could not open file", e);
        }
        return cacheFile;
    }
}
