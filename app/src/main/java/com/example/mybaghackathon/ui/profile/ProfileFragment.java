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
import com.example.mybaghackathon.databinding.FragmentProfileBinding;
import com.example.mybaghackathon.ui.overlay.EditItemSheet;
import com.example.mybaghackathon.ui.settings.NotificationSettingsActivity;

/**
 * S14 · 프로필 — 사용자의 기본 짐 항목 목록 + 앱 설정.
 *
 * 기능: 기본 항목들을 목록으로 보여주고 각 행을 누르면 EditItemSheet로
 * 수정/삭제하게 하며, 추가 버튼으로 AddItemSheet를 띄우고, 알림 설정 행
 * 클릭 시 NotificationSettingsActivity로 이동하는 화면.
 */
public class ProfileFragment extends Fragment {

    private static final String[] DEFAULT_ITEMS = {"여권", "충전기", "보조배터리", "우산"};

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LinearLayout itemList = binding.profileItemList;
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

        binding.profileAddItemWrapper.setOnClickListener(v -> {
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

        binding.profileNotifRow.setOnClickListener(v ->
                startActivity(new Intent(getContext(), NotificationSettingsActivity.class)));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
