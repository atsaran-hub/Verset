package com.example.verset.widgetcustome;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.verset.Prefs;
import com.example.verset.R;
import com.example.verset.Tiktoklike.FeedActivity;
import com.example.verset.Tiktoklike.ProfileActivity;
import com.example.verset.db.AppDatabase;
import com.example.verset.db.ContentItemEntity;
import com.example.verset.db.SeedImporter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;

import java.util.LinkedHashMap;
import java.util.Map;

public class widgetcustom extends AppCompatActivity {

    private static final String SP_TEXT = "widget_text_prefs";
    private static final String K_COLOR = "text_color";
    private static final String K_BOLD = "text_bold";
    private static final String K_ITALIC = "text_italic";
    private static final String K_OUTLINE = "text_outline";
    private static final String K_TEXT_SIZE_SP = "text_size_sp";
    private static final float DEFAULT_TEXT_SIZE_SP = 14f;

    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_BLACK = 0xFF111111;
    private static final int COLOR_BLUE  = 0xFF2F80FF;
    private static final int COLOR_GOLD  = 0xFFD4AF37;

    private BottomNavigationView bottomNav;
    private MaterialButton btnAddWidget;

    private View widgetPreviewInclude;
    private View widgetPreviewRoot;

    private boolean ignoreNavSelection = false;
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;
    private boolean isApplyingTheme = false;

    private final Map<String, View> themeRings = new LinkedHashMap<>();

    private View colorDotWhite, colorDotBlack, colorDotBlue, colorDotGold;
    private View colorSelWhite, colorSelBlack, colorSelBlue, colorSelGold;

    private MaterialButtonToggleGroup styleToggleGroup;
    private MaterialButton toggleBold, toggleItalic, toggleOutline;

