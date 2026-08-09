package com.example.mybaghackathon.ui.checklist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.FragmentChecklistMineBinding;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.ui.atoms.CheckboxView;
import com.example.mybaghackathon.ui.atoms.RestrictionTagView;
import com.example.mybaghackathon.ui.overlay.AddItemSheet;

import java.util.ArrayList;
import java.util.List;

/** S12 · 내 목록. 체크, 수정, 스와이프 삭제와 실행 취소를 제공한다. */
public class ChecklistMineFragment extends Fragment implements ChecklistDataConsumer {

    private FragmentChecklistMineBinding binding;
    private ChecklistHost host;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChecklistMineBinding.inflate(inflater, container, false);
        host = (ChecklistHost) requireActivity();
        bindAddButton();
        renderChecklist();
        return binding.getRoot();
    }

    private void bindAddButton() {
        TextView label = binding.checklistMineAddWrapper.findViewById(R.id.dashedAddCardLabel);
        label.setText(R.string.checklist_add_manual);
        binding.checklistMineAddWrapper.setOnClickListener(v -> {
            AddItemSheet sheet = new AddItemSheet();
            sheet.setOnItemAddedListener((name, priority) ->
                    host.addChecklistItem(name, priority, "PERSONAL"));
            sheet.show(getParentFragmentManager(), "add_personal_item");
        });
    }

    @Override
    public void renderChecklist() {
        if (binding == null || host == null) {
            return;
        }
        LinearLayout list = binding.checklistMineList;
        list.removeAllViews();

        List<PackingItem> myItems = new ArrayList<>();
        long currentUserId = host.getCurrentUserId();
        for (PackingItem item : host.getChecklistItems()) {
            boolean personal = "PERSONAL".equalsIgnoreCase(item.getScope());
            boolean assignedToMe = item.getAssigneeUserId() != null
                    && item.getAssigneeUserId() == currentUserId;
            if (personal || assignedToMe) {
                myItems.add(item);
            }
        }

        boolean empty = host.isChecklistLoaded() && myItems.isEmpty();
        binding.checklistMineEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        list.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            bindEmptyState();
            return;
        }

        for (PackingItem item : myItems) {
            list.addView(createItemRow(list, item));
        }
    }

    private View createItemRow(LinearLayout parent, PackingItem item) {
        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.molecule_checklist_item_row, parent, false);
        TextView label = row.findViewById(R.id.checklistItemLabel);
        label.setText(item.getItemName());
        updateCompletedStyle(label, item.isCompleted());
        row.findViewById(R.id.checklistItemAvatar).setVisibility(View.GONE);

        CheckboxView checkbox = row.findViewById(R.id.checklistItemCheckbox);
        checkbox.setState(item.isCompleted() ? CheckboxView.CHECKED : CheckboxView.UNCHECKED);
        checkbox.setOnCheckChangeListener(state -> host.toggleChecklistItem(item));
        bindRestriction(row, item.getRestrictionType());
        bindItemActions(row, item);

        row.setOnLongClickListener(v -> {
            showEditSheet(item);
            return true;
        });
        attachSwipeDelete(row, item);
        return row;
    }

    private void bindItemActions(View row, PackingItem item) {
        View moreButton = row.findViewById(R.id.checklistItemMoreButton);
        moreButton.setVisibility(View.VISIBLE);
        moreButton.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(requireContext(), moreButton);
            menu.inflate(R.menu.checklist_item_actions);
            menu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.actionDeleteChecklistItem) {
                    host.deleteChecklistItemWithUndo(item);
                    return true;
                }
                return false;
            });
            menu.show();
        });
    }

    private void attachSwipeDelete(View row, PackingItem item) {
        int touchSlop = ViewConfiguration.get(requireContext()).getScaledTouchSlop();
        row.setClickable(true);
        row.setOnTouchListener(new View.OnTouchListener() {
            private float downX;
            private float downY;
            private boolean swiping;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        swiping = false;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getX() - downX;
                        float deltaY = event.getY() - downY;
                        if (!swiping && Math.abs(deltaX) > touchSlop
                                && Math.abs(deltaX) > Math.abs(deltaY)) {
                            swiping = true;
                            view.getParent().requestDisallowInterceptTouchEvent(true);
                        }
                        if (swiping) {
                            view.setTranslationX(deltaX);
                            view.setAlpha(Math.max(0.35f,
                                    1f - Math.abs(deltaX) / Math.max(1f, view.getWidth())));
                            return true;
                        }
                        return false;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (!swiping) {
                            return false;
                        }
                        float distance = event.getX() - downX;
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        if (event.getActionMasked() == MotionEvent.ACTION_UP
                                && Math.abs(distance) >= view.getWidth() * 0.35f) {
                            host.deleteChecklistItemWithUndo(item);
                        } else {
                            view.animate().translationX(0f).alpha(1f).setDuration(160L).start();
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void showEditSheet(PackingItem item) {
        AddItemSheet sheet = AddItemSheet.newEditInstance(
                item.getItemName(), priorityLevel(item.getPriority()));
        sheet.setOnItemAddedListener((name, priority) ->
                host.updateChecklistItem(item, name, priority));
        sheet.show(getParentFragmentManager(), "edit_personal_item");
    }

    private void bindEmptyState() {
        TextView title = binding.checklistMineEmptyState.findViewById(R.id.emptyStateTitle);
        TextView description = binding.checklistMineEmptyState.findViewById(R.id.emptyStateDesc);
        View action = binding.checklistMineEmptyState.findViewById(R.id.emptyStateAction);
        title.setText("내 준비물이 아직 없어요");
        description.setText("아래 버튼을 눌러 준비물을 추가해 보세요.");
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

    private int restrictionTagType(String value) {
        if ("CARRY_ON_ONLY".equalsIgnoreCase(value) || "CABIN_ONLY".equalsIgnoreCase(value)) {
            return RestrictionTagView.CABIN_ONLY;
        }
        if ("CHECKED_ONLY".equalsIgnoreCase(value)) return RestrictionTagView.CHECKED_ONLY;
        if ("PROHIBITED".equalsIgnoreCase(value)) return RestrictionTagView.PROHIBITED;
        return -1;
    }

    @Override
    public void onDestroyView() {
        binding = null;
        host = null;
        super.onDestroyView();
    }
}
