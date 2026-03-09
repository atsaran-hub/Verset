package com.example.verset;

import android.os.Bundle;
import android.view.View;

import com.example.verset.widgetcustome.widgetcustom;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class WidgetCustomizeActivity extends widgetcustom {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }

        View btnAddWidget = findViewById(R.id.btnAddWidget);
        if (btnAddWidget != null) {
            btnAddWidget.setVisibility(View.VISIBLE);
        }
    }
}