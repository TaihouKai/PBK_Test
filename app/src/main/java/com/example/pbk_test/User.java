package com.example.pbk_test;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01KeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01Parameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class User {

    public final PKRBLS pkrbls;
    public AsymmetricCipherKeyPair keyPair;
    public CipherParameters nym;
    public Database db;

    protected Element r;
    protected BLS01Parameters parameters;

    /**
     * Construct a new user.
     * A normal user.
     * Note: Normally, user should be CONSISTENT in a real application, since you don't want to
     * lose your data after closing the application. However, in our demo, the user (and associated
     * database) is newed every single time you enter the activity (UI).
     * This is simply because we are here only to prove its performance, instead of developing a
     * REAL application.
     * @param context Current application context
     * @throws IOException Error when a.properties is not found
     */
    public User(Context context) throws IOException {
        this.pkrbls = new PKRBLS(context);
        this.parameters = this.pkrbls.setup();
        db = Room.databaseBuilder(context, Database.class, "database-main").build();
    }

    /**
     * Generate initialKey pair.
     * @throws IOException Error when a.properties is not found
     */
    public void keyGen() {
        this.keyPair = pkrbls.keyGen(this.parameters);
        this.nym = this.keyPair.getPublic();
        this.r = pkrbls.setEleZr(1, this.parameters);
    }

    /**
     * Generate an Assertion and insert to database.
     * @param attr      Attributes
     * @param insert    Whether to insert this assertion into db
     * @return          Assertion
     */
    public Assertion generateAssertion(String attr, boolean insert) {
        String msg = User.generateMsg(attr);
        Assertion assertion = new Assertion(this.nym, msg, this.pkrbls.sign(msg, this.keyPair.getPrivate(), this.r), this.parameters.getG().powZn(this.r).toBytes());
        if (insert)
            db.assertionDao().insert(assertion);
        return assertion;
    }

    /**
     * Verify an assertion using public key contained.
     * @param assertion Assertion containing signature to be verified
     * @return          Verification result
     */
    public boolean verifyAssertion(Assertion assertion, Context context) throws IOException {
        return this.pkrbls.verify(assertion.signature, assertion.msg, MainActivity.getCipherFromBytes(assertion.nym, assertion.g, context));
    }

    /**
     * Compress all signatures
     */
    public void save() {
        List<Assertion> assertions = this.db.assertionDao().getAll();
        List<byte[]> signatures = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        for (Assertion assertion: assertions) {
            if (!assertion.isSaved) {
                signatures.add(assertion.signature);
                ids.add(assertion.id);
                this.db.assertionDao().updateIsSaved(true, assertion.id);
                Log.d("PBK_Test - save", String.valueOf(assertion.id));
            }
        }
        byte[] compressedSig = this.pkrbls.aggregate(signatures, this.parameters);
        db.compressedAssertionDao().insert(new CompressedAssertion(compressedSig, ids));
    }

    /**
     * Update pseudonym.
     * WIP
     */
    public void updateNym() {
        this.r = this.r.mul(this.pkrbls.sampleEleZr(this.parameters));
        this.nym = this.pkrbls.updatePK(this.nym, this.parameters, r);
    }

    /**
     * Generate message by adding randomness to attributes
     * @param attr  Attributes
     * @return      Message
     */
    public static String generateMsg(String attr) {
        // return attr + "|" + ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
        return attr + "|" + new SecureRandom().nextInt(Integer.MAX_VALUE);
    }
}
