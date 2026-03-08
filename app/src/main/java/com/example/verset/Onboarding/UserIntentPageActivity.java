package com.example.verset.Onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.verset.Prefs;
import com.example.verset.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.HashSet;
import java.util.Set;

public class UserIntentPageActivity extends AppCompatActivity {

    private MaterialButton buttonContinue;

    private MaterialCardView optionComfort, optionEncouragement, optionPeace, optionHope, optionAnxiety, optionGuidance;
    private ImageView checkComfort, checkEncouragement, checkPeace, checkHope, checkAnxiety, checkGuidance;

    private final Set<String> selectedGoals = new HashSet<>();

    private int borderSoft, selectedBorder, selectedBg, cardBg, iconUnselected, iconSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_user_intent);

        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> goBack());

        buttonContinue = findViewById(R.id.buttonContinue);
        if (buttonContinue != null) {
            buttonContinue.setEnabled(false);
            buttonContinue.setOnClickListener(v -> goNext());
        }

        // Bind cards
        optionComfort = findViewById(R.id.optionComfort);
        optionEncouragement = findViewById(R.id.optionEncouragement);
        optionPeace = findViewById(R.id.optionPeace);
        optionHope = findViewById(R.id.optionHope);
        optionAnxiety = findViewById(R.id.optionAnxiety);
        optionGuidance = findViewById(R.id.optionGuidance);

        // Bind checks
        checkComfort = findViewById(R.id.checkComfort);
        checkEncouragement = findViewById(R.id.checkEncouragement);
        checkPeace = findViewById(R.id.checkPeace);
        checkHope = findViewById(R.id.checkHope);
        checkAnxiety = findViewById(R.id.checkAnxiety);
        checkGuidance = findViewById(R.id.checkGuidance);

        // Same colors logic as other onboarding pages
        borderSoft = getColorIdSafe("card_border_soft", 0xFFECE6DA);
        selectedBorder = getColorIdSafe("choice_selected_border", 0xFF6F8FCF);
        selectedBg = getColorIdSafe("choice_selected_bg", 0xFFEEF2FA);
        cardBg = getColorIdSafe("card_background", 0xFFFFFFFF);
        iconUnselected = getColorIdSafe("choice_unselected_icon", 0xFFC9D3E6);
        iconSelected = getColorIdSafe("accent_primary", 0xFF6F8FCF);

        // Clicks (multi-select)
        if (optionComfort != null) optionComfort.setOnClickListener(v -> toggle("comfort"));
        if (optionEncouragement != null) optionEncouragement.setOnClickListener(v -> toggle("encouragement"));
        if (optionPeace != null) optionPeace.setOnClickListener(v -> toggle("peace"));
        if (optionHope != null) optionHope.setOnClickListener(v -> toggle("hope"));
        if (optionAnxiety != null) optionAnxiety.setOnClickListener(v -> toggle("anxiety"));
        if (optionGuidance != null) optionGuidance.setOnClickListener(v -> toggle("guidance"));

        restoreSelection();
    }

    private void toggle(String key) {
        if (selectedGoals.contains(key)) selectedGoals.remove(key);
        else selectedGoals.add(key);

        // Save immediately (comme onboarding)
        Prefs.saveGoals(this, selectedGoals);

        applyUI();

        if (buttonContinue != null) buttonContinue.setEnabled(!selectedGoals.isEmpty());
    }

    private void restoreSelection() {
        selectedGoals.clear();
        selectedGoals.addAll(Prefs.getGoals(this)); // already exists in Prefs

        applyUI();

        if (buttonContinue != null) buttonContinue.setEnabled(!selectedGoals.isEmpty());
    }

    private void applyUI() {
        applyCard(optionComfort, checkComfort, selectedGoals.contains("comfort"));
        applyCard(optionEncouragement, checkEncouragement, selectedGoals.contains("encouragement"));
        applyCard(optionPeace, checkPeace, selectedGoals.contains("peace"));
        applyCard(optionHope, checkHope, selectedGoals.contains("hope"));
        applyCard(optionAnxiety, checkAnxiety, selectedGoals.contains("anxiety"));
        applyCard(optionGuidance, checkGuidance, selectedGoals.contains("guidance"));
    }

    private void applyCard(MaterialCardView card, ImageView check, boolean selected) {
        if (card == null) return;

        card.setStrokeWidth(dpToPx(1));
        if (selected) {
            card.setStrokeColor(selectedBorder);
            card.setCardBackgroundColor(selectedBg);
            if (check != null) check.setColorFilter(iconSelected);
        } else {
            card.setStrokeColor(borderSoft);
            card.setCardBackgroundColor(cardBg);
            if (check != null) check.setColorFilter(iconUnselected);
        }
    }

    private void goNext() {
        // Après cette page → Summary
        startActivity(new Intent(this, OnboardingSummaryActivity.class));
        runTransitionForward();
    }

    private void goBack() {
        finish();
        runTransitionBack();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goBack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    private void runTransitionForward() {
        int enter = getAnimId("slide_in_right");
        int exit = getAnimId("slide_out_left");
        if (enter != 0 && exit != 0) overridePendingTransition(enter, exit);
    }

    private void runTransitionBack() {
        int enter = getAnimId("slide_in_left");
        int exit = getAnimId("slide_out_right");
        if (enter != 0 && exit != 0) overridePendingTransition(enter, exit);
    }

    private int getAnimId(String name) {
        return getResources().getIdentifier(name, "anim", getPackageName());
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int getColorIdSafe(String colorName, int fallback) {
        int id = getResources().getIdentifier(colorName, "color", getPackageName());
        if (id == 0) return fallback;
        return getColor(id);
    }
}