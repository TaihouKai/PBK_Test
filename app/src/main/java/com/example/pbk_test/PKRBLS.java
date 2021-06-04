package com.example.pbk_test;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.Digest;

import java.io.IOException;
import java.util.List;

import it.unisa.dia.gas.crypto.jpbc.signature.bls01.generators.BLS01ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01Parameters;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01KeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01PrivateKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01PublicKeyParameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;


public class PKRBLS {

    private final Context context;

    /**
     * Construct a PKR-BLS scheme instance.
     * A PKR-BLS scheme instance.
     * @param context Current application context
     */
    public PKRBLS(Context context) {
        this.context = context;
    }

    /**
     * Generate Type III pairing parameters for PKR-BLS scheme.
     * Same as the original BLS scheme.
     * @throws IOException Error when a.properties is not found.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public BLS01Parameters setup() throws IOException {
        BLS01ParametersGenerator setup = new BLS01ParametersGenerator();
        setup.init(PairingFactory.getPairingParameters(MainActivity.getCacheFile("a.properties", context).toPath().toString()));

        return setup.generateParameters();
    }

    /**
     * Generate public-secret key pair for PKR-BLS scheme.
     * Same as the original BLS scheme.
     * @param parameters Type III pairing parameters.
     */
    public AsymmetricCipherKeyPair keyGen(BLS01Parameters parameters) {
        Pairing pairing = PairingFactory.getPairing(parameters.getParameters());
        Element g = parameters.getG();

        Element sk = pairing.getZr().newRandomElement();
        Element pk = g.powZn(sk);

        return new AsymmetricCipherKeyPair(
                new BLS01PublicKeyParameters(parameters, pk.getImmutable()),
                new BLS01PrivateKeyParameters(parameters, sk.getImmutable())
        );
    }

