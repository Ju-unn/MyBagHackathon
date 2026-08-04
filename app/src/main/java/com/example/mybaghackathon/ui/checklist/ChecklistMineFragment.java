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
import com.example.mybaghackathon.databinding.FragmentChecklistMineBinding;
import com.example.mybaghackathon.ui.atoms.CheckboxView;

import java.util.Arrays;
import java.util.List;

/**
 * S11 · 내 목록 — 현재 사용자에게 배정되었거나 개인적으로 추가한 항목들.
 *
 * 기능: 내 항목 목록을 체크리스트 행으로 렌더링하고, 항목이 하나도 없으면
 * 빈 상태(Empty State) 뷰를 대신 보여주는 화면.
 */
public class ChecklistMineFragment extends Fragment {

    private static final List<String> MY_ITEMS = Arrays.asList("여권", "카메라", "상비약");

    private FragmentChecklistMineBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentChecklistMineBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LinearLayout list = binding.checklistMineList;
        View emptyState = binding.checklistMineEmptyState;

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
