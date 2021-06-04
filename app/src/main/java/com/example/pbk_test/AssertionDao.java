package com.example.pbk_test;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AssertionDao {

    @Query("SELECT * FROM assertions")
    List<Assertion> getAll();

    @Query("SELECT * FROM assertions WHERE assertionId IN (:ids)")
    List<Assertion> findAllByIDs(List<Integer> ids);

    /*
    @Query("SELECT * FROM assertion WHERE nym = :thisNym LIMIT 1")
    Assertion findByNym(byte[] thisNym);
     */

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Assertion Assertion);

    @Query("DELETE FROM assertions")
    void delete();

    @Query("UPDATE assertions SET pseudonym=:newNym WHERE assertionId=:id")
    void updateNym(byte[] newNym, int id);

    @Query("UPDATE assertions SET isSaved=:saved WHERE assertionId=:id")
    void updateIsSaved(boolean saved, int id);
}
