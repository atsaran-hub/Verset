package com.example.verset;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Prefs {

    public static final String PREFS_NAME = "user_prefs";

    // ---------- BASIC USER INFO ----------
    public static final String KEY_NAME = "name";
    public static final String KEY_AGE = "age";
    public static final String KEY_GENDER = "gender";

    // ---------- GOALS / THEMES ----------
    public static final String KEY_GOALS = "goals"; // Set<String>
    public static final String KEY_VERSE_THEMES = "verse_themes"; // Set<String>

    // ------------------------------
    // Recueillement Frequency
    // ------------------------------
    private static final String KEY_RECUILLEMENT_FREQUENCY = "recueillement_frequency";

    public static void saveRecueillementFrequency(Context c, String value) {
        if (value == null) value = "";
        sp(c).edit().putString(KEY_RECUILLEMENT_FREQUENCY, value).apply();
    }
    // ---------- APPEARANCE (Light / Dark / System) ----------
    public static final String KEY_APPEARANCE_MODE = "appearance_mode"; // "light" / "dark" / "system"

    public static String getRecueillementFrequency(Context c) {
        return sp(c).getString(KEY_RECUILLEMENT_FREQUENCY, "");
    }
    // ===== Devotion reminder =====
    private static final String KEY_DEVOTION_ENABLED = "devotion_enabled";
    private static final String KEY_DEVOTION_DAYS_MASK = "devotion_days_mask"; // bitmask Mon..Sun
    private static final String KEY_DEVOTION_HOUR = "devotion_hour";
    private static final String KEY_DEVOTION_MIN = "devotion_min";

    public static void setDevotionEnabled(Context c, boolean enabled) {
        sp(c).edit().putBoolean(KEY_DEVOTION_ENABLED, enabled).apply();
    }
    public static boolean isDevotionEnabled(Context c) {
        return sp(c).getBoolean(KEY_DEVOTION_ENABLED, false);
    }
    public static void saveDevotionSchedule(Context c, int daysMask, int hour, int min) {
        sp(c).edit()
                .putInt(KEY_DEVOTION_DAYS_MASK, daysMask)
                .putInt(KEY_DEVOTION_HOUR, hour)
                .putInt(KEY_DEVOTION_MIN, min)
                .apply();
    }
    public static int getDevotionDaysMask(Context c) {
        return sp(c).getInt(KEY_DEVOTION_DAYS_MASK, 0); // 0 = none
    }
    public static int getDevotionHour(Context c) {
        return sp(c).getInt(KEY_DEVOTION_HOUR, 8);
    }
    public static int getDevotionMin(Context c) {
        return sp(c).getInt(KEY_DEVOTION_MIN, 0);
    }

    // ------------------------------
    // Streak Goal + Streak Tracking
    // ------------------------------
    private static final String KEY_STREAK_GOAL_DAYS = "streak_goal_days";
    private static final String KEY_STREAK_CURRENT = "streak_current";
    private static final String KEY_STREAK_BEST = "streak_best";
    private static final String KEY_LAST_ACTIVE_EPOCH_DAY = "last_active_epoch_day";

    public static void saveStreakGoalDays(Context c, int days) {
        if (days < 1) days = 7;
        sp(c).edit().putInt(KEY_STREAK_GOAL_DAYS, days).apply();
    }

    public static int getStreakGoalDays(Context c) {
        return sp(c).getInt(KEY_STREAK_GOAL_DAYS, 7);
    }

    public static int getCurrentStreak(Context c) {
        return sp(c).getInt(KEY_STREAK_CURRENT, 0);
    }

    public static int getBestStreak(Context c) {
        return sp(c).getInt(KEY_STREAK_BEST, 0);
    }

    public static long getLastActiveEpochDay(Context c) {
        return sp(c).getLong(KEY_LAST_ACTIVE_EPOCH_DAY, -1L);
    }
    public static void setAppearanceMode(Context c, String mode) {
        if (mode == null) mode = "system";
        sp(c).edit().putString(KEY_APPEARANCE_MODE, mode).apply();
    }

    public static String getAppearanceMode(Context c) {
        return sp(c).getString(KEY_APPEARANCE_MODE, "system");
    }

    /**
     * Call this once per app open (e.g., in MainActivity.onCreate/onResume)
     * It updates current streak + best streak based on date.
     */
    public static void markActiveToday(Context c) {
        long today = java.time.LocalDate.now().toEpochDay();
        long last = getLastActiveEpochDay(c);

        int current = getCurrentStreak(c);
        int best = getBestStreak(c);

        if (last == today) {
            // already counted today
            return;
        }

        if (last == today - 1) {
            // consecutive day
            current = current + 1;
        } else {
            // break in streak (or first time)
            current = 1;
        }

        if (current > best) best = current;

        sp(c).edit()
                .putLong(KEY_LAST_ACTIVE_EPOCH_DAY, today)
                .putInt(KEY_STREAK_CURRENT, current)
                .putInt(KEY_STREAK_BEST, best)
                .apply();
    }

    // ------------------------------
    // Tradition
    // ------------------------------
    private static final String KEY_TRADITION = "tradition";

    public static void saveTradition(Context c, String value) {
        if (value == null) value = "";
        sp(c).edit().putString(KEY_TRADITION, value).apply();
    }

    public static String getTradition(Context c) {
        return sp(c).getString(KEY_TRADITION, "");
    }

    // ------------------------------
    // Premium + Daily verses
    // ------------------------------
    private static final String KEY_IS_PREMIUM = "is_premium";
    private static final String KEY_DAILY_VERSES_APPLIED = "daily_verses_applied";   // applied
    private static final String KEY_DAILY_VERSES_DESIRED = "daily_verses_desired";   // desired

    public static void setPremium(Context c, boolean value) {
        sp(c).edit().putBoolean(KEY_IS_PREMIUM, value).apply();
    }

    public static boolean isPremium(Context c) {
        return sp(c).getBoolean(KEY_IS_PREMIUM, false);
    }

    public static void saveDailyVersesDesired(Context c, int value) {
        if (value < 1) value = 1;
        sp(c).edit().putInt(KEY_DAILY_VERSES_DESIRED, value).apply();
    }

    public static int getDailyVersesDesired(Context c) {
        return sp(c).getInt(KEY_DAILY_VERSES_DESIRED, 1);
    }

    public static void saveDailyVersesApplied(Context c, int value) {
        if (value < 1) value = 1;
        sp(c).edit().putInt(KEY_DAILY_VERSES_APPLIED, value).apply();
    }

    public static int getDailyVersesApplied(Context c) {
        return sp(c).getInt(KEY_DAILY_VERSES_APPLIED, 1);
    }

    public static void syncDailyVersesWithEntitlement(Context c) {
        int desired = getDailyVersesDesired(c);
        if (isPremium(c)) {
            saveDailyVersesApplied(Math.max(1, desired), c);
        } else {
            saveDailyVersesApplied(1, c);
        }
    }

    // helper overload to keep calls stable if you had different order somewhere
    private static void saveDailyVersesApplied(int value, Context c) {
        saveDailyVersesApplied(c, value);
    }

    // ---------- THEME ----------
    public static final String KEY_THEME_ID = "theme_id";
    public static final String KEY_THEME_TEXT_COLOR = "theme_text_color";
    public static final String KEY_THEME_CARD_BG = "theme_card_bg";
    public static final String KEY_THEME_CUSTOM_BG_COLOR = "theme_custom_bg_color";

    // ---------- TONE ----------
    public static final String KEY_TONE = "tone";

    // ---------- VERSES PER DAY ----------
    public static final String KEY_VERSES_PER_DAY = "verses_per_day";

    // ---------- NOTIFICATIONS ----------
    public static final String KEY_NOTIF_SLOT = "notif_slot";
    public static final String KEY_NOTIF_HOUR = "notif_hour";
    public static final String KEY_NOTIF_MIN = "notif_min";

    // ---------- STREAK (legacy) ----------
    public static final String KEY_STREAK = "streak_days";

    // ---------- WIDGET CUSTOM BG ----------
    public static final String KEY_WIDGET_USE_CUSTOM_BG = "widget_use_custom_bg";
    public static final String KEY_WIDGET_CUSTOM_BG_COLOR = "widget_custom_bg_color";

    // ---------- WIDGET TEXT SCALE ----------
    public static final String KEY_WIDGET_TEXT_SCALE = "widget_text_scale";

    // ---------- WIDGET TEXT SIZES ----------
    public static final String KEY_WIDGET_TONE_SP  = "widget_tone_sp";
    public static final String KEY_WIDGET_VERSE_SP = "widget_verse_sp";
    public static final String KEY_WIDGET_REF_SP   = "widget_ref_sp";

    // ---------- WIDGET TEXT COLORS ----------
    public static final String KEY_WIDGET_TONE_COLOR  = "widget_tone_color";
    public static final String KEY_WIDGET_VERSE_COLOR = "widget_verse_color";
    public static final String KEY_WIDGET_REF_COLOR   = "widget_ref_color";

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // =====================================================
    // NAME
    // =====================================================
    public static void saveName(Context c, String name) {
        if (name == null) name = "";
        sp(c).edit().putString(KEY_NAME, name.trim()).apply();
    }

    public static String getName(Context c) {
        return sp(c).getString(KEY_NAME, "");
    }

    // =====================================================
    // AGE
    // =====================================================
    public static void saveAge(Context c, String ageRange) {
        if (ageRange == null) ageRange = "";
        sp(c).edit().putString(KEY_AGE, ageRange).apply();
    }

    public static String getAge(Context c) {
        return sp(c).getString(KEY_AGE, "");
    }

    // =====================================================
    // GENDER
    // =====================================================
    public static void saveGender(Context c, String value) {
        if (value == null) value = "";
        sp(c).edit().putString(KEY_GENDER, value).apply();
    }

    public static String getGender(Context c) {
        return sp(c).getString(KEY_GENDER, "");
    }

    // =====================================================
    // Relationship status
    // =====================================================
    private static final String KEY_RELATIONSHIP_STATUS = "relationship_status";

    public static void saveRelationshipStatus(Context c, String value) {
        if (value == null) value = "";
        sp(c).edit().putString(KEY_RELATIONSHIP_STATUS, value).apply();
    }

    public static String getRelationshipStatus(Context c) {
        return sp(c).getString(KEY_RELATIONSHIP_STATUS, "");
    }

    // =====================================================
    // GOALS
    // =====================================================
    public static void saveGoals(Context c, Set<String> goals) {
        if (goals == null) goals = new HashSet<>();
        sp(c).edit().putStringSet(KEY_GOALS, new HashSet<>(goals)).apply();
    }

    public static Set<String> getGoals(Context c) {
        return new HashSet<>(sp(c).getStringSet(KEY_GOALS, new HashSet<>()));
    }

    // =====================================================
    // VERSE THEMES
    // =====================================================
    public static void saveVerseThemes(Context c, Set<String> themes) {
        if (themes == null) themes = new HashSet<>();
        sp(c).edit().putStringSet(KEY_VERSE_THEMES, new HashSet<>(themes)).apply();
    }

    public static Set<String> getVerseThemes(Context c) {
        return new HashSet<>(sp(c).getStringSet(KEY_VERSE_THEMES, new HashSet<>()));
    }

    public static String pickRandomVerseTheme(Context c) {
        Set<String> themes = getVerseThemes(c);
        if (themes == null || themes.isEmpty()) return null;

        ArrayList<String> list = new ArrayList<>(themes);
        return list.get(new Random().nextInt(list.size()));
    }

    // =====================================================
    // THEME
    // =====================================================
    public static void saveTheme(Context c, String themeId, int textColor, int cardBgRes) {
        if (themeId == null) themeId = "dawn";
        sp(c).edit()
                .putString(KEY_THEME_ID, themeId)
                .putInt(KEY_THEME_TEXT_COLOR, textColor)
                .putInt(KEY_THEME_CARD_BG, cardBgRes)
                .apply();
    }

    public static String getThemeId(Context c) {
        return sp(c).getString(KEY_THEME_ID, "dawn");
    }

    public static int getThemeTextColor(Context c) {
        return sp(c).getInt(KEY_THEME_TEXT_COLOR, 0xFF1F1F1F);
    }

    public static int getThemeCardBg(Context c) {
        return sp(c).getInt(KEY_THEME_CARD_BG, R.drawable.theme_bg_dawn);
    }

    public static void saveCustomThemeBgColor(Context c, int color) {
        sp(c).edit().putInt(KEY_THEME_CUSTOM_BG_COLOR, color).apply();
    }

    public static int getCustomThemeBgColor(Context c, int fallback) {
        return sp(c).getInt(KEY_THEME_CUSTOM_BG_COLOR, fallback);
    }

    public static boolean isCustomTheme(Context c) {
        return "custom".equals(getThemeId(c));
    }

    // =====================================================
    // TONE
    // =====================================================
    public static void saveTone(Context c, String tone) {
        if (tone == null) tone = "calm";
        sp(c).edit().putString(KEY_TONE, tone).apply();
    }

    public static String getTone(Context c) {
        return sp(c).getString(KEY_TONE, "calm");
    }

    // =====================================================
    // VERSES PER DAY (legacy)
    // =====================================================
    public static void saveVersesPerDay(Context c, int count) {
        if (count < 1) count = 1;
        if (count > 5) count = 5;
        sp(c).edit().putInt(KEY_VERSES_PER_DAY, count).apply();
    }

    public static int getVersesPerDay(Context c) {
        return sp(c).getInt(KEY_VERSES_PER_DAY, 1);
    }

    // =====================================================
    // NOTIFICATIONS
    // =====================================================
    public static void saveNotifTime(Context c, String slot, int hour, int min) {
        sp(c).edit()
                .putString(KEY_NOTIF_SLOT, slot)
                .putInt(KEY_NOTIF_HOUR, hour)
                .putInt(KEY_NOTIF_MIN, min)
                .apply();
    }

    public static String getNotifSlot(Context c) {
        return sp(c).getString(KEY_NOTIF_SLOT, null);
    }

    public static int getNotifHour(Context c) {
        return sp(c).getInt(KEY_NOTIF_HOUR, 8);
    }

    public static int getNotifMin(Context c) {
        return sp(c).getInt(KEY_NOTIF_MIN, 0);
    }

    public static boolean isNotifEnabled(Context c) {
        String slot = getNotifSlot(c);
        return slot != null && !slot.equals("none");
    }

    // =====================================================
    // STREAK (legacy)
    // =====================================================
    public static int getStreak(Context c) {
        return sp(c).getInt(KEY_STREAK, 0);
    }

    public static void setStreak(Context c, int days) {
        sp(c).edit().putInt(KEY_STREAK, days).apply();
    }

    // =====================================================
    // WIDGET CUSTOM BG
    // =====================================================
    public static void setWidgetCustomBg(Context c, boolean enabled, int color) {
        sp(c).edit()
                .putBoolean(KEY_WIDGET_USE_CUSTOM_BG, enabled)
                .putInt(KEY_WIDGET_CUSTOM_BG_COLOR, color)
                .apply();
    }

    public static boolean isWidgetCustomBgEnabled(Context c) {
        return sp(c).getBoolean(KEY_WIDGET_USE_CUSTOM_BG, false);
    }

    public static int getWidgetCustomBgColor(Context c) {
        return sp(c).getInt(KEY_WIDGET_CUSTOM_BG_COLOR, 0xFFFFF8EF);
    }

    // =====================================================
    // WIDGET TEXT SCALE
    // =====================================================
    public static void saveWidgetTextScale(Context c, float scale) {
        if (scale < 0.85f) scale = 0.85f;
        if (scale > 1.35f) scale = 1.35f;
        sp(c).edit().putFloat(KEY_WIDGET_TEXT_SCALE, scale).apply();
    }

    public static float getWidgetTextScale(Context c) {
        return sp(c).getFloat(KEY_WIDGET_TEXT_SCALE, 1.0f);
    }

    // =====================================================
    // WIDGET TEXT SIZES
    // =====================================================
    public static void saveWidgetTextSizes(Context c, int toneSp, int verseSp, int refSp) {
        toneSp = clampInt(toneSp, 10, 18);
        verseSp = clampInt(verseSp, 12, 22);
        refSp = clampInt(refSp, 9, 16);

        sp(c).edit()
                .putInt(KEY_WIDGET_TONE_SP, toneSp)
                .putInt(KEY_WIDGET_VERSE_SP, verseSp)
                .putInt(KEY_WIDGET_REF_SP, refSp)
                .apply();
    }

    public static int getWidgetToneSp(Context c) {
        return sp(c).getInt(KEY_WIDGET_TONE_SP, 12);
    }

    public static int getWidgetVerseSp(Context c) {
        return sp(c).getInt(KEY_WIDGET_VERSE_SP, 16);
    }

    public static int getWidgetRefSp(Context c) {
        return sp(c).getInt(KEY_WIDGET_REF_SP, 11);
    }

    // =====================================================
    // WIDGET TEXT COLORS
    // =====================================================
    public static void saveWidgetTextColors(Context c, int toneColor, int verseColor, int refColor) {
        sp(c).edit()
                .putInt(KEY_WIDGET_TONE_COLOR, toneColor)
                .putInt(KEY_WIDGET_VERSE_COLOR, verseColor)
                .putInt(KEY_WIDGET_REF_COLOR, refColor)
                .apply();
    }

    public static int getWidgetToneColor(Context c, int fallback) {
        return sp(c).getInt(KEY_WIDGET_TONE_COLOR, fallback);
    }

    public static int getWidgetVerseColor(Context c, int fallback) {
        return sp(c).getInt(KEY_WIDGET_VERSE_COLOR, fallback);
    }

    public static int getWidgetRefColor(Context c, int fallback) {
        return sp(c).getInt(KEY_WIDGET_REF_COLOR, fallback);
    }

    // =====================================================
    // Selected Books (Set<String>)  (kept as-is)
    // =====================================================
    private static final String KEY_SELECTED_BOOKS = "selected_books";
    private static final String BOOK_ALL = "__ALL__";

    public static void saveSelectedBooks(Context context, Set<String> books) {
        context.getSharedPreferences("verset_prefs", Context.MODE_PRIVATE)
                .edit()
                .putStringSet(KEY_SELECTED_BOOKS, books)
                .apply();
    }

    public static Set<String> getSelectedBooks(Context context) {
        Set<String> saved = context
                .getSharedPreferences("verset_prefs", Context.MODE_PRIVATE)
                .getStringSet(KEY_SELECTED_BOOKS, null);

        if (saved == null || saved.isEmpty()) {
            Set<String> def = new HashSet<>();
            def.add(BOOK_ALL);
            return def;
        }

        return new HashSet<>(saved);
    }

    // =====================================================
    private static int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
