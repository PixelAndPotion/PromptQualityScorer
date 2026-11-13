package com.eduvos.promptqualityscorer;

import com.eduvos.promptqualityscorer.model.RequirementAnalysis;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PromptAnalyserService {
    // This address lets the emulator reach LM Studio running on the computer.
    private static final String API_URL = "http://10.0.2.2:1234/v1/chat/completions";
    private final OkHttpClient client;

    public PromptAnalyserService() {
        // The whole request has a 90-second limit; these timeouts are not added together. Depending on the system though we can adjust these before running
        // Slower systems would need a higher timeout.
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .callTimeout(90, TimeUnit.SECONDS)
                .build();
    }

    public okhttp3.Call analysePrompt(String prompt, String companyContext, AnalysisCallback callback) {
        String systemMessage = "You are an expert Senior Requirements Engineer.\n" +
                "Analyse the user's rough prompt for completeness, clarity, architecture, edge cases, and compliance.\n\n" +
                "CRITICAL INSTRUCTION - COMPANY PROFILE & STANDARDS:\n" +
                "The organization has active company requirements and compliance standards:\n" +
                (companyContext.isEmpty() ? "None specified." : companyContext) + "\n\n" +
                "REQUIREMENTS ANALYSIS ENGINE DIRECTIVES:\n" +
                "1. Check if the user prompt lacks specific details mandated by the Company Profile standards (e.g. if prompt says 'it should be secure', but Company Profile mandates PCI-DSS or HIPAA, flag missing PCI-DSS tokenization, TLS 1.3, audit logging, etc. as explicit gaps!).\n" +
                "2. Calculate a clarity and completeness score (integer 0-100).\n" +
                "3. Provide a list of specific missing requirement gaps found.\n" +
                "4. Generate a comprehensive, structured Technical Specification in Markdown format that incorporates both the user's requirement AND resolves all Company Profile compliance standards.\n\n" +
                "5. Evaluate the generated specification using the same scoring criteria and return its actual score as upgradedScore. Do not assume it is perfect or higher.\n\n" +
                "Output ONLY a valid JSON object matching this schema:\n" +
                "{\"score\": <integer 0-100>, \"upgradedScore\": <integer 0-100>, \"gaps\": [\"gap 1\", \"gap 2\"], \"spec\": \"<markdown specification text>\"}";

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("model", "local-model");
        JsonArray messages = new JsonArray();

        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", systemMessage);
        messages.add(sysMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", prompt);
        messages.add(userMsg);

        jsonBody.add("messages", messages);
        jsonBody.addProperty("temperature", 0.1);

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        okhttp3.Call requestCall = client.newCall(request);
        // Send the request in the background so the user can still cancel while waiting.
        requestCall.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                if (!call.isCanceled()) {
                    callback.onError("Could not reach the analysis server. Check that your local model is running, then try again.");
                }
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) {
                try (Response closedResponse = response) {
                    if (call.isCanceled()) {
                        return;
                    }
                    if (!closedResponse.isSuccessful()) {
                        callback.onError("The analysis server returned HTTP " + closedResponse.code() + ". Check the loaded model and try again.");
                        return;
                    }
                    if (closedResponse.body() == null) {
                        throw new IOException("Empty response");
                    }
                    RequirementAnalysis analysis = AnalysisResponseParser.parse(closedResponse.body().string());
                    if (!call.isCanceled()) {
                        callback.onSuccess(analysis);
                    }
                } catch (Exception e) {
                    if (!call.isCanceled()) {
                        callback.onError("The model returned an incomplete analysis. Please try again.");
                    }
                }
            }
        });
        return requestCall;
    }

    public interface AnalysisCallback {
        void onSuccess(RequirementAnalysis analysis);
        void onError(String error);
    }
}
