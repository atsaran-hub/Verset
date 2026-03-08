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

public class AgePageActivity extends AppCompatActivity {

    // Cards (options)
    private MaterialCardView option18_24, option25_34, option35_44, option45_54, option55plus;

    // Check icons
    private ImageView check18_24, check25_34, check35_44, check45_54, check55plus;

    // CTA
    private MaterialButton buttonContinue;

    // Current selection
    private String selectedAge = null;

    // Colors (resolved at runtime)
    private int colorBorderSoft;
    private int colorSelectedBorder;
    private int colorSelectedBg;
    private int colorCardBg;
    private int colorIconUnselected;
    private int colorIconSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agepage);

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
                if (selectedAge != null) {
                    goNext(selectedAge);
                }
            });
        }

        // Find option cards
        option18_24 = findViewById(R.id.option18_24);
        option25_34 = findViewById(R.id.option25_34);
        option35_44 = findViewById(R.id.option35_44);
        option45_54 = findViewById(R.id.option45_54);
        option55plus = findViewById(R.id.option55plus);

        // Find check icons
        check18_24 = findViewById(R.id.check18_24);
        check25_34 = findViewById(R.id.check25_34);
        check35_44 = findViewById(R.id.check35_44);
        check45_54 = findViewById(R.id.check45_54);
        check55plus = findViewById(R.id.check55plus);

        // Resolve colors safely (fallback if missing)
        colorBorderSoft = getColorIdSafe("card_border_soft", 0xFFECE6DA);
        colorSelectedBorder = getColorIdSafe("choice_selected_border", 0xFF6F8FCF);
        colorSelectedBg = getColorIdSafe("choice_selected_bg", 0xFFEEF2FA);
        colorCardBg = getColorIdSafe("card_background", 0xFFFFFFFF);
        colorIconUnselected = getColorIdSafe("choice_unselected_icon", 0xFFC9D3E6);
        colorIconSelected = getColorIdSafe("accent_primary", 0xFF6F8FCF);

        // Click listeners
        if (option18_24 != null) option18_24.setOnClickListener(v -> selectAge("18–24"));
        if (option25_34 != null) option25_34.setOnClickListener(v -> selectAge("25–34"));
        if (option35_44 != null) option35_44.setOnClickListener(v -> selectAge("35–44"));
        if (option45_54 != null) option45_54.setOnClickListener(v -> selectAge("45–54"));
        if (option55plus != null) option55plus.setOnClickListener(v -> selectAge("55+"));

        // Restore selection from Prefs
        restoreSelection();
    }

    private void selectAge(String ageRange) {
        selectedAge = ageRange;

        // Save immediately (like your old flow)
        Prefs.saveAge(this, ageRange);

        // Update UI
        applySelectionUI(ageRange);

        // Enable CTA
        if (buttonContinue != null) buttonContinue.setEnabled(true);

        // If you want instant navigation (old behavior), uncomment:
        // goNext(ageRange);
    }

    private void goNext(String ageRange) {
        Intent intent = new Intent(AgePageActivity.this, GenderPageActivity.class);
        startActivity(intent);
        runTransitionForward();
    }

    private void restoreSelection() {
        String saved = Prefs.getAge(this);
        if (saved != null && !saved.isEmpty()) {
            selectedAge = saved;
            applySelectionUI(saved);
            if (buttonContinue != null) buttonContinue.setEnabled(true);
        } else {
            clearSelectionUI();
            if (buttonContinue != null) buttonContinue.setEnabled(false);
        }
    }

    private void clearSelectionUI() {
        setCardUnselected(option18_24, check18_24);
        setCardUnselected(option25_34, check25_34);
        setCardUnselected(option35_44, check35_44);
        setCardUnselected(option45_54, check45_54);
        setCardUnselected(option55plus, check55plus);
    }

    private void applySelectionUI(String ageRange) {
        // reset all first
        clearSelectionUI();

        // set selected card
        switch (ageRange) {
            case "18–24":
                setCardSelected(option18_24, check18_24);
                break;
            case "25–34":
                setCardSelected(option25_34, check25_34);
                break;
            case "35–44":
                setCardSelected(option35_44, check35_44);
                break;
            case "45–54":
                setCardSelected(option45_54, check45_54);
                break;
            case "55+":
                setCardSelected(option55plus, check55plus);
                break;
        }
    }

    private void setCardSelected(MaterialCardView card, ImageView check) {
        if (card != null) {
            card.setStrokeWidth(dpToPx(1));
            card.setStrokeColor(colorSelectedBorder);
            card.setCardBackgroundColor(colorSelectedBg);
        }
        if (check != null) {
            check.setImageResource(R.drawable.ic_check_circle);
            check.setColorFilter(colorIconSelected);
        }
    }

    private void setCardUnselected(MaterialCardView card, ImageView check) {
        if (card != null) {
            card.setStrokeWidth(dpToPx(1));
            card.setStrokeColor(colorBorderSoft);
            card.setCardBackgroundColor(colorCardBg);
        }
        if (check != null) {
            check.setImageResource(R.drawable.ic_check_circle);
            check.setColorFilter(colorIconUnselected);
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

    // Safe color resolver (works even if a color name is missing)
    private int getColorIdSafe(String colorName, int fallback) {
        int id = getResources().getIdentifier(colorName, "color", getPackageName());
        if (id == 0) return fallback;
        return getColor(id);
    }
}
