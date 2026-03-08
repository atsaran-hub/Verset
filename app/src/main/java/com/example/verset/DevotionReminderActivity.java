package com.example.verset;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.Calendar;
import java.util.Locale;

public class DevotionReminderActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton buttonEnable;

    private MaterialCardView timeRow;
    private TextView textTime;

    private Chip chipMon, chipTue, chipWed, chipThu, chipFri, chipSat, chipSun;

    private int hour = 8;
    private int minute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devotion_reminder);

        bindViews();
        setupChipsUX();

        // Load saved schedule
        hour = Prefs.getDevotionHour(this);
        minute = Prefs.getDevotionMin(this);

        textTime.setText(formatTime(hour, minute));
        restoreDayChips();

        btnBack.setOnClickListener(v -> finish());
        timeRow.setOnClickListener(v -> openTimePicker());

        buttonEnable.setOnClickListener(v -> onEnableClicked());
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        buttonEnable = findViewById(R.id.buttonEnable);

        timeRow = findViewById(R.id.timeRow);
        textTime = findViewById(R.id.textTime);

        chipMon = findViewById(R.id.chipMon);
        chipTue = findViewById(R.id.chipTue);
        chipWed = findViewById(R.id.chipWed);
        chipThu = findViewById(R.id.chipThu);
        chipFri = findViewById(R.id.chipFri);
        chipSat = findViewById(R.id.chipSat);
        chipSun = findViewById(R.id.chipSun);
    }

    private void setupChipsUX() {
        Chip[] chips = new Chip[]{chipMon, chipTue, chipWed, chipThu, chipFri, chipSat, chipSun};

        for (Chip c : chips) {
            // Sécurité (au cas où pas mis en XML)
            c.setCheckable(true);
            c.setCheckedIconVisible(false);

            // UX premium: CTA s’active/désactive en live
            c.setOnCheckedChangeListener((buttonView, isChecked) -> updateEnableButtonState());
        }

        updateEnableButtonState();
    }

    private void onEnableClicked() {
        int daysMask = computeDaysMaskFromChips();
        if (daysMask == 0) {
            Toast.makeText(this, getString(R.string.select_at_least_one_day), Toast.LENGTH_SHORT).show();
            return;
        }

        // Save
        Prefs.setDevotionEnabled(this, true);
        Prefs.saveDevotionSchedule(this, daysMask, hour, minute);

        // Schedule
        DevotionScheduler.scheduleAllSelectedDays(this);

        Toast.makeText(this, getString(R.string.devotion_enabled_ok), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateEnableButtonState() {
        boolean hasSelection = computeDaysMaskFromChips() != 0;
        buttonEnable.setEnabled(hasSelection);
        buttonEnable.setAlpha(hasSelection ? 1f : 0.6f);
    }

    private void openTimePicker() {
        boolean is24h = android.text.format.DateFormat.is24HourFormat(this);

        TimePickerDialog dlg = new TimePickerDialog(
                this,
                (view, h, m) -> {
                    hour = h;
                    minute = m;

                    textTime.setText(formatTime(hour, minute));

                    // Save direct (garde les jours actuels)
                    Prefs.saveDevotionSchedule(this, computeDaysMaskFromChips(), hour, minute);

                    // Optionnel: si déjà activé, reprogramme
                    if (Prefs.isDevotionEnabled(this)) {
                        DevotionScheduler.scheduleAllSelectedDays(this);
                    }
                },
                hour,
                minute,
                is24h
        );
        dlg.show();
    }

    private String formatTime(int h, int m) {
        return String.format(Locale.getDefault(), "%02d:%02d", h, m);
    }

    /**
     * Bitmask Mon..Sun:
     * bit0 = Mon, bit1 = Tue, ..., bit6 = Sun
     * => mask = 0..127
     */
    private int computeDaysMaskFromChips() {
        int mask = 0;
        if (chipMon.isChecked()) mask |= (1 << 0);
        if (chipTue.isChecked()) mask |= (1 << 1);
        if (chipWed.isChecked()) mask |= (1 << 2);
        if (chipThu.isChecked()) mask |= (1 << 3);
        if (chipFri.isChecked()) mask |= (1 << 4);
        if (chipSat.isChecked()) mask |= (1 << 5);
        if (chipSun.isChecked()) mask |= (1 << 6);
        return mask;
    }

    private void applyMaskToChips(int mask) {
        chipMon.setChecked((mask & (1 << 0)) != 0);
        chipTue.setChecked((mask & (1 << 1)) != 0);
        chipWed.setChecked((mask & (1 << 2)) != 0);
        chipThu.setChecked((mask & (1 << 3)) != 0);
        chipFri.setChecked((mask & (1 << 4)) != 0);
        chipSat.setChecked((mask & (1 << 5)) != 0);
        chipSun.setChecked((mask & (1 << 6)) != 0);
    }

    private void restoreDayChips() {
        int savedMask = Prefs.getDevotionDaysMask(this);

        // Si rien n’est configuré: défaut premium => Lun..Ven
        if (savedMask == 0) {
            int defaultMask = (1 << 0) | (1 << 1) | (1 << 2) | (1 << 3) | (1 << 4);
            applyMaskToChips(defaultMask);

            // On sauvegarde le défaut avec l'heure actuelle
            Prefs.saveDevotionSchedule(this, defaultMask, hour, minute);
        } else {
            applyMaskToChips(savedMask);
        }

        updateEnableButtonState();
    }

    /**
     * Helper (si ton scheduler utilise Calendar.*)
     * Retourne true si le jour Calendar.X est inclus.
     */
    public static boolean isCalendarDayIncluded(int daysMask, int calendarDay) {
        int bit;
        switch (calendarDay) {
            case Calendar.MONDAY: bit = 0; break;
            case Calendar.TUESDAY: bit = 1; break;
            case Calendar.WEDNESDAY: bit = 2; break;
            case Calendar.THURSDAY: bit = 3; break;
            case Calendar.FRIDAY: bit = 4; break;
            case Calendar.SATURDAY: bit = 5; break;
            case Calendar.SUNDAY: bit = 6; break;
            default: return false;
        }
        return (daysMask & (1 << bit)) != 0;
    }
}
