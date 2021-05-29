package com.example.pbk_test;

import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@androidx.room.Database(entities = {CompressedAssertion.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class CompressedDatabase extends RoomDatabase{
    public abstract CompressedAssertionDao compressedAssertionDao();
}
