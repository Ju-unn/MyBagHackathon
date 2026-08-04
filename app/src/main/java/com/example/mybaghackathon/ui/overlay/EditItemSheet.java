package com.example.mybaghackathon.ui.overlay;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mybaghackathon.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

/** BS03 · Edit Personal Item — rename or remove one of the user's own default items. */
public class EditItemSheet extends BottomSheetDialogFragment {

    private static final String ARG_LABEL = "label";

    public interface OnItemEditedListener {
        void onItemRenamed(String newLabel);
        void onItemDeleted();
    }

    @Nullable
    private OnItemEditedListener listener;

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
        View root = inflater.inflate(R.layout.sheet_edit_item, container, false);

        TextInputEditText nameInput = root.findViewById(R.id.editItemNameInput);
        if (getArguments() != null) {
            nameInput.setText(getArguments().getString(ARG_LABEL, ""));
        }

        root.findViewById(R.id.editItemDeleteButton).setOnClickListener(v -> {
            if (listener != null) listener.onItemDeleted();
            dismiss();
        });

        root.findViewById(R.id.editItemSaveButton).setOnClickListener(v -> {
            String label = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            if (TextUtils.isEmpty(label)) return;
            if (listener != null) listener.onItemRenamed(label);
            dismiss();
        });

        return root;
    }
}
