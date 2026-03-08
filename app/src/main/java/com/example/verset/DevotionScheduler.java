package com.example.verset;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

/**
 * Programme / annule les alarmes de rappel "Moment avec Dieu"
 * en utilisant ton Prefs :
 * - Prefs.isDevotionEnabled()
 * - Prefs.getDevotionDaysMask()  (bit0=Mon ... bit6=Sun)
 * - Prefs.getDevotionHour()
 * - Prefs.getDevotionMin()
 */
public final class DevotionScheduler {

    // requestCode unique par jour (Mon..Sun)
    private static final int REQ_BASE = 7300;
    private static final String ACTION_DEVOTION = "DEVOTION_REMINDER";

    private DevotionScheduler() {}

    /** Appelle ça après avoir sauvegardé Prefs (bouton Activer, ou après modification). */
    public static void scheduleAllSelectedDays(Context context) {
        cancelAll(context); // évite doublons

        if (!Prefs.isDevotionEnabled(context)) return;

        int mask = Prefs.getDevotionDaysMask(context);
        if (mask == 0) return;

        int hour = Prefs.getDevotionHour(context);
        int min  = Prefs.getDevotionMin(context);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        // bit0=Mon ... bit6=Sun
        for (int bit = 0; bit < 7; bit++) {
            if ((mask & (1 << bit)) == 0) continue;

            int targetDayOfWeek = bitToCalendarDay(bit);
            long triggerAtMillis = computeNextTriggerMillis(targetDayOfWeek, hour, min);

            PendingIntent pi = buildPendingIntent(context, bit);
            setExact(am, triggerAtMillis, pi);
        }
    }

    /** Annule toutes les alarmes (Mon..Sun). */
    public static void cancelAll(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        for (int bit = 0; bit < 7; bit++) {
            PendingIntent pi = buildPendingIntent(context, bit);
            am.cancel(pi);
        }
    }

    // -------------------- helpers --------------------

    private static PendingIntent buildPendingIntent(Context context, int bit) {
        Intent i = new Intent(context, DevotionReminderReceiver.class);
        i.setAction(ACTION_DEVOTION);
        i.putExtra("day_bit", bit); // debug/trace si tu veux

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getBroadcast(
                context,
                REQ_BASE + bit,
                i,
                flags
        );
    }

    private static void setExact(AlarmManager am, long triggerAtMillis, PendingIntent pi) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        }
    }

    /**
     * Convertit bitmask -> Calendar.DAY_OF_WEEK
     * bit0=Mon, bit1=Tue, ..., bit5=Sat, bit6=Sun
     */
    private static int bitToCalendarDay(int bit) {
        switch (bit) {
            case 0: return Calendar.MONDAY;
            case 1: return Calendar.TUESDAY;
            case 2: return Calendar.WEDNESDAY;
            case 3: return Calendar.THURSDAY;
            case 4: return Calendar.FRIDAY;
            case 5: return Calendar.SATURDAY;
            case 6: return Calendar.SUNDAY;
            default: return Calendar.MONDAY;
        }
    }

    /**
     * Calcule la prochaine occurrence du jour+heure.
     * Si le jour est aujourd'hui mais l'heure est déjà passée -> semaine suivante.
     */
    private static long computeNextTriggerMillis(int targetDayOfWeek, int hour, int minute) {
        Calendar now = Calendar.getInstance();

        Calendar next = Calendar.getInstance();
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, minute);

        int today = now.get(Calendar.DAY_OF_WEEK);

        int daysUntil = (targetDayOfWeek - today + 7) % 7;

        // aujourd'hui mais déjà passé => +7 jours
        if (daysUntil == 0 && next.getTimeInMillis() <= now.getTimeInMillis()) {
            daysUntil = 7;
        }

        next.add(Calendar.DAY_OF_YEAR, daysUntil);
        return next.getTimeInMillis();
    }
}
