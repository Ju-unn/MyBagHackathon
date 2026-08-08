package com.example.mybaghackathon.ui.overlay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.SheetEditFieldBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * S07 필드 수정용 바텀시트 — 입력 필드 1~2개(여행지/숙소/이동수단은 1개,
 * 일정은 시작일/종료일 2개)를 hints 개수만큼 코드에서 채워 넣는 범용 컴포넌트.
 */
public class EditFieldSheet extends BottomSheetDialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_HINTS = "hints";
    private static final String ARG_VALUES = "values";
    private static final String ARG_DATE_FIELDS = "date_fields";

    public interface OnFieldsSavedListener {
        // false를 반환하면 시트를 닫지 않음 (검증 실패 시 계속 열어두기 위함)
        boolean onFieldsSaved(ArrayList<String> values);
    }

    @Nullable
    private OnFieldsSavedListener listener;
    private SheetEditFieldBinding binding;
    private final List<TextInputEditText> inputs = new ArrayList<>();

    public static EditFieldSheet newInstance(String title, ArrayList<String> hints, ArrayList<String> currentValues) {
        return newInstance(title, hints, currentValues, null);
    }

    // dateFields[i] == true인 필드는 키보드 입력 대신 탭하면 달력이 뜨는 날짜 선택 필드가 됨
    public static EditFieldSheet newInstance(String title, ArrayList<String> hints,
                                              ArrayList<String> currentValues, boolean[] dateFields) {
        EditFieldSheet sheet = new EditFieldSheet();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putStringArrayList(ARG_HINTS, hints);
        args.putStringArrayList(ARG_VALUES, currentValues);
        if (dateFields != null) args.putBooleanArray(ARG_DATE_FIELDS, dateFields);
        sheet.setArguments(args);
        return sheet;
    }

    public void setOnFieldsSavedListener(OnFieldsSavedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = SheetEditFieldBinding.inflate(inflater, container, false);

        Bundle args = getArguments();
        ArrayList<String> hints = args != null ? args.getStringArrayList(ARG_HINTS) : new ArrayList<>();
        ArrayList<String> values = args != null ? args.getStringArrayList(ARG_VALUES) : new ArrayList<>();
        boolean[] dateFields = args != null ? args.getBooleanArray(ARG_DATE_FIELDS) : null;
        if (hints == null) hints = new ArrayList<>();
        if (values == null) values = new ArrayList<>();

        binding.editFieldTitle.setText(args != null ? args.getString(ARG_TITLE) : "");

        inputs.clear();
        for (int i = 0; i < hints.size(); i++) {
            View row = inflater.inflate(R.layout.molecule_edit_field_input, binding.editFieldInputContainer, false);
            String hint = hints.get(i);
            TextInputLayout layout = row.findViewById(R.id.editFieldInputLayout);
            layout.setHint(hint);
            TextInputEditText input = row.findViewById(R.id.editFieldInput);
            if (i < values.size()) input.setText(values.get(i));

            if (dateFields != null && i < dateFields.length && dateFields[i]) {
                input.setFocusable(false);
                input.setOnClickListener(v -> showDatePicker(input, hint));
            }

            binding.editFieldInputContainer.addView(row);
            inputs.add(input);
        }

        binding.editFieldCancelButton.setOnClickListener(v -> dismiss());
        binding.editFieldSaveButton.setOnClickListener(v -> {
            ArrayList<String> result = new ArrayList<>();
            for (TextInputEditText input : inputs) {
                result.add(input.getText() != null ? input.getText().toString().trim() : "");
            }
            if (listener == null || listener.onFieldsSaved(result)) dismiss();
        });

        return binding.getRoot();
    }

    // MaterialDatePicker는 UTC 자정 기준 millis를 돌려주므로, 로컬 타임존 오차 없이
    // "yyyy-MM-dd"로 바꾸려면 포맷터도 UTC로 맞춰야 함
    private void showDatePicker(TextInputEditText input, String title) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setTheme(R.style.ThemeOverlay_Bag_DatePicker)
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            iso.setTimeZone(TimeZone.getTimeZone("UTC"));
            input.setText(iso.format(new Date(selection)));
        });
        picker.show(getParentFragmentManager(), "date_picker");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
