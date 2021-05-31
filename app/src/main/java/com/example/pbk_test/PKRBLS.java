package com.example.pbk_test;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA256Digest;

import java.io.IOException;
import java.util.List;

import it.unisa.dia.gas.crypto.jpbc.signature.bls01.engines.BLS01Signer;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.generators.BLS01KeyPairGenerator;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.generators.BLS01ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01KeyGenerationParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01Parameters;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01KeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01PublicKeyParameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.bouncycastle.crypto.CipherParameters;


public class PKRBLS {

    private Context context;
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
     * @throws IOException Error when a.properties is not found
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public BLS01Parameters setup() throws IOException {
        BLS01ParametersGenerator setup = new BLS01ParametersGenerator();
        setup.init(PairingFactory.getPairingParameters(MainActivity.getCacheFile("a.properties", context).toPath().toString()));
        // setup.init(PairingFactory.getPairingParameters(MainActivity.propertiesPath));
        // setup.init(PairingFactory.getPairingParameters(context.getAssets().open("a.properties")));

        return setup.generateParameters();
    }

    /**
     * Generate public-secret key pair for PKR-BLS scheme.
     * Same as the original BLS scheme.
     * @param parameters     Type III pairing parameters
     * @throws IOException   Error when a.properties is not found
     */
    public AsymmetricCipherKeyPair keyGen(BLS01Parameters parameters) {
        BLS01KeyPairGenerator keyGen = new BLS01KeyPairGenerator();
        keyGen.init(new BLS01KeyGenerationParameters(null, parameters));

        return keyGen.generateKeyPair();
    }

    /**
     * Sign signatures in PKR-BLS scheme.
     * Same as the original BLS scheme.
     * @param message     String message to be signed.
     * @param privateKey  Secret key of signer, used for signing message.
     */
    public byte[] sign(String message, CipherParameters privateKey) {
        byte[] bytes = message.getBytes();

        BLS01Signer signer = new BLS01Signer(new SHA256Digest());
        signer.init(true, privateKey);
        signer.update(bytes, 0, bytes.length);

        byte[] signature = null;
        try {
            signature = signer.generateSignature();
        } catch (CryptoException e) {
            throw new RuntimeException(e);
        }
        return signature;
    }

    /**
     * Get pairing from given public key.
     * @param publicKey      public key.
     * @throws IOException   Error when a.properties is not found.
     */
    public Pairing getPairingFromPK(CipherParameters publicKey) throws IOException {
        Element g = ((BLS01KeyParameters)publicKey).getParameters().getG();
        Element pk = ((BLS01PublicKeyParameters)((BLS01KeyParameters)publicKey)).getPk();
        BLS01Parameters param = new BLS01Parameters(PairingFactory.getPairingParameters(MainActivity.getCacheFile("a.properties", context).toPath().toString()), MainActivity.getElementFromBytes(g, 2, context));

        Pairing pairing = PairingFactory.getPairing(param.getParameters());
        return pairing;
    }

    /**
     * Update public key with a random number r.
     * @param publicKey      public key to be updated.
     * @throws IOException   Error when a.properties is not found.
     */
    public CipherParameters updatePK(CipherParameters publicKey) throws IOException {
        Pairing pairing = getPairingFromPK(publicKey);

//         Generate the new random number r
        Element r = pairing.getZr().newRandomElement();

        // Update the corresponding public key
        Element updated = pk.powZn(r);
        BLS01PublicKeyParameters updatedPK = new BLS01PublicKeyParameters(param, updated.getImmutable());

        return updatedPK;
    }

    /**
     * Aggregate multiple signatures from PKR-BLS scheme.
     * @param signatures     A list of signatures to be aggregated by group multiplication.
     * @throws IOException   Error when a.properties is not found
     */
    public byte[] aggregate(List<byte[]> signatures) throws IOException {
        Pairing pairing = PairingFactory.getPairing(MainActivity.getCacheFile("a.properties", context).toPath().toString());
        Field f = pairing.getG1();
        Element aggregated = f.newElement(1);
        for (byte[] signature: signatures) {
            Element temp = pairing.getG1().newElementFromBytes(signature);
            aggregated = aggregated.mul(temp);
        }
        return aggregated.toBytes();
    }

    /**
     * Verify signatures in PKR-BLS scheme.
     * @param signature   Signature to be verified.
     * @param message     The message embedded by the signature
     * @param publicKey   Public key, used for verifying message-signature pair. Can be updated.
     */
    public boolean verify(byte[] signature, String message, CipherParameters publicKey) {
        byte[] bytes = message.getBytes();

        BLS01Signer signer = new BLS01Signer(new SHA256Digest());
        signer.init(false, publicKey);
        signer.update(bytes, 0, bytes.length);

        return signer.verifySignature(signature);
    }

    /**
     * Verify aggregate signatures in PKR-BLS scheme.
     * @param aggregated   Aggregate signature to be verified.
     * @param messages     All messages embedded by the component signatures
     * @param publicKeys   Public keys, used for verifying each component message-signature pair. Can be updated
     */
//    public boolean verifyAggre(byte[] aggregated, List<String> messages, List<CipherParameters> publicKeys) {
//        byte[] bytes = message.getBytes();
//
//        BLS01Signer signer = new BLS01Signer(new SHA256Digest());
//        signer.init(false, publicKey);
//        signer.update(bytes, 0, bytes.length);
//
//        return signer.verifySignature(signature);
//
//        Element aggre = pairing.getG1().newElementFromBytes(aggregated);
//
//        for (String message: messages) {
//            Element h = pairing.getG1().newElementFromHash(hash, 0, hash.length);
//        }
//        Element h = pairing.getG1().newElementFromHash(hash, 0, hash.length);
//
//        Element temp1 = pairing.pairing(sig, publicKey.getParameters().getG());
//        Element temp2 = pairing.pairing(h, publicKey.getPk());
//
//        return temp1.isEqual(temp2);
//    }




}
