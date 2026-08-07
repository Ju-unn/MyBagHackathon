package com.example.mybaghackathon.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.databinding.FragmentProfileBinding;
import com.example.mybaghackathon.ui.login.LoginActivity;
import com.example.mybaghackathon.ui.overlay.EditItemSheet;
import com.example.mybaghackathon.ui.settings.NotificationSettingsActivity;

import java.util.List;

/**
 * S15 · 프로필 — 아바타/이름 카드 + 내 기본 물품 미리보기(최대 4개) + 설정 목록.
 *
 * 기능: 기본 물품 앞 4개를 아이콘과 함께 보여주고 행을 누르면 EditItemSheet로
 * 수정/삭제하게 하며, "전체보기"로 ProfileItemsActivity 전체 목록으로,
 * 알림 설정 행으로 NotificationSettingsActivity로 이동하고, 로그아웃 행을
 * 누르면 로그인 화면으로 돌아가는 화면.
 */
public class ProfileFragment extends Fragment {

    private static final int PREVIEW_COUNT = 4;

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        binding.profileViewAllRow.setOnClickListener(v ->
                startActivity(new Intent(getContext(), ProfileItemsActivity.class)));

        binding.profileNotifRow.setOnClickListener(v ->
                startActivity(new Intent(getContext(), NotificationSettingsActivity.class)));

        binding.profileLogoutRow.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        renderItemPreview();
    }

    private void renderItemPreview() {
        if (binding == null) return;

        LinearLayout itemList = binding.profileItemList;
        itemList.removeAllViews();

        List<String> items = DefaultItemStore.getItems();
        binding.profileItemCount.setText(getString(
                com.example.mybaghackathon.R.string.profile_item_count_format, items.size()));

        int previewCount = Math.min(PREVIEW_COUNT, items.size());
        for (int i = 0; i < previewCount; i++) {
            String label = items.get(i);
            int index = i;
            itemList.addView(ProfileItemRowViews.create(requireContext(), label, v -> {
                EditItemSheet sheet = EditItemSheet.newInstance(label);
                sheet.setOnItemEditedListener(new EditItemSheet.OnItemEditedListener() {
                    @Override
                    public void onItemRenamed(String newLabel) {
                        DefaultItemStore.rename(index, newLabel);
                        renderItemPreview();
                    }

                    @Override
                    public void onItemDeleted() {
                        DefaultItemStore.removeAt(index);
                        renderItemPreview();
                    }
                });
                sheet.show(getParentFragmentManager(), "edit_item");
            }));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
