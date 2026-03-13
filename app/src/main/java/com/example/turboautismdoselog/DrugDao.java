package com.example.turboautismdoselog;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DrugDao {

    @Insert
    void insert(DrugEntry entry);

    @Query("SELECT * FROM DrugEntry ORDER BY timestamp DESC")
    List<DrugEntry> getAll();

    @Delete
    void delete(DrugEntry entry);

    @Update
    void update(DrugEntry entry);

    @Query(
            "SELECT drug, " +
                    "COUNT(*) AS total, " +
                    "MIN(timestamp) AS firstTimestamp," +
                    "MAX(timestamp) AS lastTimestamp " +
                    "FROM DrugEntry " +
                    "GROUP BY drug " +
                    "ORDER BY total DESC"
    )
    List<DrugStats> getDrugStats();
}