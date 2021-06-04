package com.example.pbk_test;

import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@androidx.room.Database(entities = {Assertion.class, CompressedAssertion.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class Database extends RoomDatabase {
    public abstract AssertionDao assertionDao();
    public abstract CompressedAssertionDao compressedAssertionDao();
}
