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

public class ActivityTone extends AppCompatActivity {

    private MaterialCardView optionSoft, optionBalanced, optionDirect;
    private ImageView checkSoft, checkBalanced, checkDirect;
    private MaterialButton buttonContinue;

    private String selected = null;

    private int borderSoft, selectedBorder, selectedBg, cardBg, iconUnselected, iconSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tone);

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

        optionSoft = findViewById(R.id.optionSoft);
        optionBalanced = findViewById(R.id.optionBalanced);
        optionDirect = findViewById(R.id.optionDirect);

        checkSoft = findViewById(R.id.checkSoft);
        checkBalanced = findViewById(R.id.checkBalanced);
        checkDirect = findViewById(R.id.checkDirect);

        borderSoft = getColorIdSafe("card_border_soft", 0xFFECE6DA);
        selectedBorder = getColorIdSafe("choice_selected_border", 0xFF6F8FCF);
        selectedBg = getColorIdSafe("choice_selected_bg", 0xFFEEF2FA);
        cardBg = getColorIdSafe("card_background", 0xFFFFFFFF);
        iconUnselected = getColorIdSafe("choice_unselected_icon", 0xFFC9D3E6);
        iconSelected = getColorIdSafe("accent_primary", 0xFF6F8FCF);

        if (optionSoft != null) optionSoft.setOnClickListener(v -> select("SOFT"));
        if (optionBalanced != null) optionBalanced.setOnClickListener(v -> select("BALANCED"));
        if (optionDirect != null) optionDirect.setOnClickListener(v -> select("DIRECT"));

        restoreSelection();
    }

    private void select(String value) {
        selected = value;
        Prefs.saveTone(this, value);
        applyUI(value);
        if (buttonContinue != null) buttonContinue.setEnabled(true);
    }

    private void restoreSelection() {
        String saved = Prefs.getTone(this);
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
        setUnselected(optionSoft, checkSoft);
        setUnselected(optionBalanced, checkBalanced);
        setUnselected(optionDirect, checkDirect);
    }

    private void applyUI(String value) {
        clearUI();
        switch (value) {
            case "SOFT":
                setSelected(optionSoft, checkSoft);
                break;
            case "BALANCED":
                setSelected(optionBalanced, checkBalanced);
                break;
            case "DIRECT":
                setSelected(optionDirect, checkDirect);
                break;
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
        Intent intent = new Intent(this, UserIntentPageActivity.class);
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
