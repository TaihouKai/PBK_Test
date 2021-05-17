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

    /**
     * Run the algorithm.
     * @param   view            Required by Android Studio.
     * @throws  IOException     Error occurred when a.properties is not found.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void initiate(View view) throws IOException {
        TextView res = findViewById(R.id.result);

        long startTime = System.currentTimeMillis();

        BLS01 bls01 = new BLS01(this);
        AsymmetricCipherKeyPair keyPair = bls01.keyGen(bls01.setup()); // Setup
        String message = "Hello, world!";
        bls01.verify(bls01.sign(message, keyPair.getPrivate()), message, keyPair.getPublic());

        res.setText("Time taken: " + (System.currentTimeMillis() -startTime) + "ms");
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            getCacheFile("a.properties", getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
