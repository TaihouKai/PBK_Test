package com.example.pbk_test;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import it.unisa.dia.gas.crypto.jpbc.signature.bls01.engines.BLS01Signer;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.generators.BLS01KeyPairGenerator;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.generators.BLS01ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01KeyGenerationParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01Parameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;


public class BLS01 {

    private Context context;

    public BLS01(Context context) {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public BLS01Parameters setup() throws IOException {
        BLS01ParametersGenerator setup = new BLS01ParametersGenerator();
        setup.init(PairingFactory.getPairingParameters(MainActivity.getCacheFile("a.properties", context).toPath().toString()));
        // setup.init(PairingFactory.getPairingParameters(MainActivity.propertiesPath));
        // setup.init(PairingFactory.getPairingParameters(context.getAssets().open("a.properties")));

        return setup.generateParameters();
    }

    public AsymmetricCipherKeyPair keyGen(BLS01Parameters parameters) {
        BLS01KeyPairGenerator keyGen = new BLS01KeyPairGenerator();
        keyGen.init(new BLS01KeyGenerationParameters(null, parameters));

        return keyGen.generateKeyPair();
    }

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

    public byte[] aggregate(List<byte[]> signatures) {
        Pairing pairing = PairingFactory.getPairing(MainActivity.getCacheFile("a.properties", context).toPath().toString()));
        Field f = pairing.getG1();
        Element aggregated = f.newElement(1);
        for (byte[] signature: signatures) {
            Element temp = pairing.getG1().newElementFromBytes(signature);
            aggregated = aggregated.mul(temp);
        }
        return aggregated.toBytes();
    }

    public boolean verify(byte[] signature, String message, CipherParameters publicKey) {
        byte[] bytes = message.getBytes();

        BLS01Signer signer = new BLS01Signer(new SHA256Digest());
        signer.init(false, publicKey);
        signer.update(bytes, 0, bytes.length);

        return signer.verifySignature(signature);
    }

//    public boolean verifyAggre(byte[] aggregated, List<String> messages, @NotNull List<CipherParameters> publicKeys) {
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
