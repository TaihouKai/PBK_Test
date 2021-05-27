package com.example.pbk_test;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.bouncycastle.crypto.CipherParameters;

import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01KeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01PublicKeyParameters;

@Entity
public class Assertion {

    @PrimaryKey
    @NonNull
    public byte[] nym;

    @ColumnInfo(name = "message")
    public String msg;

    @ColumnInfo(name = "signature")
    public byte[] signature;

    @ColumnInfo(name = "gValue")
    public byte[] g;

    public Assertion(CipherParameters cipherNym, String msg, byte[] signature) {
        this.nym = MainActivity.getBytesFromCipher(cipherNym);
        this.msg = msg;
        this.signature = signature;
        this.g = ((BLS01KeyParameters)cipherNym).getParameters().getG().toBytes();
    }

    public Assertion() {}
}
