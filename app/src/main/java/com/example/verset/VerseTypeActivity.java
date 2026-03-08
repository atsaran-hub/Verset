package com.example.verset;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.verset.db.AppDatabase;
import com.example.verset.db.SeedImporter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VerseTypeActivity extends AppCompatActivity {

    // UI
    private LinearLayout optPeace, optStrength, optFaith, optHope, optWisdom;
    private ImageView iconPeace, iconStrength, iconFaith, iconHope, iconWisdom;
    private View btnNext;

    // State
    private final Set<String> selectedThemes = new HashSet<>();

    // Counts: db tag key -> count
    private final Map<String, Integer> verseCountByTheme = new HashMap<>();

    // DB loaded flag
    private volatile boolean dbReady = false;

    // Drawables
    private final int drawableUnchecked = R.drawable.radio_orange_unchecked;
    private int drawableChecked;

    /**
     * UI keys on this screen:
     * - peace
     * - strength
     * - faith
     * - hope
     * - wisdom
     *
     * New DB tag keys:
     * - comfort
     * - encouragement
     * - peace
     * - hope
     * - anxiety
     * - guidance
     */
    private String mapUiKeyToDbTagKey(String uiKey) {
        switch (uiKey) {
            case "peace":
                return "peace";
            case "strength":
                return "encouragement";
            case "faith":
                return "guidance";
            case "wisdom":
                return "guidance";
            case "hope":
                return "hope";
            default:
                return uiKey;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.typedeverset);

        // Ensure new KJV database is seeded
        SeedImporter.ensureSeeded(this);

        // Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Find views
        optPeace = findViewById(R.id.optPeace);
        optStrength = findViewById(R.id.optStrength);
        optFaith = findViewById(R.id.optFaith);
        optHope = findViewById(R.id.optHope);
        optWisdom = findViewById(R.id.optWisdom);

        iconPeace = findViewById(R.id.iconPeace);
        iconStrength = findViewById(R.id.iconStrength);
        iconFaith = findViewById(R.id.iconFaith);
        iconHope = findViewById(R.id.iconHope);
        iconWisdom = findViewById(R.id.iconWisdom);

        btnNext = findViewById(R.id.buttonGetStarted);

        // Checked drawable fallback
        drawableChecked = getResources().getIdentifier(
                "radio_orange_checked",
                "drawable",
                getPackageName()
        );
        if (drawableChecked == 0) {
            drawableChecked = drawableUnchecked;
        }

        // Restore saved selection if you want later.
        restoreSelectionFromPrefs();

        // Disable next until DB is loaded
        setNextEnabled(false);

        // Load counts from NEW DB
        loadVerseCountsFromNewDbAsync();

        // Toggle listeners
        if (optPeace != null) {
            optPeace.setOnClickListener(v -> toggle("peace", iconPeace));
        }
        if (optStrength != null) {
            optStrength.setOnClickListener(v -> toggle("strength", iconStrength));
        }
        if (optFaith != null) {
            optFaith.setOnClickListener(v -> toggle("faith", iconFaith));
        }
        if (optHope != null) {
            optHope.setOnClickListener(v -> toggle("hope", iconHope));
        }
        if (optWisdom != null) {
            optWisdom.setOnClickListener(v -> toggle("wisdom", iconWisdom));
        }

        // Next button
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (!dbReady) {
                    Toast.makeText(this, "Database is still loading…", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedThemes.isEmpty()) {
                    Toast.makeText(this, "Please select at least one theme", Toast.LENGTH_SHORT).show();
                    return;
                }

                int total = 0;
                for (String uiKey : selectedThemes) {
                    String tagKey = mapUiKeyToDbTagKey(uiKey);
                    total += verseCountByTheme.getOrDefault(tagKey, 0);
                }

                if (total == 0) {
                    Toast.makeText(this, "No verses found for selected themes.", Toast.LENGTH_LONG).show();
                    return;
                }

                // If needed later, save these choices in Prefs here.

                startActivity(new Intent(VerseTypeActivity.this, ThemeActivity.class));
            });
        }
    }

    private void setNextEnabled(boolean enabled) {
        if (btnNext == null) return;
        btnNext.setEnabled(enabled);
        btnNext.setAlpha(enabled ? 1f : 0.5f);
    }

    /**
     * Loads verse counts from the new Room DB in a background thread.
     */
    private void loadVerseCountsFromNewDbAsync() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());

                String[] uiKeys = new String[]{
                        "peace",
                        "strength",
                        "faith",
                        "hope",
                        "wisdom"
                };

                verseCountByTheme.clear();

                for (String uiKey : uiKeys) {
                    String tagKey = mapUiKeyToDbTagKey(uiKey);
                    int count = db.contentDao().countByTag(tagKey, "en");

                    // Avoid double-adding for repeated mapped keys
                    if (!verseCountByTheme.containsKey(tagKey)) {
                        verseCountByTheme.put(tagKey, count);
                    }
                }

                dbReady = true;

                runOnUiThread(() -> setNextEnabled(true));

            } catch (Exception e) {
                dbReady = false;
                runOnUiThread(() -> {
                    setNextEnabled(false);
                    Toast.makeText(
                            this,
                            "DB error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        }).start();
    }

    private void restoreSelectionFromPrefs() {
        // If you want later:
        // Set<String> saved = Prefs.getVerseThemes(this);
        selectedThemes.clear();
        // selectedThemes.addAll(saved);
        refreshAllIcons();
    }

    private void toggle(String uiKey, ImageView icon) {
        boolean selected;
        if (selectedThemes.contains(uiKey)) {
            selectedThemes.remove(uiKey);
            selected = false;
        } else {
            selectedThemes.add(uiKey);
            selected = true;
        }

        refreshIcon(icon, selected);

        if (dbReady && selected) {
            String tagKey = mapUiKeyToDbTagKey(uiKey);
            int count = verseCountByTheme.getOrDefault(tagKey, 0);
            Toast.makeText(this, "Verses in this theme: " + count, Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshIcon(ImageView icon, boolean selected) {
        if (icon == null) return;
        icon.setImageResource(selected ? drawableChecked : drawableUnchecked);
    }

    private void refreshAllIcons() {
        refreshIcon(iconPeace, selectedThemes.contains("peace"));
        refreshIcon(iconStrength, selectedThemes.contains("strength"));
        refreshIcon(iconFaith, selectedThemes.contains("faith"));
        refreshIcon(iconHope, selectedThemes.contains("hope"));
        refreshIcon(iconWisdom, selectedThemes.contains("wisdom"));
    }
}