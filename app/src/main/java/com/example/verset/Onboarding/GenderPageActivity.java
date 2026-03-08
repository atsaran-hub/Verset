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

public class GenderPageActivity extends AppCompatActivity {

    // Cards
    private MaterialCardView optionMale, optionFemale, optionPrefer;

    // Check icons
    private ImageView checkMale, checkFemale, checkPrefer;

    // CTA
    private MaterialButton buttonContinue;

    // Current selection
    private String selectedGender = null;

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
        setContentView(R.layout.gender);

        // Optional ActionBar back
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

        // Back button in XML
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> goBack());

        // CTA
        buttonContinue = findViewById(R.id.buttonContinue);
        if (buttonContinue != null) {
            buttonContinue.setEnabled(false);
            buttonContinue.setOnClickListener(v -> {
                if (selectedGender != null) {
                    goNext();
                }
            });
        }

        // Find option cards
        optionMale = findViewById(R.id.optionMale);
        optionFemale = findViewById(R.id.optionFemale);
        optionPrefer = findViewById(R.id.optionPrefer);

        // Find check icons
        checkMale = findViewById(R.id.checkMale);
        checkFemale = findViewById(R.id.checkFemale);
        checkPrefer = findViewById(R.id.checkPrefer);

        // Resolve colors safely (fallback if missing)
        borderSoft = getColorIdSafe("card_border_soft", 0xFFECE6DA);
        selectedBorder = getColorIdSafe("choice_selected_border", 0xFF6F8FCF);
        selectedBg = getColorIdSafe("choice_selected_bg", 0xFFEEF2FA);
        cardBg = getColorIdSafe("card_background", 0xFFFFFFFF);
        iconUnselected = getColorIdSafe("choice_unselected_icon", 0xFFC9D3E6);
        iconSelected = getColorIdSafe("accent_primary", 0xFF6F8FCF);

        // Click listeners
        if (optionMale != null) optionMale.setOnClickListener(v -> selectGender("Male"));
        if (optionFemale != null) optionFemale.setOnClickListener(v -> selectGender("Female"));
        if (optionPrefer != null) optionPrefer.setOnClickListener(v -> selectGender("Prefer not to say"));

        // Restore selection from Prefs
        restoreSelection();
    }

    private void selectGender(String value) {
        selectedGender = value;

        // Save in Prefs
        Prefs.saveGender(this, value);

        // Update UI
        applySelectionUI(value);

        // Enable CTA
        if (buttonContinue != null) buttonContinue.setEnabled(true);
    }

    private void restoreSelection() {
        String saved = Prefs.getGender(this);
        if (saved != null && !saved.isEmpty()) {
            selectedGender = saved;
            applySelectionUI(saved);
            if (buttonContinue != null) buttonContinue.setEnabled(true);
        } else {
            clearSelectionUI();
            if (buttonContinue != null) buttonContinue.setEnabled(false);
        }
    }

    private void clearSelectionUI() {
        setCardUnselected(optionMale, checkMale);
        setCardUnselected(optionFemale, checkFemale);
        setCardUnselected(optionPrefer, checkPrefer);
    }

    private void applySelectionUI(String value) {
        clearSelectionUI();

        switch (value) {
            case "Male":
                setCardSelected(optionMale, checkMale);
                break;
            case "Female":
                setCardSelected(optionFemale, checkFemale);
                break;
            case "Prefer not to say":
                setCardSelected(optionPrefer, checkPrefer);
                break;
        }
    }

    private void setCardSelected(MaterialCardView card, ImageView check) {
        if (card != null) {
            card.setStrokeWidth(dpToPx(1));
            card.setStrokeColor(selectedBorder);
            card.setCardBackgroundColor(selectedBg);
        }
        if (check != null) {
            check.setColorFilter(iconSelected);
        }
    }

    private void setCardUnselected(MaterialCardView card, ImageView check) {
        if (card != null) {
            card.setStrokeWidth(dpToPx(1));
            card.setStrokeColor(borderSoft);
            card.setCardBackgroundColor(cardBg);
        }
        if (check != null) {
            check.setColorFilter(iconUnselected);
        }
    }

    private void goNext() {
        Intent intent = new Intent(GenderPageActivity.this, RelationshipPageActivity.class);
        startActivity(intent);
        runTransitionForward();
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
        if (enter != 0 && exit != 0) {
            overridePendingTransition(enter, exit);
        }
    }

    private void runTransitionBack() {
        int enter = getAnimId("slide_in_left");
        int exit = getAnimId("slide_out_right");
        if (enter != 0 && exit != 0) {
            overridePendingTransition(enter, exit);
        }
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
