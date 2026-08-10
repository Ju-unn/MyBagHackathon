package com.example.mybaghackathon.ui.checklist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.FragmentChecklistCommonBinding;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.TripMember;
import com.example.mybaghackathon.ui.atoms.AvatarView;
import com.example.mybaghackathon.ui.atoms.CheckboxView;
import com.example.mybaghackathon.ui.atoms.ChipView;
import com.example.mybaghackathon.ui.atoms.PriorityDotView;
import com.example.mybaghackathon.ui.atoms.RestrictionTagView;
import com.example.mybaghackathon.ui.overlay.AddItemSheet;
import com.example.mybaghackathon.ui.overlay.EditItemSheet;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/** S11 · 공용 체크리스트 탭. */
public class ChecklistCommonFragment extends Fragment implements ChecklistDataConsumer {

    private FragmentChecklistCommonBinding binding;
    private ChecklistHost host;
    private int selectedPriority = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChecklistCommonBinding.inflate(inflater, container, false);
        host = (ChecklistHost) requireActivity();
        bindFilters();
        bindAddButton();
        renderChecklist();
        return binding.getRoot();
    }

    private void bindFilters() {
        binding.checklistFilterAll.setOnClickListener(v -> selectFilter(-1));
        binding.checklistFilterEssential.setOnClickListener(v -> selectFilter(0));
        binding.checklistFilterMid.setOnClickListener(v -> selectFilter(1));
        binding.checklistFilterOptional.setOnClickListener(v -> selectFilter(2));
    }

    private void selectFilter(int priority) {
        selectedPriority = priority;
        ChipView[] chips = {
                binding.checklistFilterAll,
                binding.checklistFilterEssential,
                binding.checklistFilterMid,
                binding.checklistFilterOptional
        };
        for (int index = 0; index < chips.length; index++) {
            chips[index].setActive(index == priority + 1);
        }
        renderChecklist();
    }

    private void bindAddButton() {
        TextView label = binding.checklistCommonAddWrapper.findViewById(R.id.dashedAddCardLabel);
        label.setText(R.string.checklist_add_manual);
        binding.checklistCommonAddWrapper.setVisibility(
                host.isCurrentUserHost() ? View.VISIBLE : View.GONE);
        binding.checklistCommonAddWrapper.setOnClickListener(v -> {
            if (!host.isCurrentUserHost()) {
                return;
            }
            AddItemSheet sheet = new AddItemSheet();
            sheet.setOnItemAddedListener((name, priority) ->
                    host.addChecklistItem(name, priority, "COMMON"));
            sheet.show(getParentFragmentManager(), "add_common_item");
        });
    }

    @Override
    public void renderChecklist() {
        if (binding == null || host == null) {
            return;
        }
        binding.checklistCommonAddWrapper.setVisibility(
                host.isCurrentUserHost() ? View.VISIBLE : View.GONE);
        LinearLayout sections = binding.checklistCommonSections;
        sections.removeAllViews();
        boolean hasVisibleItems = false;
        boolean hasAnyCommonItems = false;
        for (PackingItem item : host.getChecklistItems()) {
            if (ChecklistItemVisibility.isCommon(item)) {
                hasAnyCommonItems = true;
                break;
            }
        }

        for (int priority = 0; priority < 3; priority++) {
            if (selectedPriority >= 0 && selectedPriority != priority) {
                continue;
            }
            List<PackingItem> group = new ArrayList<>();
            for (PackingItem item : host.getChecklistItems()) {
                if (ChecklistItemVisibility.isCommon(item)
                        && priorityLevel(item.getPriority()) == priority) {
                    group.add(item);
                }
            }
            if (!group.isEmpty()) {
                hasVisibleItems = true;
                addSection(sections, priority, priorityLabel(priority), group);
            }
        }

        boolean empty = host.isChecklistLoaded() && !hasVisibleItems;
        binding.checklistCommonEmptyState.getRoot().setVisibility(
                empty ? View.VISIBLE : View.GONE);
        if (empty) {
            bindEmptyState(hasAnyCommonItems);
        }
    }

    private void addSection(LinearLayout sections, int level, String label, List<PackingItem> group) {
        View header = LayoutInflater.from(requireContext())
                .inflate(R.layout.molecule_section_header, sections, false);
        ((PriorityDotView) header.findViewById(R.id.sectionHeaderDot)).setLevel(level);
        ((TextView) header.findViewById(R.id.sectionHeaderLabel))
                .setText(label + " · " + group.size());
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (sections.getChildCount() > 0) {
            headerParams.topMargin = dp(16);
        }
        sections.addView(header, headerParams);

        for (PackingItem item : group) {
            sections.addView(createItemRow(sections, item));
        }
    }

    private View createItemRow(LinearLayout parent, PackingItem item) {
        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.molecule_checklist_item_row, parent, false);
        TextView label = row.findViewById(R.id.checklistItemLabel);
        label.setText(item.getItemName());

        CheckboxView checkbox = row.findViewById(R.id.checklistItemCheckbox);
        checkbox.setState(item.isCompleted() ? CheckboxView.CHECKED : CheckboxView.UNCHECKED);
        checkbox.setEnabled(host.isCurrentUserHost());
        if (host.isCurrentUserHost()) {
            checkbox.setOnCheckChangeListener(state -> host.toggleChecklistItem(item));
        }

        TripMember assignee = findMember(item.getAssigneeUserId());
        if (assignee != null) {
            AvatarView avatar = row.findViewById(R.id.checklistItemAvatar);
            avatar.setVisibility(View.VISIBLE);
            avatar.setInitial(initial(assignee.getNickname()));
            avatar.setAvatarColor(ContextCompat.getColor(requireContext(), avatarColor(assignee.getUserId())));
        }

        bindRestriction(row, item.getRestrictionType());
        bindItemActions(row, item);
        if (host.isCurrentUserHost()) {
            row.setOnClickListener(v -> showAssigneePicker(item));
        }
        return row;
    }

    private void bindItemActions(View row, PackingItem item) {
        View moreButton = row.findViewById(R.id.checklistItemMoreButton);
        if (!host.isCurrentUserHost()) {
            moreButton.setVisibility(View.GONE);
            return;
        }
        moreButton.setVisibility(View.VISIBLE);
        moreButton.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(requireContext(), moreButton);
            menu.inflate(R.menu.checklist_common_host_actions);
            menu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.actionEditChecklistItem) {
                    showEditSheet(item);
                    return true;
                }
                if (menuItem.getItemId() == R.id.actionDeleteChecklistItem) {
                    host.deleteChecklistItemWithUndo(item);
                    return true;
                }
                return false;
            });
            menu.show();
        });
    }

    private void showAssigneePicker(PackingItem item) {
        if (!host.isCurrentUserHost()) {
            return;
        }

        List<TripMember> members = host.getTripMembers();
        String[] labels = new String[members.size() + 1];
        labels[0] = getString(R.string.checklist_assignee_none);
        int checkedIndex = 0;
        for (int index = 0; index < members.size(); index++) {
            TripMember member = members.get(index);
            labels[index + 1] = hasText(member.getNickname())
                    ? member.getNickname().trim()
                    : getString(R.string.checklist_member_fallback);
            if (item.getAssigneeUserId() != null
                    && item.getAssigneeUserId() == member.getUserId()) {
                checkedIndex = index + 1;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.checklist_assign_title, item.getItemName()))
                .setSingleChoiceItems(labels, checkedIndex, (dialog, which) -> {
                    Long assigneeUserId = which == 0 ? null : members.get(which - 1).getUserId();
                    host.assignChecklistItem(item, assigneeUserId);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showEditSheet(PackingItem item) {
        EditItemSheet sheet = EditItemSheet.newInstance(
                item.getItemName(), priorityLevel(item.getPriority()));
        sheet.setOnItemEditedListener(new EditItemSheet.OnItemEditedListener() {
            @Override
            public void onItemRenamed(String newLabel, int priorityLevel) {
                host.updateChecklistItem(item, newLabel, priorityLevel);
            }

            @Override
            public void onItemDeleted() {
                host.deleteChecklistItemWithUndo(item);
            }
        });
        sheet.show(getParentFragmentManager(), "edit_common_item");
    }

    private void bindRestriction(View row, String type) {
        int tagType = restrictionTagType(type);
        if (tagType < 0) {
            return;
        }
        RestrictionTagView tag = row.findViewById(R.id.checklistItemRestrictionTag);
        tag.setVisibility(View.VISIBLE);
        tag.setType(tagType);
    }

    private TripMember findMember(Long userId) {
        if (userId == null) {
            return null;
        }
        for (TripMember member : host.getTripMembers()) {
            if (member.getUserId() == userId) {
                return member;
            }
        }
        return null;
    }

    private void bindEmptyState(boolean filteredOut) {
        binding.checklistCommonEmptyState.emptyStateTitle
                .setText(filteredOut
                        ? R.string.checklist_filter_empty_title
                        : R.string.checklist_common_empty_title);
        binding.checklistCommonEmptyState.emptyStateDesc
                .setText(filteredOut
                        ? R.string.checklist_filter_empty_desc
                        : R.string.checklist_common_empty_desc);
        binding.checklistCommonEmptyState.emptyStateAction.setVisibility(View.GONE);
    }

    private int priorityLevel(String value) {
        if ("RECOMMENDED".equalsIgnoreCase(value)) return 1;
        if ("OPTIONAL".equalsIgnoreCase(value)) return 2;
        return 0;
    }

    private String priorityLabel(int priority) {
        if (priority == 1) return getString(R.string.review_priority_mid);
        if (priority == 2) return getString(R.string.review_priority_low);
        return getString(R.string.review_priority_high);
    }

    private int restrictionTagType(String value) {
        if ("CARRY_ON_ONLY".equalsIgnoreCase(value) || "CABIN_ONLY".equalsIgnoreCase(value)) {
            return RestrictionTagView.CABIN_ONLY;
        }
        if ("CHECKED_ONLY".equalsIgnoreCase(value)) return RestrictionTagView.CHECKED_ONLY;
        if ("PROHIBITED".equalsIgnoreCase(value)) return RestrictionTagView.PROHIBITED;
        return -1;
    }

    private int avatarColor(long userId) {
        int[] colors = {R.color.bag_avatar_1, R.color.bag_avatar_2, R.color.bag_avatar_4};
        return colors[(int) Math.abs(userId % colors.length)];
    }

    private String initial(String name) {
        return name == null || name.trim().isEmpty() ? "여" : name.trim().substring(0, 1);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
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
