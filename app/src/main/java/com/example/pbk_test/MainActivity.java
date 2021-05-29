package com.example.pbk_test;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01KeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01Parameters;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01PublicKeyParameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;


public class MainActivity extends AppCompatActivity {

    private static boolean isFirstTime = true;

    /**
     * Enter database button event.
     * @param view Required by Android Studio
     */
    public void showDatabase(View view) {
        Intent intent = new Intent(this, DatabaseActivity.class);
        startActivity(intent);
    }

    /**
     * Run the test algorithm.
     * @param   view            Required by Android Studio
     * @throws  IOException     Error when a.properties is not found
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void initiateTest(View view) throws IOException {
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
            display(timeTaken1 + "\n" + timeTaken2);
        }
    }

    /**
     * Clear the text shown.
     * @param view Required by Android Studio
     */
    public void clear(View view) {
        display(getString(R.string.display_name));
    }

    /**
     * Required by Android Studio.
     * @param savedInstanceState Required by Android Studio
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Pre-do KeyGen to make it faster
        try {
            initiateTest(getWindow().getDecorView());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get path of the given file in assets.
     * @param   path        Path to file
     * @param   context     Context of the activity
     * @return              File object containing file path
     * @throws  IOException Error when file is not found
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

    /**
     * Convert byte array to CipherParameter. Only public key is supported for now, since this function requires g.
     * @param bytes     Byte array input
     * @param g         g, for public key conversion
     * @param context   Current application context
     * @return          CipherParameter
     * @throws IOException Error when a.properties is not found
     */
    public static CipherParameters getCipherFromBytes(byte[] bytes, byte[] g, Context context) throws IOException {
        Pairing pairing = PairingFactory.getPairing(MainActivity.getCacheFile("a.properties", context).toPath().toString());
        Field f = pairing.getG2();
        Element e = f.newElement();
        e.setFromBytes(bytes);

        BLS01Parameters param = new BLS01Parameters(PairingFactory.getPairingParameters(MainActivity.getCacheFile("a.properties", context).toPath().toString()), MainActivity.getElementFromBytes(g, 2, context));
        BLS01PublicKeyParameters pkParam = new BLS01PublicKeyParameters(param, e);
        return pkParam;
    }

    /**
     * Convert byte array to Element.
     * @param bytes         Byte array input
     * @param type          1 -> G1 (sig); 2 -> G2 (pk/g)
     * @param context       Current application context
     * @return              Element
     * @throws IOException  Error when a.properties is not found
     */
    public static Element getElementFromBytes(byte[] bytes, int type, Context context) throws IOException {
        Pairing pairing = PairingFactory.getPairing(MainActivity.getCacheFile("a.properties", context).toPath().toString());
        Field f = pairing.getG2();
        Element e = f.newElement();
        e.setFromBytes(bytes);
        return e;
    }

    /**
     * Convert CipherParameter to byte array.
     * @param cp    CipherParameter input (i.e. public key)
     * @return      Byte array
     */
    public static byte[] getBytesFromCipher(CipherParameters cp) {
        return ((BLS01PublicKeyParameters)cp).getPk().toBytes();
    }

    /**
     * Display string in textView "result"
     * @param str String to be displayed
     */
    public void display(String str) {
        TextView res = findViewById(R.id.result);
        res.setText(str);
    }
}