package com.example.verset.Onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.verset.Prefs;
import com.example.verset.R;
import com.google.android.material.button.MaterialButton;

public class OnboardingSummaryActivity extends AppCompatActivity {

    private MaterialButton buttonContinue;

    private TextView valueName;
    private TextView valueAge;
    private TextView valueGender;
    private TextView valueRelationship;
    private TextView valueTradition;
    private TextView valuePrayerFrequency;
    private TextView valueVersesPerDay;
    private TextView valueTone;
    private TextView valueStreakGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_summary);

        // Optional ActionBar back
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

        // Back button (pill)
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> goBack());

        // Continue
        buttonContinue = findViewById(R.id.buttonContinue);
        if (buttonContinue != null) {
            buttonContinue.setOnClickListener(v -> goNext());
        }

        // Bind views
        valueName = findViewById(R.id.valueName);
        valueAge = findViewById(R.id.valueAge);
        valueGender = findViewById(R.id.valueGender);
        valueRelationship = findViewById(R.id.valueRelationship);
        valueTradition = findViewById(R.id.valueTradition);
        valuePrayerFrequency = findViewById(R.id.valuePrayerFrequency);
        valueVersesPerDay = findViewById(R.id.valueVersesPerDay);
        valueTone = findViewById(R.id.valueTone);
        valueStreakGoal = findViewById(R.id.valueStreakGoal);

        // Fill values
        bindSummaryValues();
    }

    private void bindSummaryValues() {
        // Basic
        setSafe(valueName, Prefs.getName(this), "—");
        setSafe(valueAge, Prefs.getAge(this), "—");
        setSafe(valueGender, humanGender(Prefs.getGender(this)), "—");
        setSafe(valueRelationship, Prefs.getRelationshipStatus(this), "—");

        // Spiritual preferences
        setSafe(valueTradition, humanTradition(Prefs.getTradition(this)), "—");
        setSafe(valuePrayerFrequency, Prefs.getRecueillementFrequency(this), "—");

        // Tone
        setSafe(valueTone, humanTone(Prefs.getTone(this)), "—");

        // Streak goal
        int goalDays = Prefs.getStreakGoalDays(this);
        if (goalDays <= 0) goalDays = 7;
        setSafe(valueStreakGoal, goalDays + " days", "—");

        // Verses/day (desired vs applied)
        int desired = Prefs.getDailyVersesDesired(this);
        int applied = Prefs.getDailyVersesApplied(this);
        boolean premium = Prefs.isPremium(this);

        // Ensure entitlement sync is reflected (optional)
        // Prefs.syncDailyVersesWithEntitlement(this);
        // applied = Prefs.getDailyVersesApplied(this);

        String versesText;
        if (premium) {
            versesText = applied + " / day";
        } else {
            // If user chose >1, show locked note
            if (desired > 1) {
                versesText = "1 / day (Premium for " + desired + ")";
            } else {
                versesText = "1 / day";
            }
        }
        setSafe(valueVersesPerDay, versesText, "—");
    }

    private void setSafe(TextView tv, String value, String fallback) {
        if (tv == null) return;
        if (value == null) value = "";
        value = value.trim();
        tv.setText(value.isEmpty() ? fallback : value);
    }

    private String humanTone(String tone) {
        if (tone == null) return "";
        tone = tone.trim();

        // Your TonePage saves: SOFT / BALANCED / DIRECT
        switch (tone) {
            case "SOFT":
                return "Very gentle";
            case "BALANCED":
                return "Balanced";
            case "DIRECT":
                return "Direct";
        }

        // Backward compatibility if old values exist
        if ("calm".equalsIgnoreCase(tone)) return "Very gentle";
        if ("encouraging".equalsIgnoreCase(tone)) return "Balanced";
        if ("simple".equalsIgnoreCase(tone)) return "Direct";

        return tone;
    }

    private String humanGender(String g) {
        if (g == null) return "";
        g = g.trim();

        // If your gender page saves exact strings like "Male/Female/Prefer not to say"
        if ("Male".equalsIgnoreCase(g)) return "Male";
        if ("Female".equalsIgnoreCase(g)) return "Female";
        if ("Prefer not to say".equalsIgnoreCase(g)) return "Prefer not to say";

        return g;
    }

    private String humanTradition(String t) {
        if (t == null) return "";
        t = t.trim();

        // If you saved in English: Catholic/Protestant/Mix
        if ("Catholic".equalsIgnoreCase(t)) return "Catholic";
        if ("Protestant".equalsIgnoreCase(t)) return "Protestant";
        if ("Mix".equalsIgnoreCase(t)) return "Mixed (all traditions)";

        return t;
    }

    private void goNext() {

        startActivity(new Intent(this, PremiumOfferActivity.class));

        finish();
        runTransitionForward();
    }

    private void goBack() {
        finish();
        runTransitionBack();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goBack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    private void runTransitionForward() {
        int enter = getAnimId("slide_in_right");
        int exit = getAnimId("slide_out_left");
        if (enter != 0 && exit != 0) overridePendingTransition(enter, exit);
    }

    private void runTransitionBack() {
        int enter = getAnimId("slide_in_left");
        int exit = getAnimId("slide_out_right");
        if (enter != 0 && exit != 0) overridePendingTransition(enter, exit);
    }

    private int getAnimId(String name) {
        return getResources().getIdentifier(name, "anim", getPackageName());
    }
}
