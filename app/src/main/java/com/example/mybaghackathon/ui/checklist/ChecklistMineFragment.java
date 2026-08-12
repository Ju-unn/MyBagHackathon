package com.example.mybaghackathon.ui.checklist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.FragmentChecklistMineBinding;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.ui.atoms.CheckboxView;
import com.example.mybaghackathon.ui.atoms.ChipView;
import com.example.mybaghackathon.ui.atoms.PriorityDotView;
import com.example.mybaghackathon.ui.atoms.RestrictionTagView;
import com.example.mybaghackathon.ui.organisms.SwipeRevealHelper;
import com.example.mybaghackathon.ui.overlay.AddItemSheet;
import com.example.mybaghackathon.ui.overlay.EditItemSheet;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/** S12 · 내 목록. 체크, 수정, 스와이프 삭제와 실행 취소를 제공한다. */
public class ChecklistMineFragment extends Fragment implements ChecklistDataConsumer {

    private FragmentChecklistMineBinding binding;
    private ChecklistHost host;
    private int selectedPriority = -1;
    private final boolean[] expandedPriorities = {true, true, true};
    private final SwipeRevealHelper.Tracker swipeTracker = new SwipeRevealHelper.Tracker();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChecklistMineBinding.inflate(inflater, container, false);
        host = (ChecklistHost) requireActivity();
        bindFilters();
        bindAddButton();
        renderChecklist();
        return binding.getRoot();
    }

    private void bindAddButton() {
        TextView label = binding.checklistMineAddWrapper.findViewById(R.id.dashedAddCardLabel);
        label.setText(R.string.checklist_add_manual);
        binding.checklistMineAddWrapper.setOnClickListener(v -> {
            AddItemSheet sheet = new AddItemSheet();
            sheet.setOnItemAddedListener((name, priority) -> {
                if (containsMineItemName(name)) {
                    Toast.makeText(requireContext(),
                            R.string.checklist_duplicate_item_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                host.addChecklistItem(name, priority, "PERSONAL");
            });
            sheet.show(getParentFragmentManager(), "add_personal_item");
        });
    }

    private void bindFilters() {
        binding.checklistMineFilterAll.setOnClickListener(v -> selectFilter(-1));
        binding.checklistMineFilterEssential.setOnClickListener(v -> selectFilter(0));
        binding.checklistMineFilterMid.setOnClickListener(v -> selectFilter(1));
        binding.checklistMineFilterOptional.setOnClickListener(v -> selectFilter(2));
        selectFilter(-1);
    }

    private void selectFilter(int priority) {
        selectedPriority = priority;
        ChipView[] chips = {
                binding.checklistMineFilterAll,
                binding.checklistMineFilterEssential,
                binding.checklistMineFilterMid,
                binding.checklistMineFilterOptional
        };
        for (int index = 0; index < chips.length; index++) {
            chips[index].setActive(index == priority + 1);
        }
        renderChecklist();
    }

    private boolean containsMineItemName(String name) {
        String target = ChecklistDuplicateDetector.normalizedName(name);
        long currentUserId = host.getCurrentUserId();
        boolean solo = host.getTripMemberCount() <= 1;
        for (PackingItem item : host.getChecklistItems()) {
            if ((ChecklistItemVisibility.isMine(item, currentUserId)
                    || (solo && ChecklistItemVisibility.isCommon(item)))
                    && target.equals(ChecklistDuplicateDetector.normalizedName(item.getItemName()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void renderChecklist() {
        if (binding == null || host == null) {
            return;
        }
        LinearLayout list = binding.checklistMineList;
        SwipeRevealHelper.closeOpenRow(swipeTracker);
        list.removeAllViews();

        long currentUserId = host.getCurrentUserId();
        List<ChecklistDuplicateDetector.ItemGroup> allMyItems =
                ChecklistDuplicateDetector.groupForMine(
                        host.getChecklistItems(), currentUserId,
                        host.getTripMemberCount() <= 1);
        List<ChecklistDuplicateDetector.ItemGroup> myItems = new ArrayList<>();
        for (ChecklistDuplicateDetector.ItemGroup itemGroup : allMyItems) {
            if (selectedPriority < 0 || priorityLevel(
                    itemGroup.getPriorityItem().getPriority()) == selectedPriority) {
                myItems.add(itemGroup);
            }
        }

        boolean empty = host.isChecklistLoaded() && myItems.isEmpty();
        binding.checklistMineEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        list.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            bindEmptyState(!allMyItems.isEmpty());
            return;
        }

        for (int priority = 0; priority < 3; priority++) {
            if (selectedPriority >= 0 && selectedPriority != priority) {
                continue;
            }
            List<ChecklistDuplicateDetector.ItemGroup> sectionItems = new ArrayList<>();
            for (ChecklistDuplicateDetector.ItemGroup itemGroup : myItems) {
                if (priorityLevel(itemGroup.getPriorityItem().getPriority()) == priority) {
                    sectionItems.add(itemGroup);
                }
            }
            if (!sectionItems.isEmpty()) {
                addSection(list, priority, priorityLabel(priority), sectionItems);
            }
        }
    }

    private void addSection(
            LinearLayout list,
            int priority,
            String label,
            List<ChecklistDuplicateDetector.ItemGroup> items
    ) {
        View header = LayoutInflater.from(requireContext())
                .inflate(R.layout.molecule_section_header, list, false);
        ((PriorityDotView) header.findViewById(R.id.sectionHeaderDot)).setLevel(priority);
        ((TextView) header.findViewById(R.id.sectionHeaderLabel)).setText(
                getString(R.string.review_priority_count_format, label, items.size()));

        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        if (list.getChildCount() > 0) {
            headerParams.topMargin = dp(16);
        }
        list.addView(header, headerParams);

        LinearLayout sectionContent = new LinearLayout(requireContext());
        sectionContent.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        sectionContent.setOrientation(LinearLayout.VERTICAL);
        for (ChecklistDuplicateDetector.ItemGroup itemGroup : items) {
            sectionContent.addView(createItemRow(sectionContent, itemGroup));
        }
        list.addView(sectionContent);
        bindSectionToggle(header, sectionContent, priority, label);
    }

    private void bindSectionToggle(
            View header,
            View sectionContent,
            int priority,
            String label
    ) {
        ImageView toggle = header.findViewById(R.id.sectionHeaderToggle);
        Runnable applyState = () -> {
            boolean expanded = expandedPriorities[priority];
            sectionContent.setVisibility(expanded ? View.VISIBLE : View.GONE);
            toggle.setRotation(expanded ? 90f : 0f);
            header.setContentDescription(getString(expanded
                    ? R.string.checklist_section_collapse
                    : R.string.checklist_section_expand, label));
        };
        header.setOnClickListener(v -> {
            expandedPriorities[priority] = !expandedPriorities[priority];
            applyState.run();
        });
        applyState.run();
    }

    private View createItemRow(
            LinearLayout parent,
            ChecklistDuplicateDetector.ItemGroup itemGroup
    ) {
        PackingItem displayItem = itemGroup.getDisplayItem();
        PackingItem completionItem = itemGroup.getCompletionItem();
        View swipeContainer = LayoutInflater.from(requireContext())
                .inflate(R.layout.molecule_checklist_swipe_delete_row, parent, false);
        View row = swipeContainer.findViewById(R.id.checklistSwipeContent);
        row.setBackgroundResource(R.drawable.bg_checklist_item_normal);
        TextView label = row.findViewById(R.id.checklistItemLabel);
        label.setText(displayItem.getItemName());
        updateCompletedStyle(label, completionItem.isCompleted());
        row.findViewById(R.id.checklistItemAvatar).setVisibility(View.GONE);

        CheckboxView checkbox = row.findViewById(R.id.checklistItemCheckbox);
        checkbox.setState(completionItem.isCompleted()
                ? CheckboxView.CHECKED : CheckboxView.UNCHECKED);
        checkbox.setOnCheckChangeListener(state -> host.toggleChecklistItem(completionItem));
        bindRestriction(row, displayItem.getRestrictionType());

        float revealWidth = getResources().getDimension(R.dimen.checklist_delete_reveal_width);
        SwipeRevealHelper.reset(row);
        SwipeRevealHelper.attach(row, revealWidth, swipeTracker);
        row.setOnClickListener(v -> {
            if (SwipeRevealHelper.isOpen(swipeTracker, row)) {
                SwipeRevealHelper.closeOpenRow(swipeTracker);
                return;
            }
            showEditSheet(itemGroup);
        });
        swipeContainer.findViewById(R.id.checklistSwipeDeleteButton).setOnClickListener(v -> {
            SwipeRevealHelper.closeOpenRow(swipeTracker);
            confirmRemoveFromMine(itemGroup);
        });
        return swipeContainer;
    }

    // 병합된 행(개인 기본 물품 + 나에게 배정된 공용 물품)은 공용 물품으로만 취급한다 —
    // 개인 물품 삭제 여부를 물을 필요 없이 곧장 담당 해제로 처리하고, 개인 기본 물품 원본도
    // 함께 삭제해서 담당 해제 후 다시 단독으로 재등장하지 않게 한다.
    private void confirmRemoveFromMine(ChecklistDuplicateDetector.ItemGroup itemGroup) {
        if (itemGroup.isMerged()) {
            showMergedUnassignDialog(itemGroup);
            return;
        }
        confirmRemoveFromMine(itemGroup.getDisplayItem());
    }

    private void showMergedUnassignDialog(ChecklistDuplicateDetector.ItemGroup itemGroup) {
        PackingItem commonItem = itemGroup.getCommonItem();
        PackingItem personalItem = itemGroup.getPersonalItem();
        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Bag_ConfirmDialog)
                .setTitle(R.string.checklist_unassign_title)
                .setMessage(getString(
                        R.string.checklist_merged_unassign_message, commonItem.getItemName()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.checklist_unassign_action,
                        (dialog, which) -> host.removeMergedItem(personalItem, commonItem))
                .show();
    }

    private void confirmRemoveFromMine(PackingItem item) {
        long uid = host.getCurrentUserId();
        if (ChecklistItemVisibility.isAssignedCommon(item, uid)) {
            showUnassignDialog(item);
            return;
        }
        // 1인방은 담당 해제 개념이 없어 AI 미배정 공용도 개인 물품처럼 삭제한다(탭 편집 경로와 동일).
        boolean soloCommon = ChecklistItemVisibility.isCommon(item) && host.getTripMemberCount() <= 1;
        if (!ChecklistItemVisibility.isOwnedPersonal(item, uid) && !soloCommon) {
            return;
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(
                requireContext(), R.style.ThemeOverlay_Bag_ConfirmDialog)
                .setTitle(R.string.checklist_delete_dialog_title)
                .setMessage(getString(R.string.checklist_delete_dialog_message, item.getItemName()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete,
                        (ignored, which) -> removeFromMine(item))
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.clay_600));
    }

    private void showUnassignDialog(PackingItem item) {
        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Bag_ConfirmDialog)
                .setTitle(R.string.checklist_unassign_title)
                .setMessage(getString(R.string.checklist_unassign_message, item.getItemName()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.checklist_unassign_action,
                        (dialog, which) -> removeFromMine(item))
                .show();
    }

    private void showEditSheet(ChecklistDuplicateDetector.ItemGroup itemGroup) {
        PackingItem personalItem = itemGroup.getPersonalItem();
        if (personalItem == null) {
            showCommonItemEditor(itemGroup.getCommonItem());
            return;
        }
        if (!ChecklistItemVisibility.isOwnedPersonal(personalItem, host.getCurrentUserId())) {
            return;
        }
        EditItemSheet sheet = EditItemSheet.newInstance(
                personalItem.getItemName(), priorityLevel(personalItem.getPriority()));
        sheet.setOnItemEditedListener(new EditItemSheet.OnItemEditedListener() {
            @Override
            public void onItemRenamed(String newLabel, int priorityLevel) {
                host.updateChecklistItem(personalItem, newLabel, priorityLevel);
            }

            @Override
            public void onItemDeleted() {
                if (itemGroup.isMerged()) {
                    showMergedUnassignDialog(itemGroup);
                } else {
                    removeFromMine(personalItem);
                }
            }
        });
        sheet.show(getParentFragmentManager(), "edit_personal_item");
    }

    // 참여자가 1명뿐이면 "담당 해제"라는 개념이 성립하지 않는다(나 말고 배정할 사람이 없음) —
    // AI 추천 등 공용 항목도 개인 항목과 동일하게 오버레이로 이름 수정/삭제하게 한다.
    // 참여자가 2명 이상일 때만 기존 담당 해제 확인창을 띄운다.
    private void showCommonItemEditor(PackingItem commonItem) {
        if (host.getTripMemberCount() > 1) {
            showUnassignDialog(commonItem);
            return;
        }
        EditItemSheet sheet = EditItemSheet.newInstance(
                commonItem.getItemName(), priorityLevel(commonItem.getPriority()));
        sheet.setOnItemEditedListener(new EditItemSheet.OnItemEditedListener() {
            @Override
            public void onItemRenamed(String newLabel, int priorityLevel) {
                host.updateChecklistItem(commonItem, newLabel, priorityLevel);
            }

            @Override
            public void onItemDeleted() {
                host.deleteChecklistItemWithUndo(commonItem);
            }
        });
        sheet.show(getParentFragmentManager(), "edit_common_item_solo");
    }

    private void removeFromMine(PackingItem item) {
        long currentUserId = host.getCurrentUserId();
        if (ChecklistItemVisibility.isAssignedCommon(item, currentUserId)) {
            host.assignChecklistItem(item, null);
        } else if (ChecklistItemVisibility.isOwnedPersonal(item, currentUserId)) {
            // packing_items의 여행 체크리스트 항목만 삭제한다.
            // 프로필의 user_default_items 원본은 별도 저장소이므로 변경되지 않는다.
            host.deleteChecklistItemWithUndo(item);
        } else if (ChecklistItemVisibility.isCommon(item) && host.getTripMemberCount() <= 1) {
            // 1인방 미배정 공용(AI 추천) 삭제 — 담당 해제 대신 항목 자체를 삭제한다.
            host.deleteChecklistItemWithUndo(item);
        }
    }

    private void bindEmptyState(boolean filteredOut) {
        TextView title = binding.checklistMineEmptyState.findViewById(R.id.emptyStateTitle);
        TextView description = binding.checklistMineEmptyState.findViewById(R.id.emptyStateDesc);
        View action = binding.checklistMineEmptyState.findViewById(R.id.emptyStateAction);
        title.setText(filteredOut
                ? R.string.checklist_filter_empty_title
                : R.string.checklist_mine_empty_title);
        description.setText(filteredOut
                ? R.string.checklist_filter_empty_desc
                : R.string.checklist_mine_empty_desc);
        action.setVisibility(View.GONE);
    }

    private void bindRestriction(View row, String type) {
        int tagType = restrictionTagType(type);
        if (tagType < 0) return;
        RestrictionTagView tag = row.findViewById(R.id.checklistItemRestrictionTag);
        tag.setVisibility(View.VISIBLE);
        tag.setType(tagType);
    }

    private void updateCompletedStyle(TextView label, boolean completed) {
        label.setTextColor(ContextCompat.getColor(requireContext(), completed
                ? R.color.bag_text_tertiary_safe
                : R.color.bag_text_primary));
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
