package com.example.mybaghackathon.ui.checklist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.FragmentChecklistAssignmentBinding;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.TripMember;
import com.example.mybaghackathon.ui.atoms.AvatarView;
import com.example.mybaghackathon.ui.atoms.CheckboxView;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** S13 · 참여자별 담당 항목과 미배정 항목을 표시한다. */
public class ChecklistAssignmentFragment extends Fragment implements ChecklistDataConsumer {

    private FragmentChecklistAssignmentBinding binding;
    private ChecklistHost host;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChecklistAssignmentBinding.inflate(inflater, container, false);
        host = (ChecklistHost) requireActivity();
        renderChecklist();
        return binding.getRoot();
    }

    @Override
    public void renderChecklist() {
        if (binding == null || host == null) {
            return;
        }
        List<TripMember> members = host.getTripMembers();
        Map<Long, TripMember> memberById = new HashMap<>();
        for (TripMember member : members) {
            memberById.put(member.getUserId(), member);
        }

        bindAvatarStack(members);
        binding.checklistAssignmentSummary.setText(getString(
                R.string.checklist_assignment_summary, members.size()));

        LinearLayout assigned = binding.checklistAssignmentList;
        LinearLayout unassigned = binding.checklistUnassignedList;
        assigned.removeAllViews();
        unassigned.removeAllViews();

        for (TripMember member : members) {
            List<PackingItem> memberItems = new ArrayList<>();
            for (PackingItem item : host.getChecklistItems()) {
                if (ChecklistItemVisibility.isCommon(item)
                        && item.getAssigneeUserId() != null
                        && item.getAssigneeUserId() == member.getUserId()) {
                    memberItems.add(item);
                }
            }
            if (!memberItems.isEmpty()) {
                addMemberHeader(assigned, member);
                for (PackingItem item : memberItems) {
                    addAssignedRow(assigned, item);
                }
            }
        }

        for (PackingItem item : host.getChecklistItems()) {
            if (ChecklistItemVisibility.isCommon(item)
                    && (item.getAssigneeUserId() == null
                    || !memberById.containsKey(item.getAssigneeUserId()))) {
                addUnassignedRow(unassigned, item);
            }
        }

        if (unassigned.getChildCount() == 0 && host.isChecklistLoaded()) {
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.checklist_unassigned_empty);
            empty.setTextAppearance(R.style.TextAppearance_Bag_BodyM);
            unassigned.addView(empty);
        }
    }

    private void addMemberHeader(LinearLayout list, TripMember member) {
        View header = LayoutInflater.from(requireContext())
                .inflate(R.layout.molecule_member_list_item, list, false);
        ((TextView) header.findViewById(R.id.memberName)).setText(member.getNickname());
        AvatarView avatar = header.findViewById(R.id.memberAvatar);
        avatar.setInitial(initial(member.getNickname()));
        avatar.setAvatarColor(ContextCompat.getColor(requireContext(), avatarColor(member.getUserId())));
        avatar.setImageUrl(member.getProfileImageUrl());
        header.findViewById(R.id.memberHostBadge).setVisibility(
                "OWNER".equalsIgnoreCase(member.getRole()) ? View.VISIBLE : View.GONE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        if (list.getChildCount() > 0) params.topMargin = dp(12);
        list.addView(header, params);
    }

    private void bindAvatarStack(List<TripMember> members) {
        LinearLayout stack = binding.checklistAssignmentAvatarStack.avatarStackRoot;
        stack.removeAllViews();
        int visibleCount = Math.min(4, members.size());
        for (int index = 0; index < visibleCount; index++) {
            TripMember member = members.get(index);
            AvatarView avatar = new AvatarView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(28), dp(28));
            if (index > 0) params.setMarginStart(-dp(8));
            avatar.setLayoutParams(params);
            avatar.setInitial(initial(member.getNickname()));
            avatar.setAvatarColor(ContextCompat.getColor(requireContext(), avatarColor(member.getUserId())));
            avatar.setStrokeEnabled(true, ContextCompat.getColor(requireContext(), R.color.bag_bg_base));
            avatar.setImageUrl(member.getProfileImageUrl());
            stack.addView(avatar);
        }
    }

    private void addAssignedRow(LinearLayout list, PackingItem item) {
        View row = inflateRow(list, item.getItemName());
        bindCompletionState(row, item);
        addWithSpacing(list, row);
    }

    private void addUnassignedRow(LinearLayout list, PackingItem item) {
        View row = inflateRow(list, item.getItemName());
        // 지정 항목과 동일한 오른쪽 슬롯을 유지해 행 크기와 텍스트 폭을 맞춘다.
        row.findViewById(R.id.assignmentRowConfirm).setVisibility(View.INVISIBLE);
        addWithSpacing(list, row);
    }

    /**
     * 분담 현황은 상태 확인 화면이므로 여기서는 체크를 변경하지 않는다.
     * 담당자가 내 목록에서 체크하면 서버 재조회 후 같은 PackingItem.completed 값이 반영된다.
     */
    private void bindCompletionState(View row, PackingItem item) {
        CheckboxView checkbox = row.findViewById(R.id.assignmentRowConfirm);
        checkbox.setVisibility(View.VISIBLE);
        checkbox.setState(item.isCompleted() ? CheckboxView.CHECKED : CheckboxView.UNCHECKED);
        checkbox.setClickable(false);
        checkbox.setFocusable(false);
        checkbox.setContentDescription(getString(item.isCompleted()
                ? R.string.checklist_assignment_item_completed
                : R.string.checklist_assignment_item_not_completed));
    }

    private View inflateRow(LinearLayout list, String label) {
        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.molecule_assignment_row, list, false);
        ((TextView) row.findViewById(R.id.assignmentRowLabel)).setText(label);
        return row;
    }

    private void addWithSpacing(LinearLayout list, View row) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        if (list.getChildCount() > 0) params.topMargin = dp(8);
        list.addView(row, params);
    }

    private int avatarColor(long userId) {
        int[] colors = {R.color.bag_avatar_1, R.color.bag_avatar_2, R.color.bag_avatar_4};
        return colors[(int) Math.abs(userId % colors.length)];
    }

    private String initial(String name) {
        return name == null || name.trim().isEmpty() ? "여" : name.trim().substring(0, 1);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        host = null;
        super.onDestroyView();
    }
}
