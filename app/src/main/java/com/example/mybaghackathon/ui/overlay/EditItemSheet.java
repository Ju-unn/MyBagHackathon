package com.example.mybaghackathon.ui.overlay;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.SheetEditItemBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

/**
 * BS03 · 개인 항목 수정 — 사용자의 기본 항목 중 하나의 이름을 바꾸거나 삭제함.
 *
 * 기능: 전달받은 기존 라벨을 입력창에 채워주고, 저장/삭제 버튼에 따라
 * 리스너(OnItemEditedListener)로 결과를 알려주는 바텀시트.
 */
public class EditItemSheet extends BottomSheetDialogFragment {

    private static final String ARG_LABEL = "label";

    public interface OnItemEditedListener {
        void onItemRenamed(String newLabel);
        void onItemDeleted();
    }

    @Nullable
    private OnItemEditedListener listener;

    private SheetEditItemBinding binding;

    public static EditItemSheet newInstance(String currentLabel) {
        EditItemSheet sheet = new EditItemSheet();
        Bundle args = new Bundle();
        args.putString(ARG_LABEL, currentLabel);
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
        if (getArguments() != null) {
            nameInput.setText(getArguments().getString(ARG_LABEL, ""));
        }

        binding.editItemDeleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onItemDeleted();
            dismiss();
        });

        binding.editItemSaveButton.setOnClickListener(v -> {
            String label = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            if (TextUtils.isEmpty(label)) return;
            if (listener != null) listener.onItemRenamed(label);
            dismiss();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
