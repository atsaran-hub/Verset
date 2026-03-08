package com.example.verset.Tiktoklike;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.verset.Prefs;
import com.example.verset.R;
import com.example.verset.widgetcustome.widgetcustom;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.Set;

public class ProfileActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private boolean ignoreNavSelection = false;

    // TextViews values
    private TextView valueName, valueAge, valueGender, valueRelationship,
            valueTradition, valuePrayer, valueVersesPerDay, valueTone, valueStreak, valueGoals;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupBottomNav();
        loadProfileData();
        setupRowClicks();
    }

    // ---------------------------------------------------
    // INIT
    // ---------------------------------------------------

    private void initViews() {
        bottomNav = findViewById(R.id.bottomNav);

        valueName = findViewById(R.id.valueName);
        valueAge = findViewById(R.id.valueAge);
        valueGender = findViewById(R.id.valueGender);
        valueRelationship = findViewById(R.id.valueRelationship);
        valueTradition = findViewById(R.id.valueTradition);
        valuePrayer = findViewById(R.id.valuePrayer);
        valueVersesPerDay = findViewById(R.id.valueVersesPerDay);
        valueTone = findViewById(R.id.valueTone);
        valueStreak = findViewById(R.id.valueStreak);

        // ✅ NEW
        valueGoals = findViewById(R.id.valueGoals);

        MaterialButton btnSave = findViewById(R.id.btnSaveProfile);
        btnSave.setOnClickListener(v -> finish());
    }

    // ---------------------------------------------------
    // LOAD DATA
    // ---------------------------------------------------

    private void loadProfileData() {
        valueName.setText(getOrDash(Prefs.getName(this)));
        valueAge.setText(getOrDash(Prefs.getAge(this)));
        valueGender.setText(getOrDash(Prefs.getGender(this)));
        valueRelationship.setText(getOrDash(Prefs.getRelationshipStatus(this)));
        valueTradition.setText(getOrDash(Prefs.getTradition(this)));

        valuePrayer.setText(getOrDash(Prefs.getRecueillementFrequency(this)));

        int desired = Prefs.getDailyVersesDesired(this);
        int applied = Prefs.getDailyVersesApplied(this);
        valueVersesPerDay.setText(desired + " (applied " + applied + ")");

        valueTone.setText(getOrDash(Prefs.getTone(this)));
        valueStreak.setText(String.valueOf(Prefs.getStreakGoalDays(this)));

        // ✅ NEW
        valueGoals.setText(formatGoalsForDisplay(Prefs.getGoals(this)));
    }

    private String getOrDash(String value) {
        return (value == null || value.trim().isEmpty()) ? "—" : value;
    }

    // ---------------------------------------------------
    // EDIT ROWS
    // ---------------------------------------------------

    private void setupRowClicks() {

        findViewById(R.id.rowName).setOnClickListener(v ->
                showEditDialog("Name", Prefs.getName(this), text -> {
                    Prefs.saveName(this, text);
                    valueName.setText(getOrDash(text));
                })
        );

        findViewById(R.id.rowAge).setOnClickListener(v ->
                showChoiceSheet(
                        "Age",
                        new String[]{"18–24", "25–34", "35–44", "45–54", "55+"},
                        Prefs.getAge(this),
                        choice -> {
                            Prefs.saveAge(this, choice);
                            valueAge.setText(getOrDash(choice));
                        }
                )
        );

        findViewById(R.id.rowGender).setOnClickListener(v ->
                showChoiceSheet(
                        "Gender",
                        new String[]{"Male", "Female", "Prefer not to say"},
                        Prefs.getGender(this),
                        choice -> {
                            Prefs.saveGender(this, choice);
                            valueGender.setText(getOrDash(choice));
                        }
                )
        );

        findViewById(R.id.rowRelationship).setOnClickListener(v ->
                showChoiceSheet(
                        "Relationship",
                        new String[]{"Single", "In a relationship", "Married", "It’s complicated", "Prefer not to say"},
                        Prefs.getRelationshipStatus(this),
                        choice -> {
                            Prefs.saveRelationshipStatus(this, choice);
                            valueRelationship.setText(getOrDash(choice));
                        }
                )
        );

        findViewById(R.id.rowTradition).setOnClickListener(v ->
                showChoiceSheet(
                        "Tradition",
                        new String[]{"Catholic", "Protestant", "Mix"},
                        Prefs.getTradition(this),
                        choice -> {
                            Prefs.saveTradition(this, choice);
                            valueTradition.setText(getOrDash(choice));
                        }
                )
        );

        findViewById(R.id.rowPrayer).setOnClickListener(v ->
                showChoiceSheet(
                        "Prayer frequency",
                        new String[]{"Every day", "A few times a week", "Occasionally", "Rarely", "I’m not sure yet"},
                        Prefs.getRecueillementFrequency(this),
                        choice -> {
                            Prefs.saveRecueillementFrequency(this, choice);
                            valuePrayer.setText(getOrDash(choice));
                        }
                )
        );

        findViewById(R.id.rowVersesPerDay).setOnClickListener(v ->
                showChoiceSheet(
                        "Verses per day",
                        new String[]{"1", "2", "3", "4", "5"},
                        String.valueOf(Prefs.getDailyVersesDesired(this)),
                        choice -> {
                            int desired = parseIntOr(choice, 1);
                            applyDailyVersesRulesAndSave(desired);

                            int applied = Prefs.getDailyVersesApplied(this);
                            valueVersesPerDay.setText(desired + " (applied " + applied + ")");

                            if (desired > 1 && !Prefs.isPremium(this)) {
                                Toast.makeText(this,
                                        "Premium unlocks 2–5 verses/day. You'll start with 1 free verse.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                )
        );

        findViewById(R.id.rowTone).setOnClickListener(v ->
                showChoiceSheet(
                        "Tone",
                        new String[]{"SOFT", "BALANCED", "DIRECT"},
                        Prefs.getTone(this),
                        choice -> {
                            Prefs.saveTone(this, choice);
                            valueTone.setText(getOrDash(choice));
                        }
                )
        );

        // ✅ NEW: Goals multi-choice sheet
        findViewById(R.id.rowGoals).setOnClickListener(v -> {
            String[] keys = new String[]{"comfort", "encouragement", "peace", "hope", "anxiety", "guidance"};
            String[] labels = new String[]{"🤍 Comfort", "🔥 Encouragement", "🕊️ Peace", "🌤️ Hope", "🌿 Anxiety relief", "🧭 Guidance"};

            MultiChoiceBottomSheet.newInstance(
                    "What are you looking for?",
                    keys,
                    labels,
                    Prefs.getGoals(this)
            ).setListener(selectedKeys -> {
                Prefs.saveGoals(this, selectedKeys);
                valueGoals.setText(formatGoalsForDisplay(selectedKeys));
            }).show(getSupportFragmentManager(), "goals_sheet");
        });

        findViewById(R.id.rowStreak).setOnClickListener(v ->
                showChoiceSheet(
                        "Streak goal (days)",
                        new String[]{"3", "7", "14", "30"},
                        String.valueOf(Prefs.getStreakGoalDays(this)),
                        choice -> {
                            int days = parseIntOr(choice, Prefs.getStreakGoalDays(this));
                            Prefs.saveStreakGoalDays(this, days);
                            valueStreak.setText(String.valueOf(Prefs.getStreakGoalDays(this)));
                        }
                )
        );
    }

    private String formatGoalsForDisplay(@Nullable Set<String> goals) {
        if (goals == null || goals.isEmpty()) return "—";

        java.util.List<String> out = new java.util.ArrayList<>();
        if (goals.contains("comfort")) out.add("Comfort");
        if (goals.contains("encouragement")) out.add("Encouragement");
        if (goals.contains("peace")) out.add("Peace");
        if (goals.contains("hope")) out.add("Hope");
        if (goals.contains("anxiety")) out.add("Anxiety relief");
        if (goals.contains("guidance")) out.add("Guidance");

        if (out.isEmpty()) out.addAll(goals);

        return android.text.TextUtils.join(", ", out);
    }

    /**
     * Option A (same as onboarding):
     * - Always save DESIRED (1..5)
     * - Applied = DESIRED if premium, else Applied = 1
     */
    private void applyDailyVersesRulesAndSave(int desired) {
        if (desired < 1) desired = 1;
        if (desired > 5) desired = 5;

        Prefs.saveDailyVersesDesired(this, desired);

        if (desired <= 1) {
            Prefs.saveDailyVersesApplied(this, 1);
            return;
        }

        if (Prefs.isPremium(this)) {
            Prefs.saveDailyVersesApplied(this, desired);
        } else {
            Prefs.saveDailyVersesApplied(this, 1);
        }
    }

    private int parseIntOr(String s, int fallback) {
        try {
            if (s == null) return fallback;
            s = s.trim();
            if (s.isEmpty()) return fallback;
            return Integer.parseInt(s);
        } catch (Exception e) {
            return fallback;
        }
    }

    // ---------------------------------------------------
    // CHOICE UI (single-choice, cards style)
    // ---------------------------------------------------

    private void showChoiceSheet(String title, String[] options, String currentValue, OnValueSaved callback) {
        ChoiceBottomSheet.newInstance(title, options, currentValue)
                .setListener(callback::onSaved)
                .show(getSupportFragmentManager(), "choice_sheet");
    }

    // ---------------------------------------------------
    // EDIT DIALOG
    // ---------------------------------------------------

    private void showEditDialog(String title, String currentValue, OnValueSaved callback) {
        showEditDialog(title, currentValue, callback, InputType.TYPE_CLASS_TEXT);
    }

    private void showEditDialog(String title, String currentValue, OnValueSaved callback, int inputType) {
        EditText input = new EditText(this);
        input.setText(currentValue == null ? "" : currentValue);
        input.setInputType(inputType);
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newValue = input.getText().toString().trim();
                    callback.onSaved(newValue);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    interface OnValueSaved {
        void onSaved(String value);
    }

    // ---------------------------------------------------
    // BOTTOM NAV
    // ---------------------------------------------------

    private void setupBottomNav() {
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            if (ignoreNavSelection) return true;

            int id = item.getItemId();
            animateNavItem(bottomNav, id);

            if (id == R.id.nav_profile) {
                return true;
            } else if (id == R.id.nav_feed) {
                goTo(FeedActivity.class);
                return true;
            } else if (id == R.id.nav_widget) {
                goTo(widgetcustom.class);
                return true;
            }
            return false;
        });
    }

    private void goTo(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void animateNavItem(BottomNavigationView nav, int itemId) {
        View v = nav.findViewById(itemId);
        if (v == null) return;

        v.animate().cancel();
        v.setScaleX(1f);
        v.setScaleY(1f);

        v.animate()
                .scaleX(1.12f)
                .scaleY(1.12f)
                .setDuration(120)
                .withEndAction(() ->
                        v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                )
                .start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            ignoreNavSelection = true;
            bottomNav.setSelectedItemId(R.id.nav_profile);
            ignoreNavSelection = false;
        }
    }
}