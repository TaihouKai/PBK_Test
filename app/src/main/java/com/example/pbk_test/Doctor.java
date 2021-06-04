package com.example.pbk_test;

import android.content.Context;

import java.util.List;

public class Doctor {

    public final PKRBLS pkrbls;

    public Doctor(Context context) {
        this.pkrbls = new PKRBLS(context);
    }

    public boolean verifyShow(List<Record> list) {
        boolean res = true;
        for (Record r: list) {
            res = this.pkrbls.verifyAgg(r.signature, r.msgs, r.nyms);
            if (!res)
                break;
        }
        return res;
    }
}
