package com.example.pbk_test;

import android.sax.Element;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.bouncycastle.crypto.CipherParameters;

import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01KeyParameters;

@Entity(tableName = "assertions")
public class Assertion {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "assertionId")
    public int id;

    @ColumnInfo(name = "pseudonym")
    public byte[] nym;

    @ColumnInfo(name = "message")
    public String msg;

    @ColumnInfo(name = "signature")
    public byte[] signature;

    @ColumnInfo(name = "gValue")
    public byte[] g;

    @ColumnInfo(name = "gPowR")
    public byte[] gPowR;

    @ColumnInfo(name = "isSaved")
    public boolean isSaved;

    public Assertion(CipherParameters cipherNym, String msg, byte[] signature, byte[] gPowR) {
        this.nym = MainActivity.getBytesFromCipher(cipherNym);
        this.msg = msg;
        this.signature = signature;
        this.g = ((BLS01KeyParameters)cipherNym).getParameters().getG().toBytes();
        this.gPowR = gPowR;
        this.isSaved = false;
    }

    public Assertion() {}
}
