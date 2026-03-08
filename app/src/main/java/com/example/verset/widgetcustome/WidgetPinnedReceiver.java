package com.example.verset.widgetcustome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WidgetPinnedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Rien à stocker.
        // Le bouton se mettra à jour via isWidgetAdded() dans onResume().
    }
}