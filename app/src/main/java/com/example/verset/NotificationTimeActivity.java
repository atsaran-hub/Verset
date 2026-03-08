package com.example.verset;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationTimeActivity extends AppCompatActivity {

    private LinearLayout optionMorning, optionAfternoon, optionEvening, optionAnytime;
    private LinearLayout optionNone; // optionnel si tu ajoutes "No notification"

    private ImageView radioMorning, radioAfternoon, radioEvening, radioAnytime;
    private ImageView radioNone; // optionnel

    private String selectedSlot = null;
    private int selectedHour = 8;
    private int selectedMin = 0;

    private final int drawableUnchecked = R.drawable.radio_orange_unchecked;
    private int drawableChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Options
        optionMorning = findViewById(R.id.optionMorning);
        optionAfternoon = findViewById(R.id.optionAfternoon);
        optionEvening = findViewById(R.id.optionEvening);
        optionAnytime = findViewById(R.id.optionAnytime);

        // Radios
        radioMorning = findViewById(R.id.radioMorning);
        radioAfternoon = findViewById(R.id.radioAfternoon);
        radioEvening = findViewById(R.id.radioEvening);
        radioAnytime = findViewById(R.id.radioAnytime);

        // ✅ checked drawable safe
        drawableChecked = getResources().getIdentifier("radio_orange_checked", "drawable", getPackageName());
        if (drawableChecked == 0) drawableChecked = drawableUnchecked;

        // ✅ Option "No notification" (si tu l'ajoutes au XML)
        optionNone = findOptionalLinearLayout("optionNone");
        radioNone = findOptionalImageView("radioNone");

        // ✅ Restore depuis Prefs
        selectedSlot = Prefs.getNotifSlot(this);
        selectedHour = Prefs.getNotifHour(this);
        selectedMin = Prefs.getNotifMin(this);

        if (selectedSlot != null && selectedSlot.trim().isEmpty()) selectedSlot = null;

        refreshRadios();

        // Click listeners (save immédiatement => overwrite)
        optionMorning.setOnClickListener(v -> selectSlot("morning", 8, 0));
        optionAfternoon.setOnClickListener(v -> selectSlot("afternoon", 18, 0));
        optionEvening.setOnClickListener(v -> selectSlot("evening", 21, 0));
        optionAnytime.setOnClickListener(v -> selectSlot("anytime", 12, 0));

        if (optionNone != null) {
            optionNone.setOnClickListener(v -> selectSlot("none", 0, 0));
        }

        // Next
        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(v -> {
            if (selectedSlot == null) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
                return;
            }

            Prefs.saveNotifTime(this, selectedSlot, selectedHour, selectedMin);

            startActivity(new Intent(NotificationTimeActivity.this, WidgetReadyActivity.class));
        });
    }

    private void selectSlot(String slot, int hour, int min) {
        selectedSlot = slot;
        selectedHour = hour;
        selectedMin = min;

        Prefs.saveNotifTime(this, slot, hour, min); // overwrite
        refreshRadios();
    }

    private void refreshRadios() {
        radioMorning.setImageResource(isSelected("morning") ? drawableChecked : drawableUnchecked);
        radioAfternoon.setImageResource(isSelected("afternoon") ? drawableChecked : drawableUnchecked);
        radioEvening.setImageResource(isSelected("evening") ? drawableChecked : drawableUnchecked);
        radioAnytime.setImageResource(isSelected("anytime") ? drawableChecked : drawableUnchecked);

        if (radioNone != null) {
            radioNone.setImageResource(isSelected("none") ? drawableChecked : drawableUnchecked);
        }
    }

    private boolean isSelected(String slot) {
        return selectedSlot != null && selectedSlot.equals(slot);
    }

    // ✅ helpers pour find des views optionnelles par nom (si pas dans le XML => null)
    private LinearLayout findOptionalLinearLayout(String idName) {
        int id = getResources().getIdentifier(idName, "id", getPackageName());
        return id == 0 ? null : findViewById(id);
    }

    private ImageView findOptionalImageView(String idName) {
        int id = getResources().getIdentifier(idName, "id", getPackageName());
        return id == 0 ? null : findViewById(id);
    }
}
