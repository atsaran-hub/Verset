package com.example.verset.widgetcustome;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.example.verset.Prefs;
import com.example.verset.R;
import com.example.verset.db.AppDatabase;
import com.example.verset.db.ContentItemEntity;
import com.example.verset.db.SeedImporter;

public class VerseWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_WIDGET_STATE_CHANGED = "com.example.verset.ACTION_WIDGET_STATE_CHANGED";
    public static final String EXTRA_HAS_WIDGET = "extra_has_widget";

    private static final String SP_TEXT = "widget_text_prefs";
    private static final String K_COLOR = "text_color";
    private static final String K_BOLD = "text_bold";
    private static final String K_ITALIC = "text_italic";
    private static final String K_OUTLINE = "text_outline";
    private static final String K_TEXT_SIZE_SP = "text_size_sp";
    private static final float DEFAULT_TEXT_SIZE_SP = 14f;

    private static final int WIDGET_LAYOUT = R.layout.widget_verse;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) updateAppWidget(context, appWidgetManager, id);
        broadcastWidgetState(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        broadcastWidgetState(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        broadcastWidgetState(context);
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        ComponentName cn = new ComponentName(context, VerseWidgetProvider.class);
        int[] ids = mgr.getAppWidgetIds(cn);

        if (ids != null) {
            for (int id : ids) updateAppWidget(context, mgr, id);
        }
        broadcastWidgetState(context);
    }

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), WIDGET_LAYOUT);

        // =========================
        // ✅ 1) On cache TOUT le "TONE" (donc plus de "soft")
        // =========================
        hideAll(views, idsBase(Block.TONE));
        hideAll(views, idsOlb(Block.TONE));
        hideAll(views, idsOlw(Block.TONE));

        // =========================
        // ✅ 2) On récupère verset + ref depuis la DB
        // =========================
        ContentItemEntity verse = null;
        try {
            SeedImporter.ensureSeeded(context);
            verse = AppDatabase.getInstance(context).contentDao().getRandomVerse("en");
        } catch (Exception ignored) {}

        String verseText = (verse != null && verse.text != null) ? verse.text : "—";
        String refText   = (verse != null && verse.reference != null) ? verse.reference : "";

        // On met le texte sur TOUTES les variantes (normal/bold/italic/outlines)
        setTextForAllVariants(views, idsBase(Block.VERSE), idsOlb(Block.VERSE), idsOlw(Block.VERSE), verseText);
        setTextForAllVariants(views, idsBase(Block.REF),   idsOlb(Block.REF),   idsOlw(Block.REF),   refText);

        // =========================
        // ✅ 3) Theme BG
        // =========================
        String themeId = Prefs.getThemeId(context);
        int bgRes = resolveThemeBgRes(context, themeId);
        views.setImageViewResource(R.id.bgImage, bgRes);

        // =========================
        // ✅ 4) Styles (bold/italic/outline + taille)
        // =========================
        int fallbackColor = Prefs.getThemeTextColor(context);

        int color = context.getSharedPreferences(SP_TEXT, Context.MODE_PRIVATE)
                .getInt(K_COLOR, fallbackColor);

        boolean bold = context.getSharedPreferences(SP_TEXT, Context.MODE_PRIVATE)
                .getBoolean(K_BOLD, false);

        boolean italic = context.getSharedPreferences(SP_TEXT, Context.MODE_PRIVATE)
                .getBoolean(K_ITALIC, false);

        boolean outline = context.getSharedPreferences(SP_TEXT, Context.MODE_PRIVATE)
                .getBoolean(K_OUTLINE, false);

        float baseSp = context.getSharedPreferences(SP_TEXT, Context.MODE_PRIVATE)
                .getFloat(K_TEXT_SIZE_SP, DEFAULT_TEXT_SIZE_SP);

        float verseSp = baseSp;
        float refSp = Math.max(8f, baseSp - 3f);

        int styleIndex = (bold ? 1 : 0) + (italic ? 2 : 0); // 0..3
        boolean outlineWhite = outline && isDarkColor(color);

        // ⚠️ On n’applique PLUS rien au TONE (puisqu’il est supprimé)
        applyBlock(views, Block.VERSE, styleIndex, outline, outlineWhite, color, verseSp);
        applyBlock(views, Block.REF, styleIndex, outline, outlineWhite, color, refSp);

        // =========================
        // ✅ 5) Click -> open app
        // =========================
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (launchIntent != null) {
            PendingIntent pi = PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.widgetRoot, pi);
            views.setOnClickPendingIntent(R.id.bgImage, pi);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // =========================
    // Helpers
    // =========================

    private static void setTextForAllVariants(RemoteViews views, int[] base, int[] olb, int[] olw, String text) {
        if (text == null) text = "";
        for (int id : base) views.setTextViewText(id, text);
        for (int id : olb)  views.setTextViewText(id, text);
        for (int id : olw)  views.setTextViewText(id, text);
    }

    private enum Block { TONE, VERSE, REF }

    private static void applyBlock(RemoteViews views,
                                   Block block,
                                   int styleIndex,
                                   boolean outline,
                                   boolean outlineWhite,
                                   int textColor,
                                   float sizeSp) {

        int[] base = idsBase(block);
        int[] olb  = idsOlb(block);
        int[] olw  = idsOlw(block);

        hideAll(views, base);
        hideAll(views, olb);
        hideAll(views, olw);

        int showId;
        if (!outline) {
            showId = pickByStyle(styleIndex, base);
        } else if (outlineWhite) {
            showId = pickByStyle(styleIndex, olw);
        } else {
            showId = pickByStyle(styleIndex, olb);
        }

        views.setViewVisibility(showId, View.VISIBLE);
        views.setTextColor(showId, textColor);
        views.setTextViewTextSize(showId, TypedValue.COMPLEX_UNIT_SP, sizeSp);
    }

    private static void hideAll(RemoteViews views, int[] ids) {
        for (int id : ids) views.setViewVisibility(id, View.GONE);
    }

    private static int pickByStyle(int styleIndex, int[] arr4) {
        if (styleIndex == 1) return arr4[1];
        if (styleIndex == 2) return arr4[2];
        if (styleIndex == 3) return arr4[3];
        return arr4[0];
    }

    private static int[] idsBase(Block b) {
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

    private static int[] idsOlb(Block b) {
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

    private static int[] idsOlw(Block b) {
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

    private static int resolveThemeBgRes(Context context, String themeId) {
        if (themeId == null || themeId.trim().isEmpty()) themeId = "dawn";
        String name = "theme_bg_" + themeId;

        int resId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        if (resId == 0) {
            resId = context.getResources().getIdentifier("theme_bg_dawn", "drawable", context.getPackageName());
        }
        if (resId == 0) resId = android.R.color.transparent;

        return resId;
    }

    private static void broadcastWidgetState(Context context) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        ComponentName cn = new ComponentName(context, VerseWidgetProvider.class);
        int[] ids = mgr.getAppWidgetIds(cn);
        boolean has = ids != null && ids.length > 0;

        Intent i = new Intent(ACTION_WIDGET_STATE_CHANGED);
        i.putExtra(EXTRA_HAS_WIDGET, has);
        context.sendBroadcast(i);
    }

    private static boolean isDarkColor(int color) {
        double r = Color.red(color) / 255.0;
        double g = Color.green(color) / 255.0;
        double b = Color.blue(color) / 255.0;
        double lum = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        return lum < 0.45;
    }
}