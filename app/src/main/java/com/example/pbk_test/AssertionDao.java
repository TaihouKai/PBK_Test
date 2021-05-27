package com.example.pbk_test;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.bouncycastle.crypto.CipherParameters;

import java.util.List;

@Dao
public interface AssertionDao {

    @Query("SELECT * FROM assertion")
    List<Assertion> getAll();

    /*
    @Query("SELECT * FROM assertion WHERE nym IN (:nyms)")
    List<Assertion> findAllByNyms(byte[][] nyms);

    @Query("SELECT * FROM assertion WHERE nym = :thisNym LIMIT 1")
    Assertion findByNym(byte[] thisNym);
     */

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Assertion Assertion);

    @Query("DELETE FROM assertion")
    void delete();
}
