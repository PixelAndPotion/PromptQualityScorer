package com.eduvos.promptqualityscorer.model;

import java.util.List;

public class RequirementAnalysis {
    private int score;
    private List<String> gaps;
    private String spec;
    private Integer upgradedScore;

    public RequirementAnalysis() {
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getGaps() {
        return gaps;
    }

    public void setGaps(List<String> gaps) {
        this.gaps = gaps;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public Integer getUpgradedScore() {
        return upgradedScore;
    }

    public void setUpgradedScore(Integer upgradedScore) {
        this.upgradedScore = upgradedScore;
    }
}
