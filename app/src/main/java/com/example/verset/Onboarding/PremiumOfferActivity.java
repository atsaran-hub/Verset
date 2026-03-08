package com.example.verset.Onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.verset.Prefs;
import com.example.verset.R;
import com.example.verset.WidgetCustomizeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class PremiumOfferActivity extends AppCompatActivity {

    private MaterialCardView cardPlanYearly, cardPlanMonthly;
    private ImageView checkYearly, checkMonthly;

    private MaterialButton btnStartTrial;
    private TextView linkSkip, txtPrice;
    private TextView txtPersonalLine1, txtPersonalLine2;

    private enum Plan { YEARLY, MONTHLY }
    private Plan selectedPlan = Plan.YEARLY; // default best conversion

    // Colors
    private int borderSoft;
    private int selectedBorder;
    private int selectedBg;
    private int cardBg;
    private int iconUnselected;
    private int iconSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium_offer);

        // Optional ActionBar back
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

        // Back
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());

        // Views
        cardPlanYearly = findViewById(R.id.cardPlanYearly);
        cardPlanMonthly = findViewById(R.id.cardPlanMonthly);
        checkYearly = findViewById(R.id.checkYearly);
        checkMonthly = findViewById(R.id.checkMonthly);

        btnStartTrial = findViewById(R.id.btnStartTrial);
        linkSkip = findViewById(R.id.linkSkip);
        txtPrice = findViewById(R.id.txtPrice);

        txtPersonalLine1 = findViewById(R.id.txtPersonalLine1);
        txtPersonalLine2 = findViewById(R.id.txtPersonalLine2);

        // Resolve colors safely
        borderSoft = getColorIdSafe("card_border_soft", 0xFFECE6DA);
        selectedBorder = getColorIdSafe("choice_selected_border", 0xFF6F8FCF);
        selectedBg = getColorIdSafe("choice_selected_bg", 0xFFEEF2FA);
        cardBg = getColorIdSafe("card_background", 0xFFFFFFFF);
        iconUnselected = getColorIdSafe("choice_unselected_icon", 0xFFC9D3E6);
        iconSelected = getColorIdSafe("accent_primary", 0xFF6F8FCF);

        // Personal section from Prefs
        fillPersonalizedText();

        // Default selection
        applyPlanUI();

        // Plan clicks
        if (cardPlanYearly != null) cardPlanYearly.setOnClickListener(v -> {
            selectedPlan = Plan.YEARLY;
            applyPlanUI();
        });

        if (cardPlanMonthly != null) cardPlanMonthly.setOnClickListener(v -> {
            selectedPlan = Plan.MONTHLY;
            applyPlanUI();
        });

        // CTA (billing later)
        if (btnStartTrial != null) {
            btnStartTrial.setOnClickListener(v -> {
                // TODO: Launch your Billing flow here.
                // For now we simulate "premium true" to keep your app logic consistent.
                Prefs.setPremium(this, true);
                Prefs.syncDailyVersesWithEntitlement(this);

                goNextAfterPaywall();
            });
        }

        // Skip + conversion dialog
        if (linkSkip != null) {
            linkSkip.setOnClickListener(v -> showSkipDialog());
        }

        // Terms/Restore/Privacy: keep clickable later if needed
        // (You can open a WebView or external link)
    }

    private void fillPersonalizedText() {
        // Verses/day: you may store desired in KEY_DAILY_VERSES_DESIRED or KEY_VERSES_PER_DAY.
        // We'll prefer "desired" because it's your premium gating logic.
        int desired = Prefs.getDailyVersesDesired(this);
        if (desired < 1) desired = 1;

        String tone = Prefs.getTone(this);
        String toneLabel = toneToLabel(tone);

        if (txtPersonalLine1 != null) {
            if (desired <= 1) {
                txtPersonalLine1.setText("• You chose 1 verse/day → you can unlock more with Premium");
            } else {
                txtPersonalLine1.setText("• You chose " + desired + " verses/day → Premium unlocks it");
            }
        }

        if (txtPersonalLine2 != null) {
            txtPersonalLine2.setText("• Your tone: " + toneLabel + " → daily messages feel “made for you”");
        }
    }

    private String toneToLabel(String tone) {
        if (tone == null) return "Balanced";
        switch (tone) {
            case "very_soft":
            case "soft":
            case "calm":
                return "Very soft";
            case "balanced":
            case "encouraging":
                return "Balanced";
            case "direct":
            case "simple":
                return "Direct";
            default:
                return "Balanced";
        }
    }

    private void applyPlanUI() {
        if (selectedPlan == Plan.YEARLY) {
            setSelected(cardPlanYearly, checkYearly);
            setUnselected(cardPlanMonthly, checkMonthly);
            if (txtPrice != null) txtPrice.setText("Best value: yearly plan selected");
        } else {
            setSelected(cardPlanMonthly, checkMonthly);
            setUnselected(cardPlanYearly, checkYearly);
            if (txtPrice != null) txtPrice.setText("Monthly plan selected • Cancel anytime");
        }
    }

    private void setSelected(MaterialCardView card, ImageView check) {
        if (card != null) {
            card.setStrokeWidth(dpToPx(1));
            card.setStrokeColor(selectedBorder);
            card.setCardBackgroundColor(selectedBg);
        }
        if (check != null) {
            check.setColorFilter(iconSelected);
        }
    }

    private void setUnselected(MaterialCardView card, ImageView check) {
        if (card != null) {
            card.setStrokeWidth(dpToPx(1));
            card.setStrokeColor(borderSoft);
            card.setCardBackgroundColor(cardBg);
        }
        if (check != null) {
            check.setColorFilter(iconUnselected);
        }
    }

    private void showSkipDialog() {
        int desired = Prefs.getDailyVersesDesired(this);
        if (desired < 1) desired = 1;

        String message =
                "You’re about to miss a powerful offer:\n\n" +
                        "✅ 3 days free\n" +
                        "✅ Unlock up to 5 verses/day\n" +
                        "✅ Audio reflections\n\n" +
                        (desired > 1
                                ? "You selected " + desired + " verses/day — without Premium you’ll stay limited to 1."
                                : "Without Premium you’ll stay limited to 1 verse/day.");

        new AlertDialog.Builder(this)
                .setTitle("Skip Premium?")
                .setMessage(message)
                .setNegativeButton("Get 3 free days", (d, w) -> {
                    if (btnStartTrial != null) btnStartTrial.performClick();
                })
                .setPositiveButton("Continue free", (d, w) -> {
                    // Ensure free entitlement is applied
                    Prefs.setPremium(this, false);
                    Prefs.syncDailyVersesWithEntitlement(this);

                    goNextAfterPaywall();
                })
                .show();
    }

    private void goNextAfterPaywall() {
        Intent intent = new Intent(this, WidgetCustomizeActivity.class);
        startActivity(intent);
        finish();
        runTransitionForward();
    }


    // ActionBar back
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void runTransitionForward() {
        int enter = getAnimId("slide_in_right");
        int exit = getAnimId("slide_out_left");
        if (enter != 0 && exit != 0) {
            overridePendingTransition(enter, exit);
        }
    }

    private int getAnimId(String name) {
        return getResources().getIdentifier(name, "anim", getPackageName());
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int getColorIdSafe(String colorName, int fallback) {
        int id = getResources().getIdentifier(colorName, "color", getPackageName());
        if (id == 0) return fallback;
        return getColor(id);
    }
}
