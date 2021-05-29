package com.example.pbk_test;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.util.List;

@Entity
public class CompressedAssertion {

    @PrimaryKey
    @NonNull
    public byte[] signature;

    @TypeConverters(Converters.class)
    @ColumnInfo(name = "ids")
    public List<Integer> ids;

    public CompressedAssertion(byte[] signature, List<Integer> ids) {
        this.signature = signature;
        this.ids = ids;
    }

    public CompressedAssertion() {}
}
