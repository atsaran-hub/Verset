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

public class RelationshipPageActivity extends AppCompatActivity {

    // Cards
    private MaterialCardView optionSingle;
    private MaterialCardView optionRelationship;
    private MaterialCardView optionMarried;
    private MaterialCardView optionComplicated;
    private MaterialCardView optionPreferNot;

    // Check icons
    private ImageView checkSingle;
    private ImageView checkRelationship;
    private ImageView checkMarried;
    private ImageView checkComplicated;
    private ImageView checkPreferNot;

    // CTA
    private MaterialButton buttonContinue;

    // Current selection
    private String selected = null;

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
        setContentView(R.layout.relationshippage);

        // ActionBar back
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> goBack());

        // Continue button
        buttonContinue = findViewById(R.id.buttonContinue);
        if (buttonContinue != null) {
            buttonContinue.setEnabled(false);
            buttonContinue.setOnClickListener(v -> {
                if (selected != null) goNext();
            });
        }

        // Cards
        optionSingle = findViewById(R.id.optionSingle);
        optionRelationship = findViewById(R.id.optionRelationship);
        optionMarried = findViewById(R.id.optionMarried);
        optionComplicated = findViewById(R.id.optionComplicated);
        optionPreferNot = findViewById(R.id.optionPreferNot);

        // Check icons
        checkSingle = findViewById(R.id.checkSingle);
        checkRelationship = findViewById(R.id.checkRelationship);
        checkMarried = findViewById(R.id.checkMarried);
        checkComplicated = findViewById(R.id.checkComplicated);
        checkPreferNot = findViewById(R.id.checkPreferNot);

        // Resolve colors safely
        borderSoft = getColorIdSafe("card_border_soft", 0xFFECE6DA);
        selectedBorder = getColorIdSafe("choice_selected_border", 0xFF6F8FCF);
        selectedBg = getColorIdSafe("choice_selected_bg", 0xFFEEF2FA);
        cardBg = getColorIdSafe("card_background", 0xFFFFFFFF);
        iconUnselected = 0xFFC9D3E6;
        iconSelected = getColorIdSafe("accent_primary", 0xFF6F8FCF);

        // Click listeners
        if (optionSingle != null) {
            optionSingle.setOnClickListener(v -> select("Single"));
        }

        if (optionRelationship != null) {
            optionRelationship.setOnClickListener(v -> select("In a relationship"));
        }

        if (optionMarried != null) {
            optionMarried.setOnClickListener(v -> select("Married"));
        }

        if (optionComplicated != null) {
            optionComplicated.setOnClickListener(v -> select("It’s complicated"));
        }

        if (optionPreferNot != null) {
            optionPreferNot.setOnClickListener(v -> select("Prefer not to say"));
        }

        // Restore selection
        restoreSelection();
    }

    private void select(String value) {
        selected = value;

        // Save in Prefs
        Prefs.saveRelationshipStatus(this, value);

        // Update UI
        applyUI(value);

        // Enable CTA
        if (buttonContinue != null) {
            buttonContinue.setEnabled(true);
        }
    }

    private void restoreSelection() {
        String saved = Prefs.getRelationshipStatus(this);
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
        setUnselected(optionSingle, checkSingle);
        setUnselected(optionRelationship, checkRelationship);
        setUnselected(optionMarried, checkMarried);
        setUnselected(optionComplicated, checkComplicated);
        setUnselected(optionPreferNot, checkPreferNot);
    }

    private void applyUI(String value) {
        clearUI();

        switch (value) {
            case "Single":
                setSelected(optionSingle, checkSingle);
                break;

            case "In a relationship":
                setSelected(optionRelationship, checkRelationship);
                break;

            case "Married":
                setSelected(optionMarried, checkMarried);
                break;

            case "It’s complicated":
                setSelected(optionComplicated, checkComplicated);
                break;

            case "Prefer not to say":
                setSelected(optionPreferNot, checkPreferNot);
                break;
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

    private void goNext() {
        Intent intent = new Intent(RelationshipPageActivity.this, GodRelationshipIntroActivity.class);
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

    // Transitions
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
