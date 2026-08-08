package com.example.mybaghackathon.ui.overlay;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.SheetRestrictionInfoBinding;
import com.example.mybaghackathon.model.RestrictedItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

/**
 * S08 반입 규정 안내 바텀시트 — restrictedItems를 품목별 행으로 나눠서
 * 구분선으로 구획된 카드 안에 보여줌.
 */
public class RestrictionInfoSheet extends BottomSheetDialogFragment {

    private static final String ARG_ITEMS = "items";

    private SheetRestrictionInfoBinding binding;

    public static RestrictionInfoSheet newInstance(ArrayList<RestrictedItem> items) {
        RestrictionInfoSheet sheet = new RestrictionInfoSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEMS, items);
        sheet.setArguments(args);
        return sheet;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = SheetRestrictionInfoBinding.inflate(inflater, container, false);

        ArrayList<RestrictedItem> items = getArguments() != null
                ? (ArrayList<RestrictedItem>) getArguments().getSerializable(ARG_ITEMS)
                : null;
        if (items == null) items = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                View divider = new View(requireContext());
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));
                divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bag_border_divider));
                binding.restrictionInfoList.addView(divider);
            }

            RestrictedItem item = items.get(i);
            View row = inflater.inflate(R.layout.molecule_restriction_info_row, binding.restrictionInfoList, false);
            ((TextView) row.findViewById(R.id.restrictionRowName)).setText(item.getItemName());

            TextView typeView = row.findViewById(R.id.restrictionRowType);
            typeView.setText(restrictionTypeLabel(item.getRestrictionType()));
            boolean severe = "PROHIBITED".equals(item.getRestrictionType());
            typeView.setTextColor(ContextCompat.getColor(requireContext(),
                    severe ? R.color.bag_status_danger : R.color.bag_text_brand));

            TextView reasonView = row.findViewById(R.id.restrictionRowReason);
            if (TextUtils.isEmpty(item.getReason())) {
                reasonView.setVisibility(View.GONE);
            } else {
                reasonView.setText(item.getReason());
            }

            binding.restrictionInfoList.addView(row);
        }

        binding.restrictionInfoConfirmButton.setOnClickListener(v -> dismiss());

        return binding.getRoot();
    }

    private String restrictionTypeLabel(String type) {
        if (type == null) return "";
        switch (type) {
            case "PROHIBITED": return getString(R.string.restriction_prohibited);
            case "CARRY_ON_ONLY": return getString(R.string.restriction_cabin_only);
            case "CHECKED_ONLY": return getString(R.string.restriction_checked_only);
            case "LIMITED": return getString(R.string.restriction_limited);
            case "CAUTION": return getString(R.string.restriction_caution);
            default: return type;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