    /**
     * Sign signatures in PKR-BLS scheme.
     * Same as the original BLS scheme.
     * @param message         String message to be signed.
     * @param privateKeyParam CipherParameters type of Secret key, converted and used for signing message.
     * @param r               randomness used in pk.
     */
    public byte[] sign(String message, CipherParameters privateKeyParam, Element r) {
        // get pairing from private key
        BLS01PrivateKeyParameters privateKey = (BLS01PrivateKeyParameters) ((BLS01KeyParameters) privateKeyParam);
        Pairing pairing = PairingFactory.getPairing(privateKey.getParameters().getParameters());

        // compute hash of the message
        byte[] bytes = message.getBytes();
        Digest digest = new SHA256Digest();
        digest.reset();
        digest.update(bytes, 0, bytes.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        Element h = pairing.getG1().newElementFromHash(hash, 0, hash.length);

        Element sig = h.powZn(privateKey.getSk().mul(r));

        return sig.toBytes();
    }

    /**
     * Verify signatures in PKR-BLS scheme.
     * @param signature      Signature to be verified.
     * @param message        The signed message.
     * @param publicKeyParam CipherParameters type of Public key, converted and used for verifying message-signature pair.
     */
    public boolean verify(byte[] signature, String message, CipherParameters publicKeyParam) {
        // get pairing from public key
        BLS01PublicKeyParameters publicKey = (BLS01PublicKeyParameters) ((BLS01KeyParameters) publicKeyParam);
        Pairing pairing = PairingFactory.getPairing(publicKey.getParameters().getParameters());

        // compute hash of the message
        byte[] bytes = message.getBytes();
        Digest digest = new SHA256Digest();
        digest.reset();
        digest.update(bytes, 0, bytes.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        Element h = pairing.getG1().newElementFromHash(hash, 0, hash.length);

        Element sig = pairing.getG1().newElementFromBytes(signature);

        Element temp1 = pairing.pairing(sig, publicKey.getParameters().getG());
        Element temp2 = pairing.pairing(h, publicKey.getPk());

        return temp1.isEqual(temp2);
    }

    /**
     * Update public key with a random number r.
     * @param publicKeyParam CipherParameters type of Public key, to be updated.
     * @param parameters     public parameter of PKRBLS scheme.
     * @param r              randomness for updating public key.
     */
    public CipherParameters updatePK(CipherParameters publicKeyParam, BLS01Parameters parameters, Element r) {
        // Update the public key
        Element pk = ((BLS01PublicKeyParameters) ((BLS01KeyParameters) publicKeyParam)).getPk();
        Element updated = pk.powZn(r);
        BLS01PublicKeyParameters updatedPK = new BLS01PublicKeyParameters(parameters, updated.getImmutable());

        return updatedPK;
    }

    /**
     * Update signature with a random number r.
     * @param parameters public parameter of PKRBLS scheme.
     * @param r          randomness for updating signature.
     */
    public byte[] updateSIG(byte[] signature, BLS01Parameters parameters, Element r) {
        Pairing pairing = PairingFactory.getPairing(parameters.getParameters());

        // Update the signature
        Element sig = pairing.getG1().newElementFromBytes(signature);
        Element updated = sig.powZn(r);
        byte[] updatedSIG = updated.toBytes();

        return updatedSIG;
    }

    /**
     * Aggregate multiple signatures from PKR-BLS scheme.
     * @param signatures A list of signatures to be aggregated by group multiplication.
     * @param parameters public parameter of PKRBLS scheme.
     */
    public byte[] aggregate(List<byte[]> signatures, BLS01Parameters parameters) {
        Pairing pairing = PairingFactory.getPairing(parameters.getParameters());

        // get identity on G1 (For multiplication)
        Element aggregate = pairing.getG1().newElement(1);

        for (byte[] signature : signatures) {
            Element temp = pairing.getG1().newElementFromBytes(signature);
            aggregate = aggregate.mul(temp);
        }
        return aggregate.toBytes();
    }

    /**
     * Verify aggregate signatures in PKR-BLS scheme.
     * @param aggregate       Aggregate signature to be verified.
     * @param messages        All messages embedded by the component signatures.
     * @param publicKeysParam CipherParameters type of Public key, used for verifying
  aggregate signature.
     */
    public boolean verifyAgg(byte[] aggregate, List<String> messages, List<CipherParameters> publicKeysParam) {
        // get pairing from the first public key
        BLS01PublicKeyParameters publicKey = (BLS01PublicKeyParameters) ((BLS01KeyParameters) publicKeysParam.get(0));
        Pairing pairing = PairingFactory.getPairing(publicKey.getParameters().getParameters());

        // compute pairing: e(agg, g)
        Element agg = pairing.getG1().newElementFromBytes(aggregate);
        Element temp1 = pairing.pairing(agg, publicKey.getParameters().getG());

        // get identity on Gt (For following multiplication)
        Element temp2 = pairing.getGT().newElement(1);

        // handle each message, compute hash and multiplied pairing
        for (int i=0; i<messages.size(); i++) {
            // compute hash of each message
            byte[] bytes = messages.get(i).getBytes();
            Digest digest = new SHA256Digest();
            digest.reset();
            digest.update(bytes, 0, bytes.length);
            byte[] hash = new byte[digest.getDigestSize()];
            digest.doFinal(hash, 0);
            Element h = pairing.getG1().newElementFromHash(hash, 0, hash.length);
            temp2 = temp2.mul(pairing.pairing(h, ((BLS01PublicKeyParameters) ((BLS01KeyParameters) publicKeysParam.get(i))).getPk()));
        }

        return temp1.isEqual(temp2);
    }

    /**
     * Helper function
     * Generate a value of Field Zr by a given integer.
     * @param i             A given integer.
     * @param parameters    public parameter of PKRBLS scheme.
     */
    public Element setEleZr(int i, BLS01Parameters parameters) {
        Pairing pairing = PairingFactory.getPairing(parameters.getParameters());

        Element e = pairing.getZr().newElement(i);
        return e;
    }

    /**
     * Helper function
     * Sample a random value of Field Zr.
     * @param parameters    public parameter of PKRBLS scheme.
     */
    public Element sampleEleZr(BLS01Parameters parameters) {
        Pairing pairing = PairingFactory.getPairing(parameters.getParameters());

        Element r = pairing.getZr().newRandomElement();
        return r;
    }
}