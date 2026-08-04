package com.example.mybaghackathon.ui.checklist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.ui.atoms.CheckboxView;

import java.util.Arrays;
import java.util.List;

/** S11 · 내 목록 — items assigned to (or personally added by) the current user. */
public class ChecklistMineFragment extends Fragment {

    private static final List<String> MY_ITEMS = Arrays.asList("여권", "카메라", "상비약");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_checklist_mine, container, false);

        LinearLayout list = root.findViewById(R.id.checklistMineList);
        View emptyState = root.findViewById(R.id.checklistMineEmptyState);

        if (MY_ITEMS.isEmpty()) {
            list.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            for (String item : MY_ITEMS) {
                View row = LayoutInflater.from(getContext()).inflate(R.layout.molecule_checklist_item_row, list, false);
                ((TextView) row.findViewById(R.id.checklistItemLabel)).setText(item);
                ((CheckboxView) row.findViewById(R.id.checklistItemCheckbox)).setState(CheckboxView.UNCHECKED);
                row.findViewById(R.id.checklistItemAvatar).setVisibility(View.GONE);
                list.addView(row);
            }
        }

        return root;
    }
}
