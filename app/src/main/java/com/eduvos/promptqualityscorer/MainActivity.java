package com.eduvos.promptqualityscorer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eduvos.promptqualityscorer.model.CompanyRuleEntity;
import com.eduvos.promptqualityscorer.model.HistoryEntity;
import com.eduvos.promptqualityscorer.model.RequirementAnalysis;
import com.eduvos.promptqualityscorer.ui.ScoreBand;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.eduvos.promptqualityscorer.ui.SpecificationFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MainViewModel model;
    private EditText inputPrompt;
    private BottomNavigationView navigation;
    private RecyclerView historyRecycler;
    private HistoryAdapter historyAdapter;
    private AlertDialog standardsDialog;
    private AlertDialog addRuleDialog;
    private RecyclerView rulesRecycler;
    private TextView emptyRules;
    private boolean updatingScreen;
    private RequirementAnalysis displayedResult;
    private List<CompanyRuleEntity> displayedRules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets keyboard = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
        model = new ViewModelProvider(this).get(MainViewModel.class);
        if (savedInstanceState != null && model.draft.isEmpty() && model.analysis == null && !model.busy) {
            restoreSession(savedInstanceState);
        }
        inputPrompt = findViewById(R.id.inputPrompt);
        inputPrompt.setText(model.draft);
        inputPrompt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!updatingScreen) {
                    model.draft = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
            }
        });
        navigation = findViewById(R.id.bottomNavigation);
        navigation.setOnItemSelectedListener(item -> {
            if (updatingScreen) {
                return true;
            }
            if (item.getItemId() == R.id.nav_standards) {
                showCompanyProfileDialog();
                return true;
            }
            if (item.getItemId() == R.id.nav_history) {
                model.goHistory();
            } else {
                model.goHome();
            }
            return true;
        });
        navigation.setOnItemReselectedListener(item -> {
            if (updatingScreen) {
                return;
            }
            if (item.getItemId() == R.id.nav_standards) {
                showCompanyProfileDialog();
            } else if (item.getItemId() == R.id.nav_home) {
                model.goHome();
            } else {
                model.goHistory();
            }
        });
        historyRecycler = findViewById(R.id.recyclerHistory);
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(model.filteredHistory(), model::openHistory);
        historyRecycler.setAdapter(historyAdapter);
        ChipGroup filters = findViewById(R.id.historyFilters);
        filters.setOnCheckedStateChangeListener((group, checked) -> {
            if (updatingScreen || checked.isEmpty()) {
                return;
            }
            int id = checked.get(0);
            if (id == R.id.filterHigh) {
                model.setHistoryFilter(ScoreBand.HIGH);
            } else if (id == R.id.filterMedium) {
                model.setHistoryFilter(ScoreBand.MEDIUM);
            } else if (id == R.id.filterLow) {
                model.setHistoryFilter(ScoreBand.LOW);
            } else {
                model.setHistoryFilter(null);
            }
        });
        findViewById(R.id.btnAnalyse).setOnClickListener(v -> {
            if (model.draft.trim().isEmpty()) {
                inputPrompt.setError("Please enter a requirement");
                inputPrompt.requestFocus();
                return;
            }
            ViewCompat.getWindowInsetsController(root).hide(WindowInsetsCompat.Type.ime());
            inputPrompt.clearFocus();
            model.analyse();
        });
        findViewById(R.id.btnCompanyProfile).setOnClickListener(v -> showCompanyProfileDialog());
        findViewById(R.id.btnViewHistory).setOnClickListener(v -> model.goHistory());
        findViewById(R.id.btnPastPrompts).setOnClickListener(v -> model.goHistory());
        findViewById(R.id.btnViewSpec).setOnClickListener(v -> model.goSpec());
        findViewById(R.id.btnCancel).setOnClickListener(v -> model.cancelAnalysis());
        findViewById(R.id.btnEditPrompt).setOnClickListener(v -> model.editPrompt());
        findViewById(R.id.backLoading).setOnClickListener(v -> model.goBack());
        findViewById(R.id.backScore).setOnClickListener(v -> model.goBack());
        findViewById(R.id.backSpec).setOnClickListener(v -> model.goBack());
        findViewById(R.id.backHistory).setOnClickListener(v -> model.goBack());
        findViewById(R.id.btnCopy).setOnClickListener(v -> {
            if (model.analysis == null) {
                return;
            }
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Technical specification", model.analysis.getSpec()));
            Toast.makeText(this, "Specification copied", Toast.LENGTH_SHORT).show();
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (model.screen == MainViewModel.Screen.HOME) {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                } else {
                    model.goBack();
                }
            }
        });
        model.getScreenUpdate().observe(this, ignored -> updateScreen());
        if (savedInstanceState != null && savedInstanceState.getBoolean("standards_open")) {
            root.post(this::showCompanyProfileDialog);
        }
    }

    // All screens use the same activity, so we hide the others and show the selected layout.
    // Only really want to see the active screen we need based on the activity we are doing...
    private void updateScreen() {
        updatingScreen = true;
        findViewById(R.id.screenHome).setVisibility(View.GONE);
        findViewById(R.id.screenLoading).setVisibility(View.GONE);
        findViewById(R.id.screenScore).setVisibility(View.GONE);
        findViewById(R.id.screenSpec).setVisibility(View.GONE);
        findViewById(R.id.screenHistory).setVisibility(View.GONE);

        switch (model.screen) {
            case HOME:
                findViewById(R.id.screenHome).setVisibility(View.VISIBLE);
                break;
            case LOADING:
                findViewById(R.id.screenLoading).setVisibility(View.VISIBLE);
                break;
            case SCORE:
                findViewById(R.id.screenScore).setVisibility(View.VISIBLE);
                break;
            case SPEC:
                findViewById(R.id.screenSpec).setVisibility(View.VISIBLE);
                break;
            case HISTORY:
                findViewById(R.id.screenHistory).setVisibility(View.VISIBLE);
                break;
        }
        if (!inputPrompt.getText().toString().equals(model.draft)) {
            inputPrompt.setText(model.draft);
            inputPrompt.setSelection(inputPrompt.length());
        }
        findViewById(R.id.btnAnalyse).setEnabled(!model.busy);
        if (standardsDialog != null && standardsDialog.isShowing()) {
            navigation.setSelectedItemId(R.id.nav_standards);
        } else if (model.screen == MainViewModel.Screen.HISTORY) {
            navigation.setSelectedItemId(R.id.nav_history);
        } else {
            navigation.setSelectedItemId(R.id.nav_home);
        }
        TextView summary = findViewById(R.id.textActiveRulesSummary);
        StringBuilder active = new StringBuilder();
        int count = 0;
        for (CompanyRuleEntity rule : model.rules) {
            if (rule.isEnabled()) {
                if (count > 0) {
                    active.append(", ");
                }
                active.append(rule.getTitle());
                count++;
            }
        }
        if (!model.rulesLoaded) {
            summary.setText(R.string.loading_rules);
        } else if (count == 0) {
            summary.setText("No company standards enabled");
        } else {
            summary.setText("• Active Company Profile Standards (" + count + "): " + active);
        }
        showResult();
        int selected = R.id.filterAll;
        if (model.historyFilter == ScoreBand.HIGH) {
            selected = R.id.filterHigh;
        } else if (model.historyFilter == ScoreBand.MEDIUM) {
            selected = R.id.filterMedium;
        } else if (model.historyFilter == ScoreBand.LOW) {
            selected = R.id.filterLow;
        }
        ((ChipGroup) findViewById(R.id.historyFilters)).check(selected);
        List<HistoryEntity> items = model.filteredHistory();
        historyAdapter.setHistory(items);
        TextView empty = findViewById(R.id.emptyHistory);
        empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        if (!model.historyLoaded) {
            empty.setText(R.string.loading_history);
        } else if (model.history.isEmpty()) {
            empty.setText(R.string.empty_history);
        } else {
            empty.setText(R.string.empty_filter);
        }
        updateRuleList();
        updatingScreen = false;
        if (model.message != null) {
            Snackbar.make(findViewById(R.id.main), model.message, Snackbar.LENGTH_LONG).setAnchorView(navigation).show();
            model.message = null;
        }
    }

    private void showResult() {
        RequirementAnalysis analysis = model.analysis;
        if (analysis == null || analysis == displayedResult) {
            return;
        }
        displayedResult = analysis;
        int score = analysis.getScore();
        CircularProgressIndicator progress = findViewById(R.id.scoreProgress);
        TextView number = findViewById(R.id.scoreNumber);
        TextView scoreMessage = findViewById(R.id.scoreMessage);
        progress.setProgress(score);
        number.setText(getString(R.string.percent_score, score));

        int colour;
        if (score >= 80) {
            colour = getColor(R.color.green);
        } else if (score >= 40) {
            colour = getColor(R.color.orange);
        } else {
            colour = getColor(R.color.red);
        }
        progress.setIndicatorColor(colour);
        scoreMessage.setTextColor(colour);
        scoreMessage.setText(ScoreBand.of(score).label());
        LinearLayout gaps = findViewById(R.id.gapsContainer);
        gaps.removeAllViews();
        for (String gap : analysis.getGaps()) {
            MaterialButton row = new MaterialButton(this, null, com.google.android.material.R.attr.borderlessButtonStyle);
            row.setText(gap);
            row.setTextColor(getColor(R.color.ink));
            row.setTextSize(14);
            row.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            row.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
            row.setAllCaps(false);
            row.setIconResource(R.drawable.ic_chevron);
            row.setIconGravity(MaterialButton.ICON_GRAVITY_END);
            row.setMinHeight(dp(64));
            row.setPadding(dp(4), dp(12), dp(4), dp(12));
            row.setOnClickListener(v -> new MaterialAlertDialogBuilder(this).setTitle("Missing requirement")
                    .setMessage(gap + "\n\n" + getString(R.string.gap_help))
                    .setPositiveButton(R.string.edit_prompt, (dialog, which) -> model.editPrompt())
                    .setNegativeButton(R.string.close, null).show());
            gaps.addView(row, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        if (analysis.getGaps().isEmpty()) {
            TextView none = new TextView(this);
            none.setText(R.string.no_gaps);
            none.setTextColor(getColor(R.color.muted));
            gaps.addView(none);
        }
        ((TextView) findViewById(R.id.textSpec)).setText(SpecificationFormatter.format(analysis.getSpec()));
        TextView label = findViewById(R.id.specScoreLabel);
        TextView value = findViewById(R.id.specScoreValue);
        Integer upgradedScore = analysis.getUpgradedScore();
        if (upgradedScore != null) {
            label.setText(upgradedScore > analysis.getScore() ? R.string.score_improved : R.string.spec_score);
            value.setText(getString(R.string.percent_score, upgradedScore));
            value.setTextSize(30);
        } else {
            label.setText(R.string.spec_ready);
            value.setText(R.string.spec_unscored);
            value.setTextSize(14);
        }
    }

    private void showCompanyProfileDialog() {
        if (isFinishing() || (standardsDialog != null && standardsDialog.isShowing())) {
            return;
        }
        View view = getLayoutInflater().inflate(R.layout.dialog_company_profile, null);
        rulesRecycler = view.findViewById(R.id.recyclerCompanyRules);
        rulesRecycler.setLayoutManager(new LinearLayoutManager(this));
        emptyRules = view.findViewById(R.id.emptyRules);
        displayedRules = null;
        standardsDialog = new MaterialAlertDialogBuilder(this)
                .setBackground(androidx.appcompat.content.res.AppCompatResources.getDrawable(this, R.drawable.dialog_background))
                .setBackgroundInsetStart(0).setBackgroundInsetEnd(0).setBackgroundInsetTop(0).setBackgroundInsetBottom(0)
                .setView(view).create();
        view.findViewById(R.id.btnCloseRules).setOnClickListener(v -> standardsDialog.dismiss());
        view.findViewById(R.id.btnAddRule).setOnClickListener(v -> showAddRuleDialog());
        standardsDialog.setOnDismissListener(dialog -> {
            standardsDialog = null;
            rulesRecycler = null;
            emptyRules = null;
            updateScreen();
        });
        standardsDialog.show();
        if (standardsDialog.getWindow() != null) {
            int availableHeight = findViewById(R.id.main).getHeight();
            int height = Math.min(dp(680), Math.max(dp(220), availableHeight - dp(48)));
            int width = Math.min(dp(440), getResources().getDisplayMetrics().widthPixels - dp(48));
            standardsDialog.getWindow().setLayout(width, height);
        }
        updateScreen();
    }

    private void updateRuleList() {
        if (rulesRecycler == null || model.rules == displayedRules) {
            return;
        }
        displayedRules = model.rules;
        rulesRecycler.setAdapter(new CompanyRuleAdapter(model.rules, new CompanyRuleAdapter.OnRuleChangeListener() {
            @Override
            public void onToggle(CompanyRuleEntity rule, boolean enabled) {
                model.toggleRule(rule, enabled);
            }

            @Override
            public void onDelete(CompanyRuleEntity rule) {
                model.deleteRule(rule);
            }
        }));
        emptyRules.setVisibility(model.rules.isEmpty() ? View.VISIBLE : View.GONE);
        emptyRules.setText(model.rulesLoaded ? R.string.empty_rules : R.string.loading_rules);
    }

    private void showAddRuleDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_rule, null);
        EditText title = view.findViewById(R.id.editRuleTitle);
        EditText description = view.findViewById(R.id.editRuleDesc);
        TextInputLayout titleLayout = view.findViewById(R.id.ruleTitleLayout);
        TextInputLayout descLayout = view.findViewById(R.id.ruleDescLayout);
        addRuleDialog = new MaterialAlertDialogBuilder(this).setTitle(R.string.add_rule).setView(view)
                .setPositiveButton(R.string.save_rule, null).setNegativeButton(R.string.cancel, null).create();
        addRuleDialog.setOnShowListener(ignored -> addRuleDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = title.getText().toString().trim();
            String details = description.getText().toString().trim();
            titleLayout.setError(name.isEmpty() ? "Enter a rule title" : null);
            descLayout.setError(details.isEmpty() ? "Enter rule details" : null);
            if (!name.isEmpty() && !details.isEmpty()) {
                model.addRule(name, details);
                addRuleDialog.dismiss();
            }
        }));
        addRuleDialog.show();
        if (addRuleDialog.getWindow() != null) {
            addRuleDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        out.putString("draft", model.draft);
        out.putString("analysed_prompt", model.analysedPrompt);
        out.putString("analysis", model.analysis == null ? null : new Gson().toJson(model.analysis));
        out.putString("screen", model.screen.name());
        out.putString("origin", model.scoreOrigin.name());
        out.putString("filter", model.historyFilter == null ? "" : model.historyFilter.name());
        out.putBoolean("standards_open", standardsDialog != null && standardsDialog.isShowing());
        super.onSaveInstanceState(out);
    }

    private void restoreSession(Bundle saved) {
        model.draft = saved.getString("draft", "");
        model.analysedPrompt = saved.getString("analysed_prompt", "");
        try {
            String analysis = saved.getString("analysis");
            if (analysis != null) {
                model.analysis = AnalysisResponseParser.parseAnalysis(analysis);
            }
            model.screen = MainViewModel.Screen.valueOf(saved.getString("screen", "HOME"));
            model.scoreOrigin = MainViewModel.Screen.valueOf(saved.getString("origin", "HOME"));
            String filter = saved.getString("filter", "");
            model.historyFilter = filter.isEmpty() ? null : ScoreBand.valueOf(filter);
            // A restored session cannot continue the old request, so let the user try again.
            if (model.screen == MainViewModel.Screen.LOADING) {
                model.screen = MainViewModel.Screen.HOME;
                model.message = "Analysis was interrupted. Your prompt is ready to try again.";
            }
            if (model.analysis == null && (model.screen == MainViewModel.Screen.SCORE || model.screen == MainViewModel.Screen.SPEC)) {
                model.screen = MainViewModel.Screen.HOME;
            }
        } catch (Exception ignored) {
            model.screen = MainViewModel.Screen.HOME;
        }
    }

    @Override
    protected void onDestroy() {
        if (standardsDialog != null) {
            standardsDialog.setOnDismissListener(null);
            standardsDialog.dismiss();
        }
        if (addRuleDialog != null) {
            addRuleDialog.dismiss();
        }
        super.onDestroy();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
