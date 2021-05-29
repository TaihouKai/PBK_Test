package com.example.pbk_test;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CompressedAssertionDao {

    @Query("SELECT * FROM compressedassertion")
    List<CompressedAssertion> getAll();

    /*
    @Query("SELECT * FROM assertion WHERE nym IN (:nyms)")
    List<Assertion> findAllByNyms(byte[][] nyms);

    @Query("SELECT * FROM assertion WHERE nym = :thisNym LIMIT 1")
    Assertion findByNym(byte[] thisNym);
     */

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(CompressedAssertion compressedAssertion);

    @Query("DELETE FROM CompressedAssertion")
    void delete();
}
