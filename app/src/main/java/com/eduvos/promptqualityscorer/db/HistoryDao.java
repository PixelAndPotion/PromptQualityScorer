package com.eduvos.promptqualityscorer.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.eduvos.promptqualityscorer.model.HistoryEntity;

import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    void insert(HistoryEntity history);

    @Query("SELECT * FROM prompt_history ORDER BY timestamp DESC")
    List<HistoryEntity> getAllHistory();
}
