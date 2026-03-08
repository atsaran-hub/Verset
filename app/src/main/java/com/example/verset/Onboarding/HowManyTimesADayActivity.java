package com.example.verset.Onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.verset.NotificationTimeActivity;
import com.example.verset.Prefs;
import com.example.verset.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class HowManyTimesADayActivity extends AppCompatActivity {

    // Cards
    private MaterialCardView option1, option2, option3, option4, option5;

    // Check icons
    private ImageView check1, check2, check3, check4, check5;

    // CTA
    private MaterialButton buttonContinue;

    // what user WANTS (can be premium)
    private Integer selectedCount = null;

    // Colors
    private int borderSoft;
    private int selectedBorder;
    private int selectedBg;
    private int cardBg;
    private int iconUnselected;
    private int iconSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ⚠️ Mets ici le nom exact de TON layout
        // setContentView(R.layout.daily_verses_plan_page);
        setContentView(R.layout.howmanytimesaday);

        // Optional ActionBar back
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

        // Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> goBack());

        // CTA
        buttonContinue = findViewById(R.id.buttonContinue);
        if (buttonContinue != null) {
            buttonContinue.setEnabled(false);
            buttonContinue.setOnClickListener(v -> {
                if (selectedCount == null) {
                    Toast.makeText(this, "Please choose an option", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Option A rules again (safe)
                applyOptionARulesAndSave(selectedCount);

                // 👉 Page suivante (change si besoin)
                startActivity(new Intent(HowManyTimesADayActivity.this, StreakGoalActivity.class));
                runTransitionForward();
            });
        }

        // Cards
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        option5 = findViewById(R.id.option5);

        // Checks
        check1 = findViewById(R.id.check1);
        check2 = findViewById(R.id.check2);
        check3 = findViewById(R.id.check3);
        check4 = findViewById(R.id.check4);
        check5 = findViewById(R.id.check5);

        // Resolve colors safely (fallback if missing)
        borderSoft = getColorIdSafe("card_border_soft", 0xFFECE6DA);
        selectedBorder = getColorIdSafe("choice_selected_border", 0xFF6F8FCF);
        selectedBg = getColorIdSafe("choice_selected_bg", 0xFFEEF2FA);
        cardBg = getColorIdSafe("card_background", 0xFFFFFFFF);
        iconUnselected = getColorIdSafe("choice_unselected_icon", 0xFFC9D3E6);
        iconSelected = getColorIdSafe("accent_primary", 0xFF6F8FCF);

        // Click listeners
        if (option1 != null) option1.setOnClickListener(v -> selectCount(1));
        if (option2 != null) option2.setOnClickListener(v -> selectCount(2));
        if (option3 != null) option3.setOnClickListener(v -> selectCount(3));
        if (option4 != null) option4.setOnClickListener(v -> selectCount(4));
        if (option5 != null) option5.setOnClickListener(v -> selectCount(5));

        // Restore desired choice
        restoreSelection();
    }

    private void selectCount(int desiredCount) {
        selectedCount = desiredCount;

        // Save desired + apply allowed
        applyOptionARulesAndSave(desiredCount);

        // UI shows what user selected (even premium)
        applyUI(desiredCount);

        // Enable CTA
        if (buttonContinue != null) buttonContinue.setEnabled(true);

        // Optional hint for premium selections
        if (desiredCount > 1 && !Prefs.isPremium(this)) {
            Toast.makeText(this,
                    "Premium unlocks 2–5 verses/day. You'll start with 1 free verse.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Option A:
     * - Always save DESIRED (1..5)
     * - Applied = DESIRED if premium, else Applied = 1
     */
    private void applyOptionARulesAndSave(int desiredCount) {
        Prefs.saveDailyVersesDesired(this, desiredCount);

        if (desiredCount <= 1) {
            Prefs.saveDailyVersesApplied(this, 1);
            return;
        }

        if (Prefs.isPremium(this)) {
            Prefs.saveDailyVersesApplied(this, desiredCount);
        } else {
            Prefs.saveDailyVersesApplied(this, 1);
        }
    }

    private void restoreSelection() {
        int desired = Prefs.getDailyVersesDesired(this);
        if (desired < 1 || desired > 5) desired = 1;

        selectedCount = desired;
        applyUI(desired);

        if (buttonContinue != null) buttonContinue.setEnabled(true);
    }

    private void clearUI() {
        setUnselected(option1, check1);
        setUnselected(option2, check2);
        setUnselected(option3, check3);
        setUnselected(option4, check4);
        setUnselected(option5, check5);
    }

    private void applyUI(int desiredCount) {
        clearUI();
        switch (desiredCount) {
            case 1: setSelected(option1, check1); break;
            case 2: setSelected(option2, check2); break;
            case 3: setSelected(option3, check3); break;
            case 4: setSelected(option4, check4); break;
            case 5: setSelected(option5, check5); break;
        }
    }

    private void setSelected(MaterialCardView card, ImageView check) {
        if (card != null) {
            card.setStrokeWidth(dpToPx(1));
            card.setStrokeColor(selectedBorder);
            card.setCardBackgroundColor(selectedBg);
        }
        if (check != null) {
            check.setColorFilter(iconSelected);
        }
    }

    private void setUnselected(MaterialCardView card, ImageView check) {
        if (card != null) {
            card.setStrokeWidth(dpToPx(1));
            card.setStrokeColor(borderSoft);
            card.setCardBackgroundColor(cardBg);
        }
        if (check != null) {
            check.setColorFilter(iconUnselected);
        }
    }

    private void goBack() {
        finish();
        runTransitionBack();
    }

    // ActionBar back arrow
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

    // Transitions SAFE
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
