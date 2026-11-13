package com.eduvos.promptqualityscorer.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "company_rules")
public class CompanyRuleEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private boolean enabled;

    public CompanyRuleEntity() {
    }

    @Ignore
    public CompanyRuleEntity(String title, String description, boolean enabled) {
        this.title = title;
        this.description = description;
        this.enabled = enabled;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
