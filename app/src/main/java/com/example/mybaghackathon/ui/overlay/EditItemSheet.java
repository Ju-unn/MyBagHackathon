package com.example.mybaghackathon.ui.overlay;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mybaghackathon.databinding.SheetEditItemBinding;
import com.example.mybaghackathon.ui.atoms.ChipView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

/**
 * BS03 · 개인 항목 수정 — 사용자의 기본 항목 중 하나의 이름/우선순위를 바꾸거나 삭제함.
 *
 * 기능: 전달받은 기존 라벨과 우선순위를 입력창/칩에 채워주고, 저장/삭제 버튼에
 * 따라 리스너(OnItemEditedListener)로 결과를 알려주는 바텀시트.
 */
public class EditItemSheet extends BottomSheetDialogFragment {

    private static final String ARG_LABEL = "label";
    private static final String ARG_PRIORITY = "priority";

    public interface OnItemEditedListener {
        void onItemRenamed(String newLabel, int priorityLevel);
        void onItemDeleted();
    }

    @Nullable
    private OnItemEditedListener listener;

    private SheetEditItemBinding binding;

    public static EditItemSheet newInstance(String currentLabel, int currentPriorityLevel) {
        EditItemSheet sheet = new EditItemSheet();
        Bundle args = new Bundle();
        args.putString(ARG_LABEL, currentLabel);
        args.putInt(ARG_PRIORITY, currentPriorityLevel);
        sheet.setArguments(args);
        return sheet;
    }

    public void setOnItemEditedListener(OnItemEditedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = SheetEditItemBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextInputEditText nameInput = binding.editItemNameInput;
        ChipView high = binding.editItemPriorityHigh;
        ChipView mid = binding.editItemPriorityMid;
        ChipView low = binding.editItemPriorityLow;

        if (getArguments() != null) {
            nameInput.setText(getArguments().getString(ARG_LABEL, ""));
            int priority = getArguments().getInt(ARG_PRIORITY, 0);
            selectPriority(high, mid, low, priority == 1 ? mid : (priority == 2 ? low : high));
        }

        high.setOnClickListener(v -> selectPriority(high, mid, low, high));
        mid.setOnClickListener(v -> selectPriority(high, mid, low, mid));
        low.setOnClickListener(v -> selectPriority(high, mid, low, low));

        binding.editItemDeleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onItemDeleted();
            dismiss();
        });

        binding.editItemSaveButton.setOnClickListener(v -> {
            String label = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            if (TextUtils.isEmpty(label)) return;
            int priority = high.isActive() ? 0 : (mid.isActive() ? 1 : 2);
            if (listener != null) listener.onItemRenamed(label, priority);
            dismiss();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void selectPriority(ChipView high, ChipView mid, ChipView low, ChipView selected) {
        high.setActive(high == selected);
        mid.setActive(mid == selected);
        low.setActive(low == selected);
    }
}
