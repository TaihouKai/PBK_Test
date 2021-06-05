package com.example.pbk_test;

import android.content.Context;

import java.util.List;

public class Doctor {

    public final PKRBLS pkrbls;

    public Doctor(Context context) {
        this.pkrbls = new PKRBLS(context);
    }

    public boolean verifyShow(List<Record> list) {
        // if list is empty, return false.
        if (list.isEmpty())
            return false;
        boolean res = true;
        for (Record r: list) {
            res = this.pkrbls.verifyAgg(r.signature, r.msgs, r.nyms);
            if (!res)
                break;
        }
        return res;
    }
}
