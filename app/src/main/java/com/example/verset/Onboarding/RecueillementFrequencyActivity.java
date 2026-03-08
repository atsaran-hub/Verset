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

public class RecueillementFrequencyActivity extends AppCompatActivity {

    private MaterialCardView optionDaily, optionFewTimes, optionOccasionally, optionRarely, optionNotSure;
    private ImageView checkDaily, checkFewTimes, checkOccasionally, checkRarely, checkNotSure;
    private MaterialButton buttonContinue;

    private String selected = null;

    private int borderSoft;
    private int selectedBorder;
    private int selectedBg;
    private int cardBg;
    private int iconUnselected;
    private int iconSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recueillement_frequency_page);

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

        optionDaily = findViewById(R.id.optionDaily);
        optionFewTimes = findViewById(R.id.optionFewTimes);
        optionOccasionally = findViewById(R.id.optionOccasionally);
        optionRarely = findViewById(R.id.optionRarely);
        optionNotSure = findViewById(R.id.optionNotSure);

        checkDaily = findViewById(R.id.checkDaily);
        checkFewTimes = findViewById(R.id.checkFewTimes);
        checkOccasionally = findViewById(R.id.checkOccasionally);
        checkRarely = findViewById(R.id.checkRarely);
        checkNotSure = findViewById(R.id.checkNotSure);

        borderSoft = getColorIdSafe("card_border_soft", 0xFFECE6DA);
        selectedBorder = getColorIdSafe("choice_selected_border", 0xFF6F8FCF);
        selectedBg = getColorIdSafe("choice_selected_bg", 0xFFEEF2FA);
        cardBg = getColorIdSafe("card_background", 0xFFFFFFFF);
        iconUnselected = getColorIdSafe("choice_unselected_icon", 0xFFC9D3E6);
        iconSelected = getColorIdSafe("accent_primary", 0xFF6F8FCF);

        if (optionDaily != null) optionDaily.setOnClickListener(v -> select("Every day"));
        if (optionFewTimes != null) optionFewTimes.setOnClickListener(v -> select("A few times a week"));
        if (optionOccasionally != null) optionOccasionally.setOnClickListener(v -> select("Occasionally"));
        if (optionRarely != null) optionRarely.setOnClickListener(v -> select("Rarely"));
        if (optionNotSure != null) optionNotSure.setOnClickListener(v -> select("I’m not sure yet"));

        restoreSelection();
    }

    private void select(String value) {
        selected = value;

        Prefs.saveRecueillementFrequency(this, value);

        applyUI(value);

        if (buttonContinue != null) buttonContinue.setEnabled(true);
    }

    private void restoreSelection() {
        String saved = Prefs.getRecueillementFrequency(this);
        if (saved != null && !saved.isEmpty()) {
            selected = saved;
            applyUI(saved);
            if (buttonContinue != null) buttonContinue.setEnabled(true);
        } else {
            clearUI();
            if (buttonContinue != null) buttonContinue.setEnabled(false);
        }
    }

    private void clearUI() {
        setUnselected(optionDaily, checkDaily);
        setUnselected(optionFewTimes, checkFewTimes);
        setUnselected(optionOccasionally, checkOccasionally);
        setUnselected(optionRarely, checkRarely);
        setUnselected(optionNotSure, checkNotSure);
    }

    private void applyUI(String value) {
        clearUI();
        switch (value) {
            case "Every day":
                setSelected(optionDaily, checkDaily); break;
            case "A few times a week":
                setSelected(optionFewTimes, checkFewTimes); break;
            case "Occasionally":
                setSelected(optionOccasionally, checkOccasionally); break;
            case "Rarely":
                setSelected(optionRarely, checkRarely); break;
            case "I’m not sure yet":
                setSelected(optionNotSure, checkNotSure); break;
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
        // ✅ NEXT -> HowManyTimesADayActivity
        Intent intent = new Intent(RecueillementFrequencyActivity.this, TraditionPageActivity.class);
        startActivity(intent);
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