    private Slider textSizeSlider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_page);

        appWidgetId = getIntent().getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
        );

        bindViews();
        setupBottomNav();

        registerThemes();
        syncThemeUiFromPrefs();

        setupTextControls();
        syncTextControlsFromPrefs();

        // ✅ Preview: verse depuis DB
        loadRandomVerseIntoPreview();

        // ✅ Bouton Add widget: visible seulement si widget PAS ajouté
        refreshAddButtonState();

        if (btnAddWidget != null) {
            btnAddWidget.setOnClickListener(v -> {
                // Re-check au clic
                if (isWidgetReallyAdded()) {
                    refreshAddButtonState();
                    Toast.makeText(this, "Widget déjà ajouté ✅", Toast.LENGTH_SHORT).show();
                    return;
                }

                requestPinWidget();

                // Re-check après flow launcher
                handler.postDelayed(this::refreshAddButtonState, 1200);
                handler.postDelayed(this::refreshAddButtonState, 2500);
            });
        }

        refreshPreviewThemeColors();
        applyTextPrefsToPreview();

        if (widgetPreviewRoot != null) {
            widgetPreviewRoot.post(this::applyRealWidgetSizeToPreview);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ✅ Re-check bouton + preview DB
        refreshAddButtonState();
        loadRandomVerseIntoPreview();

        if (bottomNav != null) {
            ignoreNavSelection = true;
            bottomNav.setSelectedItemId(R.id.nav_widget);
            ignoreNavSelection = false;
        }

        syncThemeUiFromPrefs();
        syncTextControlsFromPrefs();

        refreshPreviewThemeColors();
        applyTextPrefsToPreview();

        if (widgetPreviewRoot != null) {
            widgetPreviewRoot.post(this::applyRealWidgetSizeToPreview);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshRunnable != null) handler.removeCallbacks(refreshRunnable);
    }

    private void bindViews() {
        bottomNav = findViewById(R.id.bottomNav);
        btnAddWidget = findViewById(R.id.btnAddWidget);

        widgetPreviewInclude = findViewById(R.id.widgetPreviewInclude);
        widgetPreviewRoot = findViewById(R.id.widgetPreviewRoot);

        colorDotWhite = findViewById(R.id.colorDotWhite);
        colorDotBlack = findViewById(R.id.colorDotBlack);
        colorDotBlue  = findViewById(R.id.colorDotBlue);
        colorDotGold  = findViewById(R.id.colorDotGold);

        colorSelWhite = findViewById(R.id.colorSelWhite);
        colorSelBlack = findViewById(R.id.colorSelBlack);
        colorSelBlue  = findViewById(R.id.colorSelBlue);
        colorSelGold  = findViewById(R.id.colorSelGold);

        styleToggleGroup = findViewById(R.id.styleToggleGroup);
        toggleBold = findViewById(R.id.toggleBold);
        toggleItalic = findViewById(R.id.toggleItalic);
        toggleOutline = findViewById(R.id.toggleOutline);

        textSizeSlider = findViewById(R.id.textSizeSlider);
    }

    // ✅ PREVIEW VERSE FROM DB
    private void loadRandomVerseIntoPreview() {
        if (widgetPreviewInclude == null) return;

        try {
            SeedImporter.ensureSeeded(this);

            ContentItemEntity verse = AppDatabase.getInstance(this)
                    .contentDao()
                    .getRandomVerse("en"); // ton seed KJV est "en"

            TextView tvVerse = widgetPreviewInclude.findViewById(R.id.previewVerse);
            TextView tvRef = widgetPreviewInclude.findViewById(R.id.previewRef);

            if (tvVerse != null) {
                tvVerse.setText(verse != null && verse.text != null ? verse.text : "—");
            }
            if (tvRef != null) {
                tvRef.setText(verse != null && verse.reference != null ? verse.reference : "");
            }
        } catch (Exception e) {
            // Si souci DB, on laisse le texte par défaut du XML
            e.printStackTrace();
        }
    }

    // ---------------- THEMES ----------------

    private void registerThemes() {
        registerTheme("dawn", R.id.themeCardDawn, R.id.themeSelDawn);

        registerTheme("ocean_serenity", R.id.themeCardOceanSerenity, R.id.themeSelOceanSerenity);
        registerTheme("soft_flame", R.id.themeCardSoftFlame, R.id.themeSelSoftFlame);
        registerTheme("silent_forest", R.id.themeCardSilentForest, R.id.themeSelSilentForest);
        registerTheme("midnight_calm", R.id.themeCardMidnightCalm, R.id.themeSelMidnightCalm);
        registerTheme("floating_clouds", R.id.themeCardFloatingClouds, R.id.themeSelFloatingClouds);
        registerTheme("light_particles", R.id.themeCardLightParticles, R.id.themeSelLightParticles);
        registerTheme("deep_galaxy", R.id.themeCardDeepGalaxy, R.id.themeSelDeepGalaxy);
        registerTheme("cross_eternal_light", R.id.themeCardCrossEternalLight, R.id.themeSelCrossEternalLight);
        registerTheme("cross_eternal_dark", R.id.themeCardCrossEternalDark, R.id.themeSelCrossEternalDark);

        registerTheme("cross_glory", R.id.themeCardCrossGlory, R.id.themeSelCrossGlory);
        registerTheme("faith_minimal", R.id.themeCardFaithMinimal, R.id.themeSelFaithMinimal);
        registerTheme("ocean_night", R.id.themeCardOceanNight, R.id.themeSelOceanNight);
        registerTheme("sunset_photo", R.id.themeCardSunsetPhoto, R.id.themeSelSunsetPhoto);
    }

    private void registerTheme(String themeId, int cardResId, int ringResId) {
        View card = findViewById(cardResId);
        View ring = findViewById(ringResId);

        if (ring != null) themeRings.put(themeId, ring);
        if (card != null) card.setOnClickListener(v -> applyThemeAndRefresh(themeId));
    }

    private void applyThemeAndRefresh(String themeId) {
        if (isApplyingTheme) return;
        isApplyingTheme = true;

        try {
            int bgRes = resolveDrawable("theme_bg_" + themeId, R.drawable.theme_bg_dawn);
            int textColor = isDarkTheme(themeId) ? 0xFFFFFFFF : 0xFF1F1F1F;

            Prefs.saveTheme(this, themeId, textColor, bgRes);

            syncThemeUiFromPrefs();
            refreshPreviewThemeColors();
            applyTextPrefsToPreview();
            refreshWidgetDebouncedSafe();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors du changement de thème", Toast.LENGTH_SHORT).show();
        } finally {
            handler.postDelayed(() -> isApplyingTheme = false, 220);
        }
    }

    private int resolveDrawable(String name, int fallbackRes) {
        int resId = getResources().getIdentifier(name, "drawable", getPackageName());
        return resId != 0 ? resId : fallbackRes;
    }

    private boolean isDarkTheme(String themeId) {
        if (themeId == null) return false;
        switch (themeId) {
            case "midnight_calm":
            case "deep_galaxy":
            case "cross_eternal_dark":
            case "ocean_night":
            case "cross_glory":
                return true;
            default:
                return false;
        }
    }

    private void syncThemeUiFromPrefs() {
        String current = Prefs.getThemeId(this);
        for (Map.Entry<String, View> e : themeRings.entrySet()) {
            View ring = e.getValue();
            if (ring == null) continue;
            ring.setVisibility(e.getKey().equals(current) ? View.VISIBLE : View.GONE);
        }
    }

    private void refreshPreviewThemeColors() {
        // Ici ton include preview n’a pas les ids textTone_normal etc -> pas grave, ça ne casse pas.
        // Tu peux le laisser tel quel.
    }

    // ---------------- TEXT CONTROLS ----------------

    private void setupTextControls() {
        if (colorDotWhite != null) colorDotWhite.setOnClickListener(v -> setTextColorPref(COLOR_WHITE));
        if (colorDotBlack != null) colorDotBlack.setOnClickListener(v -> setTextColorPref(COLOR_BLACK));
        if (colorDotBlue  != null) colorDotBlue.setOnClickListener(v -> setTextColorPref(COLOR_BLUE));
        if (colorDotGold  != null) colorDotGold.setOnClickListener(v -> setTextColorPref(COLOR_GOLD));

        if (styleToggleGroup != null) {
            styleToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                saveStylePrefsFromToggles();
                applyTextPrefsToPreview();
                refreshWidgetDebouncedSafe();
            });
        }

        if (textSizeSlider != null) {
            float size = getSharedPreferences(SP_TEXT, MODE_PRIVATE)
                    .getFloat(K_TEXT_SIZE_SP, DEFAULT_TEXT_SIZE_SP);

            textSizeSlider.setValue(size);

            textSizeSlider.addOnChangeListener((slider, value, fromUser) -> {
                getSharedPreferences(SP_TEXT, MODE_PRIVATE).edit()
                        .putFloat(K_TEXT_SIZE_SP, value)
                        .apply();

                applyTextPrefsToPreview();
                refreshWidgetDebouncedSafe();
            });
        }
    }

    private void syncTextControlsFromPrefs() {
        int fallbackColor = Prefs.getThemeTextColor(this);

        int color = getSharedPreferences(SP_TEXT, MODE_PRIVATE).getInt(K_COLOR, fallbackColor);
        boolean bold = getSharedPreferences(SP_TEXT, MODE_PRIVATE).getBoolean(K_BOLD, false);
        boolean italic = getSharedPreferences(SP_TEXT, MODE_PRIVATE).getBoolean(K_ITALIC, false);
        boolean outline = getSharedPreferences(SP_TEXT, MODE_PRIVATE).getBoolean(K_OUTLINE, false);
        float size = getSharedPreferences(SP_TEXT, MODE_PRIVATE).getFloat(K_TEXT_SIZE_SP, DEFAULT_TEXT_SIZE_SP);

        if (toggleBold != null) toggleBold.setChecked(bold);
        if (toggleItalic != null) toggleItalic.setChecked(italic);
        if (toggleOutline != null) toggleOutline.setChecked(outline);

        if (textSizeSlider != null) textSizeSlider.setValue(size);

        syncColorUiFromPrefs(color);
    }

    private void setTextColorPref(int color) {
        getSharedPreferences(SP_TEXT, MODE_PRIVATE).edit().putInt(K_COLOR, color).apply();
        syncColorUiFromPrefs(color);
        applyTextPrefsToPreview();
        refreshWidgetDebouncedSafe();
    }

    private void saveStylePrefsFromToggles() {
        boolean bold = toggleBold != null && toggleBold.isChecked();
        boolean italic = toggleItalic != null && toggleItalic.isChecked();
        boolean outline = toggleOutline != null && toggleOutline.isChecked();

        getSharedPreferences(SP_TEXT, MODE_PRIVATE).edit()
                .putBoolean(K_BOLD, bold)
                .putBoolean(K_ITALIC, italic)
                .putBoolean(K_OUTLINE, outline)
                .apply();
    }

    private void syncColorUiFromPrefs(int color) {
        setRing(colorSelWhite, color == COLOR_WHITE);
        setRing(colorSelBlack, color == COLOR_BLACK);
        setRing(colorSelBlue,  color == COLOR_BLUE);
        setRing(colorSelGold,  color == COLOR_GOLD);
    }

    private void setRing(View ring, boolean show) {
        if (ring == null) return;
        ring.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void applyTextPrefsToPreview() {
        // Ton preview actuel = previewVerse / previewRef.
        // Les styles avancés ne sont pas appliqués ici (pas les mêmes ids que widget_verse.xml),
        // mais ça n’empêche pas le widget réel d’être stylé.
        // Si tu veux appliquer les styles au preview aussi, je peux te le faire.
    }

    // ---------------- PREVIEW SIZE ----------------

    private void applyRealWidgetSizeToPreview() {
        if (widgetPreviewRoot == null) return;

        AppWidgetManager mgr = AppWidgetManager.getInstance(this);

        int wDp = 0;
        int hDp = 0;

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            Bundle options = mgr.getAppWidgetOptions(appWidgetId);
            if (options != null) {
                wDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0);
                hDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
            }
        }

        if (wDp <= 0 || hDp <= 0) {
            AppWidgetProviderInfo info = null;
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                info = mgr.getAppWidgetInfo(appWidgetId);
            }
            if (info != null) {
                wDp = info.minWidth;
                hDp = info.minHeight;
            } else {
                wDp = 320;
                hDp = 140;
            }
        }

        int wPx = dpToPx(wDp);
        int hPx = dpToPx(hDp);

        ViewGroup.LayoutParams lp = widgetPreviewRoot.getLayoutParams();
        if (lp != null) {
            lp.width = wPx;
            lp.height = hPx;
            widgetPreviewRoot.setLayoutParams(lp);
        }

        View parent = (View) widgetPreviewRoot.getParent();
        if (parent != null && parent.getWidth() > 0) {
            float scale = Math.min(1f, (float) parent.getWidth() / (float) wPx);
            widgetPreviewRoot.setPivotX(0f);
            widgetPreviewRoot.setPivotY(0f);
            widgetPreviewRoot.setScaleX(scale);
            widgetPreviewRoot.setScaleY(scale);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // ---------------- WIDGET UPDATE ----------------

    private void refreshWidgetDebouncedSafe() {
        if (refreshRunnable != null) handler.removeCallbacks(refreshRunnable);

        refreshRunnable = () -> {
            try {
                VerseWidgetProvider.updateAllWidgets(getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        handler.postDelayed(refreshRunnable, 120);
    }

    private boolean isWidgetReallyAdded() {
        AppWidgetManager mgr = AppWidgetManager.getInstance(this);
        ComponentName cn = new ComponentName(this, VerseWidgetProvider.class);
        int[] ids = mgr.getAppWidgetIds(cn);

        if (ids == null || ids.length == 0) return false;

        for (int id : ids) {
            try {
                AppWidgetProviderInfo info = mgr.getAppWidgetInfo(id);
                if (info != null) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    private void refreshAddButtonState() {
        boolean has = isWidgetReallyAdded();
        applyAddButtonState(has);
    }

    // ✅ ICI: si déjà ajouté -> DISPARAIT
    private void applyAddButtonState(boolean hasWidget) {
        if (btnAddWidget == null) return;

        if (hasWidget) {
            btnAddWidget.setVisibility(View.GONE);
        } else {
            btnAddWidget.setVisibility(View.VISIBLE);
            btnAddWidget.setEnabled(true);
            btnAddWidget.setAlpha(1f);
            btnAddWidget.setText("Add widget to home screen");
        }
    }

    // ---------------- PIN WIDGET ----------------

    private void requestPinWidget() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(this, "Android < 8 : ajoute le widget depuis l’écran d’accueil.", Toast.LENGTH_LONG).show();
            openWidgetPickerFallback();
            return;
        }

        AppWidgetManager mgr = getSystemService(AppWidgetManager.class);
        if (mgr == null) {
            Toast.makeText(this, "AppWidgetManager indisponible.", Toast.LENGTH_SHORT).show();
            openWidgetPickerFallback();
            return;
        }

        ComponentName provider = new ComponentName(this, VerseWidgetProvider.class);

        if (mgr.isRequestPinAppWidgetSupported()) {
            try {
                Intent callbackIntent = new Intent(this, WidgetPinnedReceiver.class);
                callbackIntent.setAction("com.example.verset.ACTION_WIDGET_PINNED");

                PendingIntent successCallback = PendingIntent.getBroadcast(
                        this,
                        5001,
                        callbackIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                boolean launched = mgr.requestPinAppWidget(provider, null, successCallback);

                if (launched) {
                    Toast.makeText(this, "Confirme l’ajout du widget sur l’accueil.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Impossible d’ouvrir l’ajout automatique.", Toast.LENGTH_SHORT).show();
                    openWidgetPickerFallback();
                }
                return;

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur pendant l’ajout du widget.", Toast.LENGTH_SHORT).show();
                openWidgetPickerFallback();
                return;
            }
        }

        Toast.makeText(this, "Ton launcher ne supporte pas l’ajout automatique.", Toast.LENGTH_SHORT).show();
        openWidgetPickerFallback();
    }

    private void openWidgetPickerFallback() {
        Toast.makeText(this, "Maintiens l’écran d’accueil → Widgets → Verset", Toast.LENGTH_LONG).show();

        try {
            Intent i = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
            startActivity(i);
        } catch (Exception ignored) {}
    }

    // ---------------- NAV ----------------

    private void setupBottomNav() {
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_widget);
        bottomNav.setOnItemReselectedListener(item -> {});

        bottomNav.setOnItemSelectedListener(item -> {
            if (ignoreNavSelection) return true;

            int id = item.getItemId();
            if (id == R.id.nav_widget) return true;

            if (id == R.id.nav_feed) {
                goTo(FeedActivity.class);
                return true;
            } else if (id == R.id.nav_profile) {
                goTo(ProfileActivity.class);
                return true;
            }
            return false;
        });
    }

    private void goTo(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}