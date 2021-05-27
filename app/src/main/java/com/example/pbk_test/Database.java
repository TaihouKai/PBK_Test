package com.example.pbk_test;

import androidx.room.RoomDatabase;

@androidx.room.Database(entities = {Assertion.class}, version = 1, exportSchema = false)
public abstract class Database extends RoomDatabase {
    public abstract AssertionDao assertionDao();
}
