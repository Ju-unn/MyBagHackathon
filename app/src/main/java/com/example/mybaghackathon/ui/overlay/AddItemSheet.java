package com.example.mybaghackathon.ui.overlay;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.SheetAddItemBinding;
import com.example.mybaghackathon.ui.atoms.ChipView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

/**
 * BS02 · 항목 추가 — 우선순위를 지정해서 짐 항목을 수동으로 추가하는 바텀시트.
 *
 * 기능: 이름 입력 + 우선순위 칩(높음/중간/낮음) 중 하나를 고르게 하고,
 * 저장 시 리스너(OnItemAddedListener)로 값을 전달한 뒤 닫히는 바텀시트.
 */
public class AddItemSheet extends BottomSheetDialogFragment {

    private static final int MAX_ITEM_NAME_LENGTH = 15;

    private static final String ARG_ITEM_NAME = "item_name";
    private static final String ARG_PRIORITY = "priority";
    private static final String ARG_EDIT_MODE = "edit_mode";

    /** Implemented by the host screen to receive the new item. */
    public interface OnItemAddedListener {
        void onItemAdded(String label, int priorityLevel);
    }

    @Nullable
    private OnItemAddedListener listener;

    private SheetAddItemBinding binding;

    public void setOnItemAddedListener(OnItemAddedListener listener) {
        this.listener = listener;
    }

    public static AddItemSheet newEditInstance(String itemName, int priorityLevel) {
        AddItemSheet sheet = new AddItemSheet();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_ITEM_NAME, itemName);
        arguments.putInt(ARG_PRIORITY, priorityLevel);
        arguments.putBoolean(ARG_EDIT_MODE, true);
        sheet.setArguments(arguments);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = SheetAddItemBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextInputEditText nameInput = binding.addItemNameInput;
        ChipView high = binding.addItemPriorityHigh;
        ChipView mid = binding.addItemPriorityMid;
        ChipView low = binding.addItemPriorityLow;

        Bundle arguments = getArguments();
        if (arguments != null && arguments.getBoolean(ARG_EDIT_MODE, false)) {
            binding.addItemTitle.setText(R.string.bs_edit_item_title);
            binding.addItemSaveButton.setText(R.string.action_save);
            nameInput.setText(arguments.getString(ARG_ITEM_NAME, ""));
            int priority = arguments.getInt(ARG_PRIORITY, 0);
            selectPriority(high, mid, low, priority == 1 ? mid : (priority == 2 ? low : high));
        }

        high.setOnClickListener(v -> selectPriority(high, mid, low, high));
        mid.setOnClickListener(v -> selectPriority(high, mid, low, mid));
        low.setOnClickListener(v -> selectPriority(high, mid, low, low));

        nameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_ITEM_NAME_LENGTH)});
        binding.addItemNameLengthNotice.setText(getString(R.string.item_name_too_long, MAX_ITEM_NAME_LENGTH));
        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.addItemNameLengthNotice.setVisibility(
                        s.length() >= MAX_ITEM_NAME_LENGTH ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.addItemSaveButton.setOnClickListener(v -> {
            String label = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            if (TextUtils.isEmpty(label)) {
                Toast.makeText(getContext(), R.string.item_name_required, Toast.LENGTH_SHORT).show();
                return;
            }

            int priority = high.isActive() ? 0 : (mid.isActive() ? 1 : 2);
            if (listener != null) listener.onItemAdded(label, priority);
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
