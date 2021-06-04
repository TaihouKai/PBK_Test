package com.example.pbk_test;

import org.bouncycastle.crypto.CipherParameters;

import java.util.ArrayList;
import java.util.List;

import it.unisa.dia.gas.crypto.jpbc.signature.bls01.params.BLS01Parameters;
import it.unisa.dia.gas.jpbc.Element;

public class Record {
    public byte[] signature;
    public List<CipherParameters> nyms;
    public List<String> msgs;
    public List<byte[]> gPowRs;

    public Record(byte[] signature, PKRBLS pkrbls, BLS01Parameters parameters, Element r) {
        this.signature = pkrbls.updateSIG(signature, parameters, r);
        this.nyms = new ArrayList<>();
        this.msgs = new ArrayList<>();
        this.gPowRs = new ArrayList<>();
    }
}
