package com.example.pbk_test;

import android.content.Context;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class User {

    public CipherParameters nym;
    public BLS01 bls01;
    public AsymmetricCipherKeyPair keyPair;

    /**
     * Construct a new user.
     * @param context Current application context
     */
    public User(Context context) {
        this.bls01 = new BLS01(context);
    }

    /**
     * Generate Key pair.
     * @throws IOException Error when a.properties is not found
     */
    public void keyGen() throws IOException {
        this.keyPair = bls01.keyGen(bls01.setup());
        this.nym = this.keyPair.getPublic();
    }

    /**
     * Generate an Assertion and insert to database.
     * @param attr  Attributes
     * @return      Assertion
     */
    public Assertion generateAssertion(String attr, Database db) {
        Assertion assertion = new Assertion(this.nym, User.generateMsg(attr), this.bls01.sign(attr, this.keyPair.getPrivate()));
        db.assertionDao().insert(assertion);
        return assertion;
    }

    /**
     * Generate an assertion WITHOUT inserting to database.
     * @param attr  Attributes
     * @return      Assertion
     */
    public Assertion generateAssertion(String attr) {
        return new Assertion(this.nym, User.generateMsg(attr), this.bls01.sign(attr, this.keyPair.getPrivate()));
    }

    /**
     * Verify an assertion using public key contained.
     * @param assertion Assertion containing signature to be verified
     * @return          Verification result
     */
    public boolean verifyAssertion(Assertion assertion, Context context) throws IOException {
        return this.bls01.verify(assertion.signature, assertion.msg, MainActivity.getCipherFromBytes(assertion.nym, assertion.g, context));
    }

    /**
     * WIP
     * @param signatures    Signatures received
     * @return              Compressed signatures
     */
    public byte[] save(List<byte[]> signatures, Context context) throws IOException {


        return new byte[0];
    }

    /**
     * WIP
     * @param signature
     * @param db
     * @return
     */
    public Assertion show(byte[] signature, Database db) {
        // WIP

        // this.updateAssertions();
        // delete all assertions in DB
        return generateAssertion("", db);
    }

    /**
     * Update pseudonym.
     * WIP
     */
    public void updateNym() {
        // this.nym = this.nym.update();
    }

    /**
     * Update assertions.
     * WIP
     */
    public void updateAssertions() {
        // WIP
        // Update assertions in DB
    }

    /**
     * Generate message by adding randomness to attributes
     * @param attr  Attributes
     * @return      Message
     */
    public static String generateMsg(String attr) {
        return attr + "|" + ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
    }
}
