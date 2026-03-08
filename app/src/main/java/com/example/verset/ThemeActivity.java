package com.example.verset;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.verset.Onboarding.ActivityTone;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ThemeActivity extends AppCompatActivity {

    // Appearance buttons/cards
    private MaterialCardView optionLight, optionDark;

    // Theme cards
    private MaterialCardView themeCardDawn, themeCardSea, themeCardMirror;

    // Selection overlays (rings)
    private View themeSelDawn, themeSelSea, themeSelMirror;

    private MaterialButton buttonSave;

    private String selectedThemeId = null;
    private String selectedAppearance = null; // "light" / "dark" / "system"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ✅ applique le mode avant d'afficher la page (important)
        applySavedAppearanceMode();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        // Appearance
        optionLight = findViewById(R.id.optionLight);
        optionDark  = findViewById(R.id.optionDark);

        // Theme cards
        themeCardDawn   = findViewById(R.id.themeCardDawn);
        themeCardSea    = findViewById(R.id.themeCardSea);
        themeCardMirror = findViewById(R.id.themeCardMirror);

        // Selection overlays
        themeSelDawn   = findViewById(R.id.themeSelDawn);
        themeSelSea    = findViewById(R.id.themeSelSea);
        themeSelMirror = findViewById(R.id.themeSelMirror);

        // CTA
        buttonSave = findViewById(R.id.buttonSave);

        // ✅ restore Prefs
        selectedAppearance = Prefs.getAppearanceMode(this); // "light"/"dark"/"system"
        selectedThemeId = Prefs.getThemeId(this);           // "dawn"/...

        // UI refresh
        refreshAppearanceUI();
        refreshSelectionUI();

        // -------------------------
        // Click: Light / Dark
        // -------------------------
        optionLight.setOnClickListener(v -> setAppearance("light"));
        optionDark.setOnClickListener(v -> setAppearance("dark"));

        // -------------------------
        // Click: Themes (save direct)
        // -------------------------
        themeCardDawn.setOnClickListener(v -> selectTheme("dawn"));
        themeCardSea.setOnClickListener(v -> selectTheme("sea"));
        themeCardMirror.setOnClickListener(v -> selectTheme("mirror"));

        // -------------------------
        // Continue
        // -------------------------
        buttonSave.setOnClickListener(v -> {
            if (selectedThemeId == null || selectedThemeId.trim().isEmpty()) {
                Toast.makeText(this, "Please select a theme", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(ThemeActivity.this, ActivityTone.class));
        });
    }

    // =====================================================
    // Appearance (Light/Dark) : save + apply instant
    // =====================================================
    private void setAppearance(String mode) {
        selectedAppearance = mode;

        // ✅ save direct
        Prefs.setAppearanceMode(this, mode);

        // ✅ apply + recreate to refresh colors immediately
        if ("light".equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if ("dark".equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        refreshAppearanceUI();
        recreate();
    }

    private void applySavedAppearanceMode() {
        String mode = Prefs.getAppearanceMode(this);

        if ("light".equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if ("dark".equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    private void refreshAppearanceUI() {
        // petit feedback visuel simple (alpha)
        if (optionLight != null) optionLight.setAlpha("light".equals(selectedAppearance) ? 1f : 0.55f);
        if (optionDark != null)  optionDark.setAlpha("dark".equals(selectedAppearance) ? 1f : 0.55f);
    }

    // =====================================================
    // Themes : save direct (Prefs.saveTheme)
    // =====================================================
    private void selectTheme(String themeId) {
        selectedThemeId = themeId;

        // Calcule couleurs/ressources pour Prefs.saveTheme (comme ton système existant)
        int textColor = getThemeTextColor(themeId);
        int cardBgRes = getThemeCardBackground(themeId);

        // ✅ save direct
        Prefs.saveTheme(this, themeId, textColor, cardBgRes);

        refreshSelectionUI();
    }

    private void refreshSelectionUI() {
        if (themeSelDawn != null) themeSelDawn.setVisibility(View.GONE);
        if (themeSelSea != null) themeSelSea.setVisibility(View.GONE);
        if (themeSelMirror != null) themeSelMirror.setVisibility(View.GONE);

        if ("dawn".equals(selectedThemeId) && themeSelDawn != null) {
            themeSelDawn.setVisibility(View.VISIBLE);
        } else if ("sea".equals(selectedThemeId) && themeSelSea != null) {
            themeSelSea.setVisibility(View.VISIBLE);
        } else if ("mirror".equals(selectedThemeId) && themeSelMirror != null) {
            themeSelMirror.setVisibility(View.VISIBLE);
        }
    }

    // =====================================================
    // Helpers : text color + background resource
    // =====================================================
    private int getThemeTextColor(String themeId) {
        switch (themeId) {
            case "mirror":
                return 0xFFFFFFFF; // blanc
            case "sea":
                return 0xFF1F1F1F; // noir doux
            case "dawn":
            default:
                return 0xFF1F1F1F;
        }
    }

    private int getThemeCardBackground(String themeId) {
        switch (themeId) {
            case "sea":
                return R.drawable.theme_bg_sky;     // ✅ mets ton drawable "sea" ici si tu en as un
            case "mirror":
                return R.drawable.theme_bg_midnight; // ✅ mets ton drawable "mirror" ici si tu en as un
            case "dawn":
            default:
                return R.drawable.theme_bg_dawn;
        }
    }
}
