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
import com.example.mybaghackathon.ui.overlay.AssignItemSheet;
import com.example.mybaghackathon.ui.overlay.EditItemSheet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** S11 · 공용 체크리스트 탭. */
public class ChecklistCommonFragment extends Fragment implements ChecklistDataConsumer {

    private FragmentChecklistCommonBinding binding;
    private ChecklistHost host;
    private int selectedPriority = -1;
    // 같은 이름으로 여러 명에게 배정된(row가 인원 수만큼 나뉜) 공용 물품을 화면에서 한 줄로 묶어
    // 보여주기 위한 맵 — 대표(첫 번째) packing_item_id -> 그룹 전체. renderChecklist()마다 새로 채워짐.
    private final Map<Long, List<PackingItem>> groupsByAnchorId = new LinkedHashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(
                AssignItemSheet.RESULT_REQUEST_KEY,
                this,
                (requestKey, result) -> {
                    if (host == null) {
                        return;
                    }
                    long itemId = result.getLong(AssignItemSheet.RESULT_ITEM_ID, -1L);
                    long[] selectedIds = result.getLongArray(AssignItemSheet.RESULT_SELECTED_IDS);
                    List<Long> userIds = boxedIds(selectedIds);

                    List<PackingItem> group = groupsByAnchorId.get(itemId);
                    if (group != null) {
                        host.reassignGroupedItem(group, userIds);
                        return;
                    }
                    PackingItem item = findItem(itemId);
                    if (item == null) {
                        return;
                    }
                    if (userIds.size() > 1) {
                        host.assignChecklistItems(item, userIds);
                    } else {
                        host.assignChecklistItem(item, userIds.isEmpty() ? null : userIds.get(0));
                    }
                });
    }

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
        groupsByAnchorId.clear();
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

    private void addSection(LinearLayout sections, int level, String label, List<PackingItem> items) {
        List<List<PackingItem>> groups = groupAssignmentRows(items);

        View header = LayoutInflater.from(requireContext())
                .inflate(R.layout.molecule_section_header, sections, false);
        ((PriorityDotView) header.findViewById(R.id.sectionHeaderDot)).setLevel(level);
        ((TextView) header.findViewById(R.id.sectionHeaderLabel))
                .setText(label + " · " + groups.size());
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (sections.getChildCount() > 0) {
            headerParams.topMargin = dp(16);
        }
        sections.addView(header, headerParams);

        for (List<PackingItem> group : groups) {
            sections.addView(createItemRow(sections, group));
        }
    }

    // 다중 배정으로 복제된 row만 한 줄로 묶는다. 이름만 같고 별도로 추가한 물품은 분리한다.
    private List<List<PackingItem>> groupAssignmentRows(List<PackingItem> items) {
        Map<String, List<PackingItem>> byAssignment = new LinkedHashMap<>();
        for (PackingItem item : items) {
            String key = assignmentGroupKey(item);
            byAssignment.computeIfAbsent(key, ignored -> new ArrayList<>()).add(item);
        }
        return new ArrayList<>(byAssignment.values());
    }

    private String assignmentGroupKey(PackingItem item) {
        return ChecklistDuplicateDetector.normalizedName(item.getItemName())
                + '|' + item.getCreatedByUserId()
                + '|' + item.getSortOrder()
                + '|' + normalizedField(item.getPriority())
                + '|' + normalizedField(item.getCategory())
                + '|' + normalizedField(item.getSource())
                + '|' + normalizedField(item.getRestrictionType())
                + '|' + normalizedField(item.getRestrictionReason());
    }

    private String normalizedField(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private View createItemRow(LinearLayout parent, List<PackingItem> group) {
        PackingItem first = group.get(0);
        boolean grouped = group.size() > 1;

        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.molecule_checklist_item_row, parent, false);
        TextView label = row.findViewById(R.id.checklistItemLabel);
        label.setText(first.getItemName());

        boolean allCompleted = true;
        for (PackingItem item : group) {
            if (!item.isCompleted()) {
                allCompleted = false;
                break;
            }
        }
        boolean targetCompleted = !allCompleted;
        CheckboxView checkbox = row.findViewById(R.id.checklistItemCheckbox);
        checkbox.setState(allCompleted ? CheckboxView.CHECKED : CheckboxView.UNCHECKED);
        checkbox.setEnabled(host.isCurrentUserHost());
        if (host.isCurrentUserHost()) {
            checkbox.setOnCheckChangeListener(state -> {
                for (PackingItem item : group) {
                    if (item.isCompleted() != targetCompleted) {
                        host.toggleChecklistItem(item);
                    }
                }
            });
        }

        List<TripMember> assignees = new ArrayList<>();
        for (PackingItem item : group) {
            TripMember member = findMember(item.getAssigneeUserId());
            if (member != null && !containsMember(assignees, member.getUserId())) {
                assignees.add(member);
            }
        }
        if (grouped) {
            bindAvatarStack(row, assignees);
            groupsByAnchorId.put(first.getPackingItemId(), group);
        } else if (!assignees.isEmpty()) {
            TripMember assignee = assignees.get(0);
            AvatarView avatar = row.findViewById(R.id.checklistItemAvatar);
            avatar.setVisibility(View.VISIBLE);
            avatar.setInitial(initial(assignee.getNickname()));
            avatar.setAvatarColor(ContextCompat.getColor(requireContext(), avatarColor(assignee.getUserId())));
        }

        bindRestriction(row, first.getRestrictionType());
        bindItemActions(row, group);
        if (host.isCurrentUserHost()) {
            row.setOnClickListener(v -> showAssigneePicker(group));
        }
        return row;
    }

    // 여러 명에게 배정된 물품 — 아바타를 최대 2개까지 겹쳐 보여주고, 그 이상은 "+N"으로 표시
    private void bindAvatarStack(View row, List<TripMember> assignees) {
        LinearLayout stack = row.findViewById(R.id.checklistItemAvatarGroup);
        stack.removeAllViews();
        if (assignees.isEmpty()) {
            return;
        }
        stack.setVisibility(View.VISIBLE);

        int strokeColor = ContextCompat.getColor(requireContext(), R.color.bag_bg_surface);
        int shown = Math.min(assignees.size(), 2);
        for (int index = 0; index < shown; index++) {
            TripMember member = assignees.get(index);
            stack.addView(buildStackedAvatar(
                    initial(member.getNickname()),
                    ContextCompat.getColor(requireContext(), avatarColor(member.getUserId())),
                    strokeColor,
                    index > 0));
        }
        int remaining = assignees.size() - shown;
        if (remaining > 0) {
            stack.addView(buildStackedAvatar(
                    "+" + remaining,
                    ContextCompat.getColor(requireContext(), R.color.bag_text_secondary),
                    strokeColor,
                    true));
        }
    }

    private AvatarView buildStackedAvatar(String initial, int color, int strokeColor, boolean overlap) {
        AvatarView avatar = new AvatarView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(26), dp(26));
        if (overlap) {
            params.leftMargin = dp(-10);
        }
        avatar.setLayoutParams(params);
        avatar.setInitial(initial);
        avatar.setAvatarColor(color);
        avatar.setStrokeEnabled(true, strokeColor);
        return avatar;
    }

    private boolean containsMember(List<TripMember> members, long userId) {
        for (TripMember member : members) {
            if (member.getUserId() == userId) {
                return true;
            }
        }
        return false;
    }

    private void bindItemActions(View row, List<PackingItem> group) {
        View moreButton = row.findViewById(R.id.checklistItemMoreButton);
        if (!host.isCurrentUserHost()) {
            moreButton.setVisibility(View.GONE);
            return;
        }
        boolean grouped = group.size() > 1;
        moreButton.setVisibility(View.VISIBLE);
        moreButton.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(requireContext(), moreButton);
            menu.inflate(R.menu.checklist_common_host_actions);
            if (grouped) {
                // 그룹 전체를 대상으로 이름/우선순위를 부분 수정하는 건 애매해서 이번엔 삭제만 지원
                menu.getMenu().findItem(R.id.actionEditChecklistItem).setVisible(false);
            }
            menu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.actionEditChecklistItem) {
                    showEditSheet(group.get(0));
                    return true;
                }
                if (menuItem.getItemId() == R.id.actionDeleteChecklistItem) {
                    for (PackingItem item : group) {
                        host.deleteChecklistItem(item);
                    }
                    return true;
                }
                return false;
            });
            menu.show();
        });
    }

    private void showAssigneePicker(List<PackingItem> group) {
        if (!host.isCurrentUserHost()) {
            return;
        }

        List<TripMember> members = host.getTripMembers();
        List<Long> selectedIds = new ArrayList<>();
        for (PackingItem item : group) {
            Long assigneeId = item.getAssigneeUserId();
            if (assigneeId != null && !selectedIds.contains(assigneeId)) {
                selectedIds.add(assigneeId);
            }
        }

        PackingItem anchor = group.get(0);
        AssignItemSheet sheet = AssignItemSheet.newInstance(
                anchor.getPackingItemId(), anchor.getItemName(), members, selectedIds);
        sheet.show(getParentFragmentManager(), "assign_common_item");
    }

    private PackingItem findItem(long itemId) {
        for (PackingItem item : host.getChecklistItems()) {
            if (item.getPackingItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    private List<Long> boxedIds(@Nullable long[] ids) {
        List<Long> result = new ArrayList<>();
        if (ids != null) {
            for (long id : ids) {
                result.add(id);
            }
        }
        return result;
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
                host.deleteChecklistItem(item);
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
