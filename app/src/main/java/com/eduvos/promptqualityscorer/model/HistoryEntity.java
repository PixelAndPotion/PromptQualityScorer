package com.eduvos.promptqualityscorer.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "prompt_history")
public class HistoryEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String originalPrompt;
    private int score;
    private String gapsJson;
    // Storing as JSON string
    private String generatedSpec;
    private long timestamp;
    private Integer upgradedScore;

    public HistoryEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOriginalPrompt() {
        return originalPrompt;
    }

    public void setOriginalPrompt(String originalPrompt) {
        this.originalPrompt = originalPrompt;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getGapsJson() {
        return gapsJson;
    }

    public void setGapsJson(String gapsJson) {
        this.gapsJson = gapsJson;
    }

    public String getGeneratedSpec() {
        return generatedSpec;
    }

    public void setGeneratedSpec(String generatedSpec) {
        this.generatedSpec = generatedSpec;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getUpgradedScore() {
        return upgradedScore;
    }

    public void setUpgradedScore(Integer upgradedScore) {
        this.upgradedScore = upgradedScore;
    }
}
