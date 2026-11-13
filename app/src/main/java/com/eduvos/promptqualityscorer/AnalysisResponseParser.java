package com.eduvos.promptqualityscorer;

import com.eduvos.promptqualityscorer.model.RequirementAnalysis;
import com.google.gson.*;
import java.util.ArrayList;
import java.util.List;

/** Validates model output before it is allowed into the UI or saved history. */
public final class AnalysisResponseParser {
    private AnalysisResponseParser() {
    }

    public static RequirementAnalysis parse(String responseBody) {
        JsonObject envelope = JsonParser.parseString(responseBody).getAsJsonObject();
        String content = envelope.getAsJsonArray("choices").get(0).getAsJsonObject()
                .getAsJsonObject("message").get("content").getAsString().trim();
        // Strip only an outer fence, preserving any code blocks inside the specification.
        if (content.startsWith("```")) {
            int firstLine = content.indexOf('\n');
            int closing = content.lastIndexOf("```");
            if (firstLine < 0 || closing <= firstLine) {
                throw new IllegalArgumentException("Incomplete JSON response");
            }
            content = content.substring(firstLine + 1, closing).trim();
        }
        return parseAnalysis(content);
    }

    public static RequirementAnalysis parseAnalysis(String json) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        RequirementAnalysis analysis = new RequirementAnalysis();
        analysis.setScore(readScore(object.get("score")));
        JsonElement gapsElement = object.get("gaps");
        if (gapsElement == null || !gapsElement.isJsonArray()) {
            throw new IllegalArgumentException("Missing gap list");
        }
        List<String> gaps = new ArrayList<>();
        for (JsonElement value : gapsElement.getAsJsonArray()) {
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException("Invalid gap description");
            }
            if (!value.getAsString().trim().isEmpty()) {
                gaps.add(value.getAsString().trim());
            }
        }
        analysis.setGaps(gaps);
        JsonElement spec = object.get("spec");
        if (spec == null || !spec.isJsonPrimitive() || !spec.getAsJsonPrimitive().isString() || spec.getAsString().trim().isEmpty()) {
            throw new IllegalArgumentException("Missing specification");
        }
        analysis.setSpec(spec.getAsString());
        if (object.has("upgradedScore") && !object.get("upgradedScore").isJsonNull()) {
            analysis.setUpgradedScore(readScore(object.get("upgradedScore")));
        }
        return analysis;
    }

    private static int readScore(JsonElement element) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("Missing numeric score");
        }
        double score = element.getAsDouble();
        if (!Double.isFinite(score) || score < 0 || score > 100 || score != Math.floor(score)) {
            throw new IllegalArgumentException("Score must be a whole number from 0 to 100");
        }
        return (int) score;
    }
}
