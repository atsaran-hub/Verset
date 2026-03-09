package com.example.verset.widgetcustome;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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
    private static final int COLOR_BLUE = 0xFF2F80FF;
    private static final int COLOR_GOLD = 0xFFD4AF37;

    private BottomNavigationView bottomNav;
    private MaterialButton btnAddWidget;
    private MaterialButton btnGallery;

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
    private ActivityResultLauncher<String[]> galleryLauncher;

    private enum Block { TONE, VERSE, REF }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_page);

        appWidgetId = getIntent().getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
        );

        setupGalleryPicker();
        bindViews();
        setupBottomNav();

        registerThemes();
        syncThemeUiFromPrefs();

        setupTextControls();
        syncTextControlsFromPrefs();

        loadRandomVerseIntoPreview();
        updateGalleryButtonState();
        refreshPreviewThemeColors();
        applyTextPrefsToPreview();

        if (btnGallery != null) {
            btnGallery.setOnClickListener(v -> {
                if (galleryLauncher != null) {
                    galleryLauncher.launch(new String[]{"image/*"});
                }
            });
        }

        if (btnAddWidget != null) {
            btnAddWidget.setVisibility(View.VISIBLE);
            btnAddWidget.setOnClickListener(v -> addWidgetToHome());
        }

        if (widgetPreviewRoot != null) {
            widgetPreviewRoot.post(this::applyRealWidgetSizeToPreview);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadRandomVerseIntoPreview();
        updateGalleryButtonState();
        syncThemeUiFromPrefs();
        syncTextControlsFromPrefs();
        refreshPreviewThemeColors();
        applyTextPrefsToPreview();

        if (bottomNav != null) {
            ignoreNavSelection = true;
            bottomNav.setSelectedItemId(R.id.nav_widget);
            ignoreNavSelection = false;
        }

        if (widgetPreviewRoot != null) {
            widgetPreviewRoot.post(this::applyRealWidgetSizeToPreview);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }

    private void bindViews() {
        bottomNav = findViewById(R.id.bottomNav);
        btnAddWidget = findViewById(R.id.btnAddWidget);
        btnGallery = findViewById(R.id.btnGallery);

        widgetPreviewInclude = findViewById(R.id.widgetPreviewInclude);
        widgetPreviewRoot = findViewById(R.id.widgetPreviewRoot);

        colorDotWhite = findViewById(R.id.colorDotWhite);
        colorDotBlack = findViewById(R.id.colorDotBlack);
        colorDotBlue = findViewById(R.id.colorDotBlue);
        colorDotGold = findViewById(R.id.colorDotGold);

        colorSelWhite = findViewById(R.id.colorSelWhite);
        colorSelBlack = findViewById(R.id.colorSelBlack);
        colorSelBlue = findViewById(R.id.colorSelBlue);
        colorSelGold = findViewById(R.id.colorSelGold);

        styleToggleGroup = findViewById(R.id.styleToggleGroup);
        toggleBold = findViewById(R.id.toggleBold);
        toggleItalic = findViewById(R.id.toggleItalic);
        toggleOutline = findViewById(R.id.toggleOutline);

        textSizeSlider = findViewById(R.id.textSizeSlider);
    }

    private void setupGalleryPicker() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) return;

                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {
                    }

                    String localPath = copyImageToInternalStorage(uri);
                    if (localPath == null) {
                        Toast.makeText(this, "Impossible de lire l'image", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Prefs.setWidgetCustomImageUri(this, uri.toString());
                    Prefs.setWidgetLocalImagePath(this, localPath);
                    Prefs.saveTheme(this, "custom_gallery", 0xFFFFFFFF, R.drawable.theme_bg_dawn);

                    updateGalleryButtonState();
                    syncThemeUiFromPrefs();
                    refreshPreviewThemeColors();
                    applyTextPrefsToPreview();
                    refreshWidgetDebouncedSafe();

                    Toast.makeText(this, "Photo choisie ✅", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private String copyImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) return null;

            File file = new File(getFilesDir(), "widget_custom_image.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();

            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addWidgetToHome() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(this, "Ajoute le widget depuis l’écran d’accueil.", Toast.LENGTH_LONG).show();
            return;
        }

        AppWidgetManager appWidgetManager = getSystemService(AppWidgetManager.class);
        if (appWidgetManager == null) {
            Toast.makeText(this, "AppWidgetManager indisponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!appWidgetManager.isRequestPinAppWidgetSupported()) {
            Toast.makeText(this, "Ton launcher ne supporte pas l’ajout automatique.", Toast.LENGTH_SHORT).show();
            return;
        }

        ComponentName provider = new ComponentName(this, VerseWidgetProvider.class);

        try {
            boolean ok = appWidgetManager.requestPinAppWidget(provider, null, null);

            if (ok) {
                Toast.makeText(this, "Confirme l’ajout du widget.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Impossible d’ouvrir l’ajout du widget.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur pendant l’ajout du widget.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateGalleryButtonState() {
        if (btnGallery == null) return;

        boolean hasPhoto = Prefs.hasWidgetCustomImage(this);
        boolean isPhotoTheme = "custom_gallery".equals(Prefs.getThemeId(this));

        if (isPhotoTheme && hasPhoto) {
            btnGallery.setText("Change photo");
        } else {
            btnGallery.setText("Choose photo");
        }
    }

    private void loadRandomVerseIntoPreview() {
        if (widgetPreviewInclude == null) return;

        try {
            SeedImporter.ensureSeeded(this);

            ContentItemEntity verse = AppDatabase.getInstance(this)
                    .contentDao()
                    .getRandomVerse("en");

            String verseText = verse != null && verse.text != null ? verse.text : "—";
            String refText = verse != null && verse.reference != null ? verse.reference : "";

            setTextForAllVariants(widgetPreviewInclude, idsBase(Block.VERSE), idsOlb(Block.VERSE), idsOlw(Block.VERSE), verseText);
            setTextForAllVariants(widgetPreviewInclude, idsBase(Block.REF), idsOlb(Block.REF), idsOlw(Block.REF), refText);

            setTextForAllVariants(widgetPreviewInclude, idsBase(Block.TONE), idsOlb(Block.TONE), idsOlw(Block.TONE), "");
            hideAll(widgetPreviewInclude, idsBase(Block.TONE));
            hideAll(widgetPreviewInclude, idsOlb(Block.TONE));
            hideAll(widgetPreviewInclude, idsOlw(Block.TONE));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

            Prefs.clearWidgetCustomImageUri(this);
            Prefs.clearWidgetLocalImagePath(this);
            Prefs.saveTheme(this, themeId, textColor, bgRes);

            syncThemeUiFromPrefs();
            updateGalleryButtonState();
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
            case "custom_gallery":
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
        if (widgetPreviewInclude == null) return;

        ImageView bg = widgetPreviewInclude.findViewById(R.id.bgImage);
        if (bg == null) return;

        String themeId = Prefs.getThemeId(this);

        if ("custom_gallery".equals(themeId) && Prefs.hasWidgetCustomImage(this)) {
            try {
                bg.setImageURI(Uri.parse(Prefs.getWidgetCustomImageUri(this)));
                return;
            } catch (Exception ignored) {
            }
        }

        int bgRes = resolveDrawable("theme_bg_" + themeId, R.drawable.theme_bg_dawn);
        bg.setImageResource(bgRes);
    }

    private void setupTextControls() {
        if (colorDotWhite != null) colorDotWhite.setOnClickListener(v -> setTextColorPref(COLOR_WHITE));
        if (colorDotBlack != null) colorDotBlack.setOnClickListener(v -> setTextColorPref(COLOR_BLACK));
        if (colorDotBlue != null) colorDotBlue.setOnClickListener(v -> setTextColorPref(COLOR_BLUE));
        if (colorDotGold != null) colorDotGold.setOnClickListener(v -> setTextColorPref(COLOR_GOLD));

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
                getSharedPreferences(SP_TEXT, MODE_PRIVATE)
                        .edit()
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
        getSharedPreferences(SP_TEXT, MODE_PRIVATE)
                .edit()
                .putInt(K_COLOR, color)
                .apply();

        syncColorUiFromPrefs(color);
        applyTextPrefsToPreview();
        refreshWidgetDebouncedSafe();
    }

    private void saveStylePrefsFromToggles() {
        boolean bold = toggleBold != null && toggleBold.isChecked();
        boolean italic = toggleItalic != null && toggleItalic.isChecked();
        boolean outline = toggleOutline != null && toggleOutline.isChecked();

        getSharedPreferences(SP_TEXT, MODE_PRIVATE)
                .edit()
                .putBoolean(K_BOLD, bold)
                .putBoolean(K_ITALIC, italic)
                .putBoolean(K_OUTLINE, outline)
                .apply();
    }

    private void syncColorUiFromPrefs(int color) {
        setRing(colorSelWhite, color == COLOR_WHITE);
        setRing(colorSelBlack, color == COLOR_BLACK);
        setRing(colorSelBlue, color == COLOR_BLUE);
        setRing(colorSelGold, color == COLOR_GOLD);
    }

    private void setRing(View ring, boolean show) {
        if (ring == null) return;
        ring.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void applyTextPrefsToPreview() {
        if (widgetPreviewInclude == null) return;

        int fallbackColor = Prefs.getThemeTextColor(this);

        int color = getSharedPreferences(SP_TEXT, MODE_PRIVATE).getInt(K_COLOR, fallbackColor);
        boolean bold = getSharedPreferences(SP_TEXT, MODE_PRIVATE).getBoolean(K_BOLD, false);
        boolean italic = getSharedPreferences(SP_TEXT, MODE_PRIVATE).getBoolean(K_ITALIC, false);
        boolean outline = getSharedPreferences(SP_TEXT, MODE_PRIVATE).getBoolean(K_OUTLINE, false);
        float baseSp = getSharedPreferences(SP_TEXT, MODE_PRIVATE).getFloat(K_TEXT_SIZE_SP, DEFAULT_TEXT_SIZE_SP);

        float verseSp = baseSp;
        float refSp = Math.max(8f, baseSp - 3f);

        int styleIndex = (bold ? 1 : 0) + (italic ? 2 : 0);
        boolean outlineWhite = outline && isDarkColor(color);

        hideAll(widgetPreviewInclude, idsBase(Block.TONE));
        hideAll(widgetPreviewInclude, idsOlb(Block.TONE));
        hideAll(widgetPreviewInclude, idsOlw(Block.TONE));

        applyBlockToPreview(Block.VERSE, styleIndex, outline, outlineWhite, color, verseSp);
        applyBlockToPreview(Block.REF, styleIndex, outline, outlineWhite, color, refSp);
    }

    private void applyBlockToPreview(
            Block block,
            int styleIndex,
            boolean outline,
            boolean outlineWhite,
            int color,
            float textSizeSp
    ) {
        int[] base = idsBase(block);
        int[] olb = idsOlb(block);
        int[] olw = idsOlw(block);

        hideAll(widgetPreviewInclude, base);
        hideAll(widgetPreviewInclude, olb);
        hideAll(widgetPreviewInclude, olw);

        int targetId = !outline ? base[styleIndex] : (outlineWhite ? olw[styleIndex] : olb[styleIndex]);

        View v = widgetPreviewInclude.findViewById(targetId);
        if (v instanceof TextView) {
            TextView tv = (TextView) v;
            tv.setVisibility(View.VISIBLE);
            tv.setTextColor(color);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        }
    }

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

    private void refreshWidgetDebouncedSafe() {
        if (refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }

        refreshRunnable = () -> {
            try {
                VerseWidgetProvider.updateAllWidgets(getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        handler.postDelayed(refreshRunnable, 120);
    }

    private void setupBottomNav() {
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_widget);
        bottomNav.setOnItemReselectedListener(item -> { });

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

    private void setTextForAllVariants(View root, int[] base, int[] olb, int[] olw, String text) {
        if (root == null) return;
        if (text == null) text = "";

        for (int id : base) {
            View v = root.findViewById(id);
            if (v instanceof TextView) ((TextView) v).setText(text);
        }
        for (int id : olb) {
            View v = root.findViewById(id);
            if (v instanceof TextView) ((TextView) v).setText(text);
        }
        for (int id : olw) {
            View v = root.findViewById(id);
            if (v instanceof TextView) ((TextView) v).setText(text);
        }
    }

    private void hideAll(View root, int[] ids) {
        if (root == null) return;
        for (int id : ids) {
            View v = root.findViewById(id);
            if (v != null) v.setVisibility(View.GONE);
        }
    }

    private int[] idsBase(Block b) {
        switch (b) {
            case TONE:
                return new int[]{R.id.textTone_normal, R.id.textTone_bold, R.id.textTone_italic, R.id.textTone_bold_italic};
            case VERSE:
                return new int[]{R.id.textVerse_normal, R.id.textVerse_bold, R.id.textVerse_italic, R.id.textVerse_bold_italic};
            case REF:
            default:
                return new int[]{R.id.textRef_normal, R.id.textRef_bold, R.id.textRef_italic, R.id.textRef_bold_italic};
        }
    }

    private int[] idsOlb(Block b) {
        switch (b) {
            case TONE:
                return new int[]{R.id.textTone_normal_olb, R.id.textTone_bold_olb, R.id.textTone_italic_olb, R.id.textTone_bold_italic_olb};
            case VERSE:
                return new int[]{R.id.textVerse_normal_olb, R.id.textVerse_bold_olb, R.id.textVerse_italic_olb, R.id.textVerse_bold_italic_olb};
            case REF:
            default:
                return new int[]{R.id.textRef_normal_olb, R.id.textRef_bold_olb, R.id.textRef_italic_olb, R.id.textRef_bold_italic_olb};
        }
    }

    private int[] idsOlw(Block b) {
        switch (b) {
            case TONE:
                return new int[]{R.id.textTone_normal_olw, R.id.textTone_bold_olw, R.id.textTone_italic_olw, R.id.textTone_bold_italic_olw};
            case VERSE:
                return new int[]{R.id.textVerse_normal_olw, R.id.textVerse_bold_olw, R.id.textVerse_italic_olw, R.id.textVerse_bold_italic_olw};
            case REF:
            default:
                return new int[]{R.id.textRef_normal_olw, R.id.textRef_bold_olw, R.id.textRef_italic_olw, R.id.textRef_bold_italic_olw};
        }
    }

    private boolean isDarkColor(int color) {
        double r = Color.red(color) / 255.0;
        double g = Color.green(color) / 255.0;
        double b = Color.blue(color) / 255.0;
        double lum = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        return lum < 0.45;
    }
}