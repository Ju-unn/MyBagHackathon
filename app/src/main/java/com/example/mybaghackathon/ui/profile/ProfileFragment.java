package com.example.mybaghackathon.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.AppContainer;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.data.local.UserStorage;
import com.example.mybaghackathon.data.repository.DefaultItemRepository;
import com.example.mybaghackathon.databinding.FragmentProfileBinding;
import com.example.mybaghackathon.model.User;
import com.example.mybaghackathon.model.UserDefaultItem;
import com.example.mybaghackathon.ui.login.LoginActivity;
import com.example.mybaghackathon.ui.overlay.EditItemSheet;
import com.example.mybaghackathon.ui.settings.NotificationSettingsActivity;

import java.util.List;

/**
 * S15 · 프로필 — 아바타/이름 카드 + 내 기본 물품 미리보기(최대 4개) + 설정 목록.
 *
 * 기능: ProfilePresenter가 불러온 기본 물품 앞 4개를 RecyclerView로 보여주고
 * 행을 누르면 EditItemSheet로 수정/삭제하게 하며, "전체보기"로
 * ProfileItemsActivity 전체 목록으로, 알림 설정 행으로
 * NotificationSettingsActivity로 이동하고, 로그아웃 행을 누르면 로그인
 * 화면으로 돌아가는 화면.
 */
public class ProfileFragment extends Fragment implements ProfileContract.View {

    private FragmentProfileBinding binding;
    private ProfileContract.Presenter presenter;
    private ProfileItemAdapter itemAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        AppContainer appContainer = ((MyBagApplication) requireActivity().getApplication()).getAppContainer();
        DefaultItemRepository defaultItemRepository = appContainer.defaultItemRepository;
        UserStorage userStorage = appContainer.userStorage;
        presenter = new ProfilePresenter(this, defaultItemRepository, userStorage);

        itemAdapter = new ProfileItemAdapter((position, item) -> {
            EditItemSheet sheet = EditItemSheet.newInstance(item.getItemName());
            sheet.setOnItemEditedListener(new EditItemSheet.OnItemEditedListener() {
                @Override
                public void onItemRenamed(String newLabel) {
                    presenter.renameItem(item.getDefaultItemId(), newLabel);
                }

                @Override
                public void onItemDeleted() {
                    presenter.deleteItem(item.getDefaultItemId());
                }
            });
            sheet.show(getParentFragmentManager(), "edit_item");
        });
        binding.profileItemRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.profileItemRecycler.setAdapter(itemAdapter);

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
        presenter.loadItems();
    }

    @Override
    public void onDestroyView() {
        presenter.onDestroy();
        binding = null;
        super.onDestroyView();
    }

    // ===== ProfileContract.View =====

    @Override
    public void showUser(User user) {
        if (binding == null) return;
        if (user == null || user.getNickname() == null || user.getNickname().isEmpty()) {
            binding.profileName.setText(null);
            binding.profileAvatar.setInitial(null);
            binding.profileAvatar.setImageUrl(null);
            return;
        }
        binding.profileName.setText(user.getNickname());
        binding.profileAvatar.setInitial(user.getNickname().substring(0, 1));
        binding.profileAvatar.setImageUrl(user.getProfileImageUrl());
    }

    @Override
    public void showItemPreview(List<UserDefaultItem> previewItems, int totalCount) {
        if (binding == null) return;
        binding.profileItemCount.setText(getString(R.string.profile_item_count_format, totalCount));
        itemAdapter.submitList(previewItems);
    }

    @Override
    public void showError(String message) {
        if (getContext() == null) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
