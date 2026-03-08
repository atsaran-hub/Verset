package com.example.verset;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class DevotionReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "devotion_reminders";
    private static final int NOTIF_ID = 9901;

    @Override
    public void onReceive(Context context, Intent intent) {

        // Si désactivé -> on annule toutes les alarmes et stop
        if (!Prefs.isDevotionEnabled(context)) {
            DevotionScheduler.cancelAll(context);
            return;
        }



        // Click notif -> ouvre MainActivity
        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            piFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                open,
                piFlags
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // idéalement une icône notif dédiée
                .setContentTitle("Moment avec Dieu")
                .setContentText("Prends 2 minutes pour lire un verset et une phrase pour toi.")
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify(NOTIF_ID, b.build());

        // IMPORTANT: comme c'est du setExact -> on reprogramme pour les prochains jours
        DevotionScheduler.scheduleAllSelectedDays(context);
    }
}
