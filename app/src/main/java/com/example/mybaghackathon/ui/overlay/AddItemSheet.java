package com.example.mybaghackathon.ui.overlay;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mybaghackathon.R;
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

    /** Implemented by the host screen to receive the new item. */
    public interface OnItemAddedListener {
        void onItemAdded(String label, int priorityLevel);
    }

    @Nullable
    private OnItemAddedListener listener;

    public void setOnItemAddedListener(OnItemAddedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sheet_add_item, container, false);

        TextInputEditText nameInput = root.findViewById(R.id.addItemNameInput);
        ChipView high = root.findViewById(R.id.addItemPriorityHigh);
        ChipView mid = root.findViewById(R.id.addItemPriorityMid);
        ChipView low = root.findViewById(R.id.addItemPriorityLow);

        high.setOnClickListener(v -> selectPriority(high, mid, low, high));
        mid.setOnClickListener(v -> selectPriority(high, mid, low, mid));
        low.setOnClickListener(v -> selectPriority(high, mid, low, low));

        root.findViewById(R.id.addItemSaveButton).setOnClickListener(v -> {
            String label = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            if (TextUtils.isEmpty(label)) return;

            int priority = high.isActive() ? 0 : (mid.isActive() ? 1 : 2);
            if (listener != null) listener.onItemAdded(label, priority);
            dismiss();
        });

        return root;
    }

    private void selectPriority(ChipView high, ChipView mid, ChipView low, ChipView selected) {
        high.setActive(high == selected);
        mid.setActive(mid == selected);
        low.setActive(low == selected);
    }
}
