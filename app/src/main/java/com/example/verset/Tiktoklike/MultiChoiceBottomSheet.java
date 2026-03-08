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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bottom sheet "choice cards" style onboarding, but MULTI-select.
 * Reuses item_choice_card.xml.
 */
public class MultiChoiceBottomSheet extends BottomSheetDialogFragment {

    public interface Listener {
        void onSaved(@NonNull Set<String> selectedKeys);
    }

    private static final String ARG_TITLE = "title";
    private static final String ARG_KEYS = "keys";
    private static final String ARG_LABELS = "labels";
    private static final String ARG_SELECTED = "selected";

    private Listener listener;

    // colors
    private int colorBorderSoft;
    private int colorSelectedBorder;
    private int colorSelectedBg;
    private int colorCardBg;
    private int colorIconUnselected;
    private int colorIconSelected;

    private final List<MaterialCardView> cards = new ArrayList<>();
    private final List<ImageView> checks = new ArrayList<>();
    private final List<String> keys = new ArrayList<>();

    private final Set<String> selected = new HashSet<>();

    public static MultiChoiceBottomSheet newInstance(
            @NonNull String title,
            @NonNull String[] keys,
            @NonNull String[] labels,
            @Nullable Set<String> currentlySelected
    ) {
        MultiChoiceBottomSheet sheet = new MultiChoiceBottomSheet();
        Bundle b = new Bundle();
        b.putString(ARG_TITLE, title);
        b.putStringArray(ARG_KEYS, keys);
        b.putStringArray(ARG_LABELS, labels);
        b.putStringArray(ARG_SELECTED, currentlySelected == null ? new String[0] : currentlySelected.toArray(new String[0]));
        sheet.setArguments(b);
        return sheet;
    }

    public MultiChoiceBottomSheet setListener(@Nullable Listener l) {
        this.listener = l;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.bottom_sheet_multi_choice, container, false);

        TextView title = root.findViewById(R.id.sheetTitle);
        LinearLayout optionContainer = root.findViewById(R.id.optionContainer);
        MaterialButton btnSave = root.findViewById(R.id.btnSaveMultiChoice);

        Bundle args = getArguments();
        String sheetTitle = args != null ? args.getString(ARG_TITLE, "") : "";
        String[] argKeys = args != null ? args.getStringArray(ARG_KEYS) : new String[0];
        String[] argLabels = args != null ? args.getStringArray(ARG_LABELS) : new String[0];
        String[] argSelected = args != null ? args.getStringArray(ARG_SELECTED) : new String[0];

        if (title != null) title.setText(sheetTitle);

        selected.clear();
        if (argSelected != null) {
            for (String s : argSelected) {
                if (s != null && !s.trim().isEmpty()) selected.add(s);
            }
        }

        // Resolve colors safely (same naming as onboarding)
        Context ctx = requireContext();
        colorBorderSoft = getColorIdSafe(ctx, "card_border_soft", 0xFFECE6DA);
        colorSelectedBorder = getColorIdSafe(ctx, "choice_selected_border", 0xFF6F8FCF);
        colorSelectedBg = getColorIdSafe(ctx, "choice_selected_bg", 0xFFEEF2FA);
        colorCardBg = getColorIdSafe(ctx, "card_background", 0xFFFFFFFF);
        colorIconUnselected = getColorIdSafe(ctx, "choice_unselected_icon", 0xFFC9D3E6);
        colorIconSelected = getColorIdSafe(ctx, "accent_primary", 0xFF6F8FCF);

        cards.clear();
        checks.clear();
        keys.clear();

        int count = Math.min(argKeys == null ? 0 : argKeys.length, argLabels == null ? 0 : argLabels.length);

        if (optionContainer != null) {
            for (int i = 0; i < count; i++) {
                String key = argKeys[i];
                String label = argLabels[i];

                View item = inflater.inflate(R.layout.item_choice_card, optionContainer, false);
                MaterialCardView card = item.findViewById(R.id.choiceCard);
                TextView text = item.findViewById(R.id.choiceText);
                ImageView check = item.findViewById(R.id.choiceCheck);

                if (text != null) text.setText(label);

                cards.add(card);
                checks.add(check);
                keys.add(key);

                if (card != null) {
                    card.setOnClickListener(v -> {
                        toggle(key);
                        applySelectionUI();
                        if (btnSave != null) btnSave.setEnabled(!selected.isEmpty());
                    });
                }

                optionContainer.addView(item);
            }
        }

        applySelectionUI();

        if (btnSave != null) {
            btnSave.setEnabled(!selected.isEmpty());
            btnSave.setOnClickListener(v -> {
                if (listener != null) listener.onSaved(new HashSet<>(selected));
                dismissAllowingStateLoss();
            });
        }

        return root;
    }

    private void toggle(@NonNull String key) {
        if (selected.contains(key)) selected.remove(key);
        else selected.add(key);
    }

    private void applySelectionUI() {
        for (int i = 0; i < cards.size(); i++) {
            String key = keys.get(i);
            boolean isSelected = selected.contains(key);
            if (isSelected) setCardSelected(cards.get(i), checks.get(i));
            else setCardUnselected(cards.get(i), checks.get(i));
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