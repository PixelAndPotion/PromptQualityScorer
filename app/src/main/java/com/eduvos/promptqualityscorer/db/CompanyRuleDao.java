package com.eduvos.promptqualityscorer.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.eduvos.promptqualityscorer.model.CompanyRuleEntity;

import java.util.List;

@Dao
public interface CompanyRuleDao {
    @Insert
    long insert(CompanyRuleEntity rule);

    @Update
    void update(CompanyRuleEntity rule);

    @Delete
    void delete(CompanyRuleEntity rule);

    @Query("SELECT * FROM company_rules ORDER BY id ASC")
    List<CompanyRuleEntity> getAllRules();

    @Query("SELECT * FROM company_rules WHERE enabled = 1 ORDER BY id ASC")
    List<CompanyRuleEntity> getActiveRules();

    @Query("SELECT COUNT(*) FROM company_rules")
    int getRuleCount();
}
