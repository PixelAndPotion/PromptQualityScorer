package com.eduvos.promptqualityscorer;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.eduvos.promptqualityscorer.db.AppDatabase;
import com.eduvos.promptqualityscorer.model.CompanyRuleEntity;
import com.eduvos.promptqualityscorer.model.HistoryEntity;
import com.eduvos.promptqualityscorer.model.RequirementAnalysis;
import com.eduvos.promptqualityscorer.ui.ScoreBand;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.Call;

// Keeps the current prompt and result when the phone rotates.
public class MainViewModel extends AndroidViewModel {
    public enum Screen {
        HOME, LOADING, SCORE, SPEC, HISTORY
    }

    private final MutableLiveData<Boolean> screenUpdate = new MutableLiveData<>(true);
    private final Handler mainThread = new Handler(Looper.getMainLooper());
    private final ExecutorService databaseThread = Executors.newSingleThreadExecutor();
    private final AppDatabase db;
    private final PromptAnalyserService service;
    private final Gson gson = new Gson();
    private Call activeCall;
    private int requestNumber;
    private boolean closed;
    public String draft = "";
    public String analysedPrompt = "";
    public RequirementAnalysis analysis;
    public Screen screen = Screen.HOME;
    public Screen scoreOrigin = Screen.HOME;
    public ScoreBand historyFilter;
    public boolean busy;
    public boolean rulesLoaded;
    public boolean historyLoaded;
    public List<CompanyRuleEntity> rules = new ArrayList<>();
    public List<HistoryEntity> history = new ArrayList<>();
    public String message;

    public MainViewModel(@NonNull Application application) {
        super(application);
        service = new PromptAnalyserService();
        db = AppDatabase.getInstance(application);
        executeDatabase(() -> {
            SharedPreferences preferences = application.getSharedPreferences("setup", 0);
            // Add the example rules once, so deleted rules do not come back when the app opens.
            if (!preferences.getBoolean("company_rules_seeded", false)) {
                db.runInTransaction(() -> {
                    if (db.companyRuleDao().getRuleCount() == 0) {
                        db.companyRuleDao().insert(new CompanyRuleEntity("PCI-DSS Compliance Standard", "Mandatory for payment handling. Requires TLS 1.3 encryption, credit card tokenisation, and strict access logging. No plain text card data allowed.", true));
                        db.companyRuleDao().insert(new CompanyRuleEntity("HIPAA Data Protection Standard", "Mandatory for healthcare/patient data. All Protected Health Information (PHI) must be encrypted at rest and in transit with strict audit trails.", true));
                    }
                });
                preferences.edit().putBoolean("company_rules_seeded", true).apply();
            }
            loadRules();
            loadHistory();
        }, "Could not load saved data. Reopen the app to try again.");
    }

    public LiveData<Boolean> getScreenUpdate() {
        return screenUpdate;
    }

    private void updateScreen() {
        if (!closed) {
            screenUpdate.setValue(true);
        }
    }

    public void goHome() {
        if (busy) {
            screen = Screen.LOADING;
        } else {
            screen = Screen.HOME;
        }
        updateScreen();
    }

    public void goHistory() {
        screen = Screen.HISTORY;
        refreshHistory();
        updateScreen();
    }

    // The specification came back with the score, so there is no second model request here.
    public void goSpec() {
        if (analysis != null) {
            screen = Screen.SPEC;
            updateScreen();
        }
    }

    public void goBack() {
        switch (screen) {
            case LOADING:
                cancelAnalysis();
                break;
            case SPEC:
                screen = Screen.SCORE;
                updateScreen();
                break;
            case SCORE:
                screen = scoreOrigin;
                updateScreen();
                break;
            default:
                goHome();
                break;
        }
    }

    public void editPrompt() {
        draft = analysedPrompt;
        screen = Screen.HOME;
        updateScreen();
    }

    public void setHistoryFilter(ScoreBand filter) {
        historyFilter = filter;
        updateScreen();
    }

    public List<HistoryEntity> filteredHistory() {
        List<HistoryEntity> result = new ArrayList<>();
        for (HistoryEntity item : history) {
            if (historyFilter == null || ScoreBand.of(item.getScore()) == historyFilter) {
                result.add(item);
            }
        }
        return result;
    }

