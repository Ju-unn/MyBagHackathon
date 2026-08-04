package com.example.mybaghackathon.ui.checklist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.ui.atoms.AvatarView;

/**
 * S12 · 분담 현황 — 어떤 항목을 누가 담당하는지, 그리고 아직 배정되지 않은
 * 항목이 무엇인지 보여줌.
 *
 * 기능: 하드코딩된 배정/미배정 항목 목록을 각각 리스트에 뿌려주고, 배정된
 * 행에는 담당자 아바타(이니셜+색상)와 확인 여부를 표시해주는 화면.
 */
public class ChecklistAssignmentFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_checklist_assignment, container, false);

        LinearLayout assigned = root.findViewById(R.id.checklistAssignmentList);
        addRow(assigned, "여권", "나", R.color.bag_avatar_2, true);
        addRow(assigned, "항공권", "민지", R.color.bag_avatar_1, true);
        addRow(assigned, "우산", "유진", R.color.bag_avatar_4, false);

        LinearLayout unassigned = root.findViewById(R.id.checklistUnassignedList);
        addUnassignedRow(unassigned, "충전기");
        addUnassignedRow(unassigned, "선글라스");

        return root;
    }

    private void addRow(LinearLayout list, String label, String initial, int colorRes, boolean confirmed) {
        View row = LayoutInflater.from(getContext()).inflate(R.layout.molecule_assignment_row, list, false);
        ((TextView) row.findViewById(R.id.assignmentRowLabel)).setText(label);
        AvatarView avatar = row.findViewById(R.id.assignmentRowAvatar);
        avatar.setInitial(initial);
        avatar.setAvatarColor(ContextCompat.getColor(requireContext(), colorRes));
        if (confirmed) {
            row.findViewById(R.id.assignmentRowConfirm).setBackgroundResource(R.drawable.bg_checkbox_checked);
        }
        addWithSpacing(list, row);
    }

    private void addUnassignedRow(LinearLayout list, String label) {
        View row = LayoutInflater.from(getContext()).inflate(R.layout.molecule_assignment_row, list, false);
        ((TextView) row.findViewById(R.id.assignmentRowLabel)).setText(label);
        row.findViewById(R.id.assignmentRowAvatar).setVisibility(View.GONE);
        addWithSpacing(list, row);
    }

    private void addWithSpacing(LinearLayout list, View row) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (list.getChildCount() > 0) lp.topMargin = dp(8);
        list.addView(row, lp);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
