package com.example.verset.widgetcustome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class WidgetPinnedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Widget ajouté à l’accueil ✅", Toast.LENGTH_SHORT).show();
    }
}