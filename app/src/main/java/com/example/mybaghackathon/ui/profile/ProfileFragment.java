package com.example.mybaghackathon.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.ui.overlay.EditItemSheet;
import com.example.mybaghackathon.ui.settings.NotificationSettingsActivity;

/** S14 · Profile — user's default packing items + app settings. */
public class ProfileFragment extends Fragment {

    private static final String[] DEFAULT_ITEMS = {"여권", "충전기", "보조배터리", "우산"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        LinearLayout itemList = root.findViewById(R.id.profileItemList);
        for (String item : DEFAULT_ITEMS) {
            TextView row = new TextView(requireContext());
            row.setText(item);
            row.setTextAppearance(R.style.TextAppearance_Bag_BodyL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(40));
            row.setLayoutParams(lp);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setOnClickListener(v -> {
                EditItemSheet sheet = EditItemSheet.newInstance(item);
                sheet.setOnItemEditedListener(new EditItemSheet.OnItemEditedListener() {
                    @Override
                    public void onItemRenamed(String newLabel) {
                        row.setText(newLabel);
                    }

                    @Override
                    public void onItemDeleted() {
                        itemList.removeView(row);
                    }
                });
                sheet.show(getParentFragmentManager(), "edit_item");
            });
            itemList.addView(row);
        }

        root.findViewById(R.id.profileAddItemWrapper).setOnClickListener(v -> {
            com.example.mybaghackathon.ui.overlay.AddItemSheet sheet =
                    new com.example.mybaghackathon.ui.overlay.AddItemSheet();
            sheet.setOnItemAddedListener((label, priority) -> {
                TextView row = new TextView(requireContext());
                row.setText(label);
                row.setTextAppearance(R.style.TextAppearance_Bag_BodyL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(40));
                row.setLayoutParams(lp);
                row.setGravity(android.view.Gravity.CENTER_VERTICAL);
                itemList.addView(row);
            });
            sheet.show(getParentFragmentManager(), "add_item");
        });

        root.findViewById(R.id.profileNotifRow).setOnClickListener(v ->
                startActivity(new Intent(getContext(), NotificationSettingsActivity.class)));

        return root;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
