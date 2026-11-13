package com.eduvos.promptqualityscorer.ui;

/** Shared boundaries for the score screen and history filters. */
public enum ScoreBand {
    HIGH, MEDIUM, LOW;

    public static ScoreBand of(int score) {
        if (score >= 80) {
            return HIGH;
        } else if (score >= 40) {
            return MEDIUM;
        } else {
            return LOW;
        }
    }

    public String label() {
        switch (this) {
            case HIGH:
                return "Looks good";
            case MEDIUM:
                return "Needs work";
            default:
                return "Needs detail";
        }
    }
}
