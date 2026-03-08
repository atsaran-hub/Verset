package com.example.verset;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.verset.db.AppDatabase;
import com.example.verset.db.ContentItemEntity;
import com.example.verset.db.SeedImporter;
import com.example.verset.widgetcustome.VerseWidgetProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WidgetReadyActivity extends AppCompatActivity {

    private TextView textDebugPrefs;

    // ✅ DB: tag_key -> count
    private final Map<String, Integer> verseCountByTag = new HashMap<>();
    private final Map<String, String> tagLabelByKey = new HashMap<>();

    // ✅ Selected goals (Prefs)  (NEW SYSTEM)
    private final Set<String> selectedGoals = new HashSet<>();

    // ✅ DEBUG DB extras
    private int totalVersesInTable = -1;
    private ContentItemEntity randomVerse = null;

    // UI
    private Button buttonAddWidget;

    // Optional: if your UI still uses peace/strength/faith/hope/wisdom anywhere
    private String mapUiKeyToDbTagKey(String uiKey) {
        switch (uiKey) {
            case "peace": return "peace";
            case "strength": return "encouragement";
            case "faith": return "guidance";
            case "wisdom": return "guidance";
            case "hope": return "hope";
            default: return uiKey;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_ready);

        // ✅ Ensure NEW KJV DB is seeded
        SeedImporter.ensureSeeded(this);

        textDebugPrefs = findViewById(R.id.textDebugPrefs);

        Button buttonChangeVerse = findViewById(R.id.buttonChangeVerse);
        Button buttonCustomizeWidget = findViewById(R.id.buttonCustomizeWidget);
        buttonAddWidget = findViewById(R.id.buttonAddWidget);
        View textLater = findViewById(R.id.textLater);

        restoreSelectionFromPrefs();

        // ✅ Load debug from NEW DB (background)
        loadDebugFromNewDbAsync();

        // ✅ Choose a verse screen
        buttonChangeVerse.setOnClickListener(v ->
                startActivity(new Intent(this, VerseTypeActivity.class))
        );

        // ✅ OPEN Customize Widget screen (you said we keep it empty for now)
        buttonCustomizeWidget.setOnClickListener(v -> {
            Intent i = new Intent(WidgetReadyActivity.this, WidgetCustomizeActivity.class);
            i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            startActivity(i);
        });

        // ✅ Update button state immediately
        updateAddWidgetButtonState();

        // ✅ Add widget to home screen
        buttonAddWidget.setOnClickListener(v -> {
            if (isWidgetAlreadyAdded()) {
                Toast.makeText(this, "Widget already added on home screen.", Toast.LENGTH_SHORT).show();
                updateAddWidgetButtonState();
                return;
            }

            addWidgetToHome();

            // Re-check after a short delay (launcher pin flow)
            v.postDelayed(this::updateAddWidgetButtonState, 900);
        });

        textLater.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAddWidgetButtonState();
    }

    // =====================================================
    // ✅ WIDGET EXISTENCE CHECK
    // =====================================================
    private boolean isWidgetAlreadyAdded() {
        AppWidgetManager mgr = AppWidgetManager.getInstance(this);
        ComponentName provider = new ComponentName(this, VerseWidgetProvider.class);
        int[] ids = mgr.getAppWidgetIds(provider);
        return ids != null && ids.length > 0;
    }

    private void updateAddWidgetButtonState() {
        if (buttonAddWidget == null) return;

        boolean already = isWidgetAlreadyAdded();
        buttonAddWidget.setEnabled(!already);
        buttonAddWidget.setAlpha(already ? 0.55f : 1f);
        buttonAddWidget.setText(already ? "Widget already added" : "Add widget to home screen");
    }

    // =====================================================
    // ✅ ADD WIDGET TO HOME
    // =====================================================
    private void addWidgetToHome() {
        AppWidgetManager mgr = AppWidgetManager.getInstance(this);
        ComponentName provider = new ComponentName(this, VerseWidgetProvider.class);

        // Android 8+ : requestPinAppWidget
        if (mgr.isRequestPinAppWidgetSupported()) {
            boolean ok = mgr.requestPinAppWidget(provider, null, null);
            if (!ok) {
                openWidgetPicker();
            } else {
                Toast.makeText(this, "Add the widget on your home screen.", Toast.LENGTH_LONG).show();
            }
            return;
        }

        openWidgetPicker();
    }

    private void openWidgetPicker() {
        try {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
            startActivity(intent);
            Toast.makeText(this, "Choose the widget and place it on your home screen.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Long press home screen → Widgets → Verset", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * ✅ Load debug from NEW DB:
     * - counts per tag
     * - total verses
     * - random verse preview
     */
    private void loadDebugFromNewDbAsync() {
        if (textDebugPrefs != null) {
            textDebugPrefs.setText("DEBUG: loading DB...");
        }

        new Thread(() -> {
            String error = null;

            verseCountByTag.clear();
            tagLabelByKey.clear();
            totalVersesInTable = -1;
            randomVerse = null;

            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());

                // ✅ total
                totalVersesInTable = db.contentDao().getContentCount();

                // ✅ random preview
                randomVerse = db.contentDao().getRandomVerse("en");

                // ✅ counts per tag (the tags you use in onboarding/profile)
                String[] tags = new String[]{"comfort", "encouragement", "peace", "hope", "anxiety", "guidance"};

                for (String t : tags) {
                    int c = db.contentDao().countByTag(t, "en");
                    verseCountByTag.put(t, c);
                }

                // Labels (for debug display)
                tagLabelByKey.put("comfort", "Comfort");
                tagLabelByKey.put("encouragement", "Encouragement");
                tagLabelByKey.put("peace", "Peace");
                tagLabelByKey.put("hope", "Hope");
                tagLabelByKey.put("anxiety", "Anxiety relief");
                tagLabelByKey.put("guidance", "Guidance");

            } catch (Exception e) {
                error = e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage());
            }

            final String finalError = error;

            runOnUiThread(() -> {
                if (finalError != null) {
                    if (textDebugPrefs != null) {
                        textDebugPrefs.setText("DEBUG DB ERROR: " + finalError);
                    }
                    return;
                }
                renderDebugIntoTextView();
            });
        }).start();
    }

    private void restoreSelectionFromPrefs() {
        // ✅ NEW: use Goals (the new system you added on Profile)
        Set<String> saved = Prefs.getGoals(this);
        selectedGoals.clear();
        if (saved != null) selectedGoals.addAll(saved);
    }

    private void renderDebugIntoTextView() {
        if (textDebugPrefs == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("DEBUG PREFS + NEW KJV DB\n\n");

        // Prefs
        sb.append("Selected goals (Prefs): ").append(selectedGoals).append("\n\n");

        // DB totals
        sb.append("DB verses total (table 'content_items'): ")
                .append(totalVersesInTable)
                .append("\n\n");

        // Random verse preview
        sb.append("Random verse preview:\n");
        if (randomVerse == null) {
            sb.append("- NULL (empty DB? seed JSON missing?)\n\n");
        } else {
            String verse = (randomVerse.text == null) ? "(null text)" : randomVerse.text.trim();
            String ref = (randomVerse.reference == null) ? "(null reference)" : randomVerse.reference.trim();

            sb.append("- ").append(ref).append("\n");
            sb.append("- ").append(verse).append("\n\n");
        }

        // Counts by tag
        sb.append("Counts in DB (tags -> verses):\n");
        int totalTagged = 0;

        for (Map.Entry<String, Integer> e : verseCountByTag.entrySet()) {
            String key = e.getKey();
            int count = e.getValue();
            totalTagged += count;

            String label = tagLabelByKey.getOrDefault(key, "");
            sb.append("- ").append(key);
            if (!label.isEmpty()) sb.append(" (").append(label).append(")");
            sb.append(" = ").append(count).append("\n");
        }

        sb.append("\nSum of tag counts: ").append(totalTagged).append("\n\n");

        // Selected goals counts
        sb.append("Selected goals -> counts:\n");
        int selectedTotal = 0;
        for (String goalKey : selectedGoals) {
            String dbKey = mapUiKeyToDbTagKey(goalKey);
            int c = verseCountByTag.getOrDefault(dbKey, 0);
            selectedTotal += c;
            sb.append("- ").append(goalKey).append(" -> ").append(dbKey).append(" = ").append(c).append("\n");
        }
        sb.append("\nTotal verses available for selection: ").append(selectedTotal).append("\n");

        if (!selectedGoals.isEmpty() && selectedTotal == 0) {
            sb.append("\n⚠️ WARNING: selected goals do not match your DB tags.\n");
        }

        if (totalVersesInTable == 0) {
            sb.append("\n⚠️ WARNING: DB is empty -> check content_seed_kjv.json in assets + reinstall app.\n");
        }

        textDebugPrefs.setText(sb.toString());
    }
}