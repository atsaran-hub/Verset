package com.example.verset.Onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.example.verset.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.verset.Prefs;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class StreakGoalActivity extends AppCompatActivity {

    private MaterialCardView option3, option7, option14, option30;
    private ImageView check3, check7, check14, check30;
    private MaterialButton buttonContinue;

    private Integer selected = null;

    private int borderSoft, selectedBorder, selectedBg, cardBg, iconUnselected, iconSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regularity_page);

        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> goBack());

        buttonContinue = findViewById(R.id.buttonContinue);
        if (buttonContinue != null) {
            buttonContinue.setEnabled(false);
            buttonContinue.setOnClickListener(v -> {
                if (selected != null) goNext();
            });
        }

        option3 = findViewById(R.id.option3);
        option7 = findViewById(R.id.option7);
        option14 = findViewById(R.id.option14);
        option30 = findViewById(R.id.option30);

        check3 = findViewById(R.id.check3);
        check7 = findViewById(R.id.check7);
        check14 = findViewById(R.id.check14);
        check30 = findViewById(R.id.check30);

        borderSoft = getColorIdSafe("card_border_soft", 0xFFECE6DA);
        selectedBorder = getColorIdSafe("choice_selected_border", 0xFF6F8FCF);
        selectedBg = getColorIdSafe("choice_selected_bg", 0xFFEEF2FA);
        cardBg = getColorIdSafe("card_background", 0xFFFFFFFF);
        iconUnselected = getColorIdSafe("choice_unselected_icon", 0xFFC9D3E6);
        iconSelected = getColorIdSafe("accent_primary", 0xFF6F8FCF);

        if (option3 != null) option3.setOnClickListener(v -> select(3));
        if (option7 != null) option7.setOnClickListener(v -> select(7));
        if (option14 != null) option14.setOnClickListener(v -> select(14));
        if (option30 != null) option30.setOnClickListener(v -> select(30));

        restoreSelection();
    }

    private void select(int days) {
        selected = days;

        Prefs.saveStreakGoalDays(this, days);

        applyUI(days);

        if (buttonContinue != null) buttonContinue.setEnabled(true);
    }

    private void restoreSelection() {
        int saved = Prefs.getStreakGoalDays(this);
        if (saved <= 0) saved = 7; // default nice goal
        selected = saved;

        applyUI(saved);
        if (buttonContinue != null) buttonContinue.setEnabled(true);
    }

    private void clearUI() {
        setUnselected(option3, check3);
        setUnselected(option7, check7);
        setUnselected(option14, check14);
        setUnselected(option30, check30);
    }

    private void applyUI(int days) {
        clearUI();
        switch (days) {
            case 3: setSelected(option3, check3); break;
            case 7: setSelected(option7, check7); break;
            case 14: setSelected(option14, check14); break;
            case 30: setSelected(option30, check30); break;
        }
    }

    private void setSelected(MaterialCardView card, ImageView check) {
        if (card != null) {
            card.setStrokeWidth(dpToPx(1));
            card.setStrokeColor(selectedBorder);
            card.setCardBackgroundColor(selectedBg);
        }
        if (check != null) check.setColorFilter(iconSelected);
    }

    private void setUnselected(MaterialCardView card, ImageView check) {
        if (card != null) {
            card.setStrokeWidth(dpToPx(1));
            card.setStrokeColor(borderSoft);
            card.setCardBackgroundColor(cardBg);
        }
        if (check != null) check.setColorFilter(iconUnselected);
    }

    private void goNext() {
        // ✅ change the next screen if you want
        startActivity(new Intent(StreakGoalActivity.this, ActivityTone.class));
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