    // Start the analysis here, then show the result and save it once the model replies.
    public void analyse() {
        if (busy) {
            return;
        }
        final String submitted = draft.trim();
        if (submitted.isEmpty()) {
            message = "Please enter a requirement first.";
            updateScreen();
            return;
        }
        busy = true;
        screen = Screen.LOADING;
        // Ignore replies from older or cancelled requests.
        requestNumber++;
        final int thisRequest = requestNumber;
        updateScreen();
        databaseThread.execute(() -> {
            try {
                // Only send the standards that the user has switched on.
                // We want to ignore what is not selected here.
                List<CompanyRuleEntity> active = db.companyRuleDao().getActiveRules();
                StringBuilder context = new StringBuilder();
                for (CompanyRuleEntity rule : active) {
                    context.append("- ").append(rule.getTitle()).append(": ").append(rule.getDescription()).append('\n');
                }
                mainThread.post(() -> {
                    if (closed || thisRequest != requestNumber) {
                        return;
                    }
                    activeCall = service.analysePrompt(submitted, context.toString(), new PromptAnalyserService.AnalysisCallback() {
                        @Override
                        public void onSuccess(RequirementAnalysis result) {
                            mainThread.post(() -> {
                                if (closed || thisRequest != requestNumber) {
                                    return;
                                }
                                activeCall = null;
                                busy = false;
                                analysis = result;
                                analysedPrompt = submitted;
                                scoreOrigin = Screen.HOME;
                                if (screen == Screen.LOADING) {
                                    screen = Screen.SCORE;
                                }
                                saveHistory(submitted, result);
                                updateScreen();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            mainThread.post(() -> analysisFailed(thisRequest, error));
                        }
                    });
                });
            } catch (Exception e) {
                mainThread.post(() -> analysisFailed(thisRequest, "Could not read company standards. Please try again."));
            }
        });
    }

    private void analysisFailed(int thisRequest, String error) {
        if (closed || thisRequest != requestNumber) {
            return;
        }
        activeCall = null;
        busy = false;
        if (screen == Screen.LOADING) {
            screen = Screen.HOME;
        }
        message = error;
        updateScreen();
    }

    public void cancelAnalysis() {
        requestNumber++;
        if (activeCall != null) {
            activeCall.cancel();
        }
        activeCall = null;
        busy = false;
        screen = Screen.HOME;
        updateScreen();
    }

    private void saveHistory(String submitted, RequirementAnalysis result) {
        // Save the submitted prompt, even if the input box has changed.
        HistoryEntity entity = new HistoryEntity();
        entity.setOriginalPrompt(submitted);
        entity.setScore(result.getScore());
        entity.setGapsJson(gson.toJson(result.getGaps()));
        entity.setGeneratedSpec(result.getSpec());
        entity.setUpgradedScore(result.getUpgradedScore());
        entity.setTimestamp(System.currentTimeMillis());
        executeDatabase(() -> {
            db.historyDao().insert(entity);
            loadHistory();
        }, "Your result is ready, but history could not be saved.");
    }

    // Open the saved result from the database without sending the prompt to the model again.
    // This can get a bit "loopy" but this code seems to work for now.
    public void openHistory(HistoryEntity item) {
        if (busy) {
            message = "Cancel the current analysis before opening a saved result.";
            updateScreen();
            return;
        }
        try {
            RequirementAnalysis restored = new RequirementAnalysis();
            restored.setScore(item.getScore());
            List<String> gaps = gson.fromJson(item.getGapsJson(), new TypeToken<List<String>>() {}.getType());
            restored.setGaps(gaps == null ? new ArrayList<>() : gaps);
            restored.setSpec(item.getGeneratedSpec() == null ? "" : item.getGeneratedSpec());
            restored.setUpgradedScore(item.getUpgradedScore());
            analysis = restored;
            analysedPrompt = item.getOriginalPrompt() == null ? "" : item.getOriginalPrompt();
            draft = analysedPrompt;
            scoreOrigin = Screen.HISTORY;
            screen = Screen.SCORE;
        } catch (Exception e) {
            message = "This saved result could not be opened.";
        }
        updateScreen();
    }

    public void addRule(String title, String description) {
        executeDatabase(() -> {
            db.companyRuleDao().insert(new CompanyRuleEntity(title, description, true));
            loadRules();
        }, "Could not save the rule. Please try again.");
    }

    public void toggleRule(CompanyRuleEntity rule, boolean enabled) {
        // Each change needs its own copy if the switch is tapped quickly.
        CompanyRuleEntity update = new CompanyRuleEntity(rule.getTitle(), rule.getDescription(), enabled);
        update.setId(rule.getId());
        executeDatabase(() -> {
            db.companyRuleDao().update(update);
            loadRules();
        }, "Could not update the rule. Please try again.");
    }

    public void deleteRule(CompanyRuleEntity rule) {
        executeDatabase(() -> {
            db.companyRuleDao().delete(rule);
            loadRules();
        }, "Could not delete the rule. Please try again.");
    }

    public void refreshHistory() {
        executeDatabase(this::loadHistory, "Could not load history. Try opening it again.");
    }

    private void loadRules() {
        List<CompanyRuleEntity> loaded = db.companyRuleDao().getAllRules();
        mainThread.post(() -> {
            rules = loaded;
            rulesLoaded = true;
            updateScreen();
        });
    }

    private void loadHistory() {
        List<HistoryEntity> loaded = db.historyDao().getAllHistory();
        mainThread.post(() -> {
            history = loaded;
            historyLoaded = true;
            updateScreen();
        });
    }

    // Run database work separately so the screen does not have to wait for it.
    private void executeDatabase(Runnable task, String errorMessage) {
        databaseThread.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                mainThread.post(() -> {
                    message = errorMessage;
                    updateScreen();
                });
            }
        });
    }

    @Override
    protected void onCleared() {
        closed = true;
        requestNumber++;
        if (activeCall != null) {
            activeCall.cancel();
        }
        databaseThread.shutdown();
    }
}
