package com.example.verset.Tiktoklike;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.verset.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Bottom sheet that mimics the onboarding "choice cards" UI.
 *
 * Usage:
 * ChoiceBottomSheet.newInstance(title, options, currentValue)
 *   .setListener(...)
 *   .show(getSupportFragmentManager(), "choice");
 */
public class ChoiceBottomSheet extends BottomSheetDialogFragment {

    public interface Listener {
        void onSelected(@NonNull String value);
    }

    private static final String ARG_TITLE = "title";
    private static final String ARG_OPTIONS = "options";
    private static final String ARG_CURRENT = "current";

    private Listener listener;

    // Runtime colors (same names as onboarding)
    private int colorBorderSoft;
    private int colorSelectedBorder;
    private int colorSelectedBg;
    private int colorCardBg;
    private int colorIconUnselected;
    private int colorIconSelected;

    private final List<MaterialCardView> cards = new ArrayList<>();
    private final List<ImageView> checks = new ArrayList<>();
    private final List<String> values = new ArrayList<>();

    public static ChoiceBottomSheet newInstance(@NonNull String title,
                                                @NonNull String[] options,
                                                @Nullable String currentValue) {
        ChoiceBottomSheet sheet = new ChoiceBottomSheet();
        Bundle b = new Bundle();
        b.putString(ARG_TITLE, title);
        b.putStringArray(ARG_OPTIONS, options);
        b.putString(ARG_CURRENT, currentValue);
        sheet.setArguments(b);
        return sheet;
    }

    public ChoiceBottomSheet setListener(@Nullable Listener l) {
        this.listener = l;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.bottom_sheet_choice, container, false);

        TextView title = root.findViewById(R.id.sheetTitle);
        LinearLayout optionContainer = root.findViewById(R.id.optionContainer);

        Bundle args = getArguments();
        String sheetTitle = args != null ? args.getString(ARG_TITLE, "") : "";
        String[] options = args != null ? args.getStringArray(ARG_OPTIONS) : new String[0];
        String current = args != null ? args.getString(ARG_CURRENT) : null;

        if (title != null) title.setText(sheetTitle);

        // Resolve colors safely (same as onboarding activities)
        Context ctx = requireContext();
        colorBorderSoft = getColorIdSafe(ctx, "card_border_soft", 0xFFECE6DA);
        colorSelectedBorder = getColorIdSafe(ctx, "choice_selected_border", 0xFF6F8FCF);
        colorSelectedBg = getColorIdSafe(ctx, "choice_selected_bg", 0xFFEEF2FA);
        colorCardBg = getColorIdSafe(ctx, "card_background", 0xFFFFFFFF);
        colorIconUnselected = getColorIdSafe(ctx, "choice_unselected_icon", 0xFFC9D3E6);
        colorIconSelected = getColorIdSafe(ctx, "accent_primary", 0xFF6F8FCF);

        // Build option cards dynamically
        if (optionContainer != null && options != null) {
            for (String opt : options) {
                View item = inflater.inflate(R.layout.item_choice_card, optionContainer, false);
                MaterialCardView card = item.findViewById(R.id.choiceCard);
                TextView text = item.findViewById(R.id.choiceText);
                ImageView check = item.findViewById(R.id.choiceCheck);

                if (text != null) text.setText(opt);

                cards.add(card);
                checks.add(check);
                values.add(opt);

                if (card != null) {
                    card.setOnClickListener(v -> {
                        applySelectionUI(opt);
                        if (listener != null) listener.onSelected(opt);
                        dismissAllowingStateLoss();
                    });
                }

                optionContainer.addView(item);
            }
        }

        applySelectionUI(current);

        return root;
    }

    private void applySelectionUI(@Nullable String selected) {
        for (int i = 0; i < cards.size(); i++) {
            String v = values.get(i);
            boolean isSelected = selected != null && selected.equals(v);
            if (isSelected) {
                setCardSelected(cards.get(i), checks.get(i));
            } else {
                setCardUnselected(cards.get(i), checks.get(i));
            }
        }
    }

    private void setCardSelected(@Nullable MaterialCardView card, @Nullable ImageView check) {
        if (card != null) {
            card.setStrokeWidth(dpToPx(1));
            card.setStrokeColor(colorSelectedBorder);
            card.setCardBackgroundColor(colorSelectedBg);
        }
        if (check != null) {
            check.setImageResource(R.drawable.ic_check_circle);
            check.setColorFilter(colorIconSelected);
        }
    }

    private void setCardUnselected(@Nullable MaterialCardView card, @Nullable ImageView check) {
        if (card != null) {
            card.setStrokeWidth(dpToPx(1));
            card.setStrokeColor(colorBorderSoft);
            card.setCardBackgroundColor(colorCardBg);
        }
        if (check != null) {
            check.setImageResource(R.drawable.ic_check_circle);
            check.setColorFilter(colorIconUnselected);
        }
    }

    private int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                requireContext().getResources().getDisplayMetrics()
        );
    }

    private int getColorIdSafe(@NonNull Context ctx, @NonNull String name, int fallback) {
        int id = ctx.getResources().getIdentifier(name, "color", ctx.getPackageName());
        try {
            return id != 0 ? ctx.getColor(id) : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}