package com.example.mybaghackathon.ui.overlay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.SheetAssignItemBinding;
import com.example.mybaghackathon.model.TripMember;
import com.example.mybaghackathon.ui.atoms.AvatarView;
import com.example.mybaghackathon.ui.atoms.CheckboxView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * BS07 · 공용 물품 담당자 지정 바텀시트.
 *
 * <p>참여자 복수 선택 후 완료 시 선택된 ID 목록을 Fragment Result로 돌려준다.
 * 1명 선택 시 단일 배정(ChecklistPresenter.assignItem), 2명 이상 선택 시 방장 전용
 * 다중 배정(ChecklistPresenter.assignItems, 서버가 COMMON row를 인원 수만큼 복제)으로
 * 갈린다 — 분기는 호출부(ChecklistCommonFragment)에서 처리한다.</p>
 */
public class AssignItemSheet extends BottomSheetDialogFragment {

    public static final String RESULT_REQUEST_KEY = "assign_item_result";
    public static final String RESULT_ITEM_ID = "result_item_id";
    public static final String RESULT_SELECTED_IDS = "result_selected_ids";

    private static final String ARG_ITEM_ID = "item_id";
    private static final String ARG_ITEM_NAME = "item_name";
    private static final String ARG_MEMBER_IDS = "member_ids";
    private static final String ARG_MEMBER_NAMES = "member_names";
    private static final String ARG_MEMBER_IMAGE_URLS = "member_image_urls";
    private static final String ARG_MEMBER_ROLES = "member_roles";
    private static final String ARG_SELECTED_IDS = "selected_ids";
    private static final String STATE_SELECTED_IDS = "state_selected_ids";

    private final Set<Long> selectedUserIds = new LinkedHashSet<>();
    private SheetAssignItemBinding binding;

    public static AssignItemSheet newInstance(
            long itemId,
            String itemName,
            List<TripMember> members,
            List<Long> selectedUserIds
    ) {
        AssignItemSheet sheet = new AssignItemSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_ITEM_ID, itemId);
        args.putString(ARG_ITEM_NAME, itemName);

        long[] ids = new long[members.size()];
        ArrayList<String> names = new ArrayList<>(members.size());
        ArrayList<String> imageUrls = new ArrayList<>(members.size());
        ArrayList<String> roles = new ArrayList<>(members.size());
        for (int index = 0; index < members.size(); index++) {
            TripMember member = members.get(index);
            ids[index] = member.getUserId();
            names.add(member.getNickname());
            imageUrls.add(member.getProfileImageUrl());
            roles.add(member.getRole());
        }
        args.putLongArray(ARG_MEMBER_IDS, ids);
        args.putStringArrayList(ARG_MEMBER_NAMES, names);
        args.putStringArrayList(ARG_MEMBER_IMAGE_URLS, imageUrls);
        args.putStringArrayList(ARG_MEMBER_ROLES, roles);
        args.putLongArray(ARG_SELECTED_IDS, toLongArray(selectedUserIds));
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = SheetAssignItemBinding.inflate(inflater, container, false);
        Bundle args = requireArguments();
        binding.assignItemTitle.setText(getString(
                R.string.bs_assign_item_title_format,
                args.getString(ARG_ITEM_NAME, "")));

        long[] restoredIds = savedInstanceState == null
                ? args.getLongArray(ARG_SELECTED_IDS)
                : savedInstanceState.getLongArray(STATE_SELECTED_IDS);
        restoreSelection(restoredIds);
        bindMembers(args);

        binding.assignItemCompleteButton.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putLong(RESULT_ITEM_ID, args.getLong(ARG_ITEM_ID));
            result.putLongArray(RESULT_SELECTED_IDS, toLongArray(selectedUserIds));
            getParentFragmentManager().setFragmentResult(RESULT_REQUEST_KEY, result);
            dismiss();
        });
        return binding.getRoot();
    }

    private void bindMembers(Bundle args) {
        LinearLayout memberList = binding.assignItemMemberList;
        memberList.removeAllViews();

        long[] ids = args.getLongArray(ARG_MEMBER_IDS);
        ArrayList<String> names = args.getStringArrayList(ARG_MEMBER_NAMES);
        ArrayList<String> imageUrls = args.getStringArrayList(ARG_MEMBER_IMAGE_URLS);
        ArrayList<String> roles = args.getStringArrayList(ARG_MEMBER_ROLES);
        if (ids == null || names == null || imageUrls == null || roles == null) {
            return;
        }

        int memberCount = Math.min(ids.length,
                Math.min(names.size(), Math.min(imageUrls.size(), roles.size())));
        for (int index = 0; index < memberCount; index++) {
            addMemberRow(memberList, ids[index], names.get(index), imageUrls.get(index), roles.get(index));
        }
    }

    private void addMemberRow(
            LinearLayout parent,
            long userId,
            @Nullable String nickname,
            @Nullable String profileImageUrl,
            @Nullable String role
    ) {
        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.molecule_member_assign_row, parent, false);
        AvatarView avatar = row.findViewById(R.id.memberAvatar);
        TextView name = row.findViewById(R.id.memberName);
        TextView hostBadge = row.findViewById(R.id.memberHostBadge);
        CheckboxView checkbox = row.findViewById(R.id.memberAssignRowCheckbox);

        String displayName = hasText(nickname)
                ? nickname.trim()
                : getString(R.string.checklist_member_fallback);
        name.setText(displayName);
        avatar.setInitial(displayName.substring(0, 1));
        avatar.setAvatarColor(ContextCompat.getColor(requireContext(), avatarColor(userId)));
        avatar.setImageUrl(profileImageUrl);
        hostBadge.setVisibility("OWNER".equalsIgnoreCase(role) ? View.VISIBLE : View.GONE);

        checkbox.setState(selectedUserIds.contains(userId)
                ? CheckboxView.CHECKED
                : CheckboxView.UNCHECKED);
        checkbox.setOnCheckChangeListener(state -> updateSelection(userId, state));
        row.setOnClickListener(v -> {
            int nextState = checkbox.getState() == CheckboxView.CHECKED
                    ? CheckboxView.UNCHECKED
                    : CheckboxView.CHECKED;
            checkbox.setState(nextState);
            updateSelection(userId, nextState);
        });
        parent.addView(row);
    }

    private void updateSelection(long userId, int checkboxState) {
        if (checkboxState == CheckboxView.CHECKED) {
            selectedUserIds.add(userId);
        } else {
            selectedUserIds.remove(userId);
        }
    }

    private void restoreSelection(@Nullable long[] ids) {
        selectedUserIds.clear();
        if (ids == null) {
            return;
        }
        for (long id : ids) {
            selectedUserIds.add(id);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLongArray(STATE_SELECTED_IDS, toLongArray(selectedUserIds));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    @ColorRes
    private int avatarColor(long userId) {
        int[] colors = {
                R.color.bag_avatar_1,
                R.color.bag_avatar_2,
                R.color.bag_avatar_3,
                R.color.bag_avatar_4
        };
        return colors[(int) Math.abs(userId % colors.length)];
    }

    private boolean hasText(@Nullable String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static long[] toLongArray(@Nullable Iterable<Long> values) {
        if (values == null) {
            return new long[0];
        }
        ArrayList<Long> safeValues = new ArrayList<>();
        for (Long value : values) {
            if (value != null) {
                safeValues.add(value);
            }
        }
        long[] result = new long[safeValues.size()];
        for (int index = 0; index < safeValues.size(); index++) {
            result[index] = safeValues.get(index);
        }
        return result;
    }
}
