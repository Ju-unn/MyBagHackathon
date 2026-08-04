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
import com.example.mybaghackathon.data.ChecklistItem;
import com.example.mybaghackathon.ui.atoms.AvatarView;
import com.example.mybaghackathon.ui.atoms.CheckboxView;
import com.example.mybaghackathon.ui.atoms.PriorityDotView;
import com.example.mybaghackathon.ui.atoms.RestrictionTagView;
import com.example.mybaghackathon.ui.overlay.AddItemSheet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * S10 · 공용 리스트 — 방에 있는 모든 짐 항목을 우선순위별로 그룹지어 보여줌.
 *
 * 기능: 우선순위(높음/중간/낮음) 섹션별로 헤더 + 체크리스트 행을 동적으로
 * 생성하고, 담당자 아바타/제한 태그를 조건에 따라 표시하며, "직접 추가" 클릭 시
 * AddItemSheet을 띄워 새 항목을 목록에 반영하는 화면.
 */
public class ChecklistCommonFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_checklist_common, container, false);

        final LinearLayout sections = root.findViewById(R.id.checklistCommonSections);

        ChecklistItem passport = new ChecklistItem("여권", 0);
        passport.assigneeInitial = "나";

        ChecklistItem powerBank = new ChecklistItem("보조배터리", 0);
        powerBank.restrictionType = RestrictionTagView.CABIN_ONLY;

        addSection(sections, 0, getString(R.string.review_priority_high),
                Arrays.asList(passport, powerBank, new ChecklistItem("항공권", 0)));
        addSection(sections, 1, getString(R.string.review_priority_mid),
                Arrays.asList(new ChecklistItem("우산", 1), new ChecklistItem("충전기", 1)));
        addSection(sections, 2, getString(R.string.review_priority_low),
                Arrays.asList(new ChecklistItem("선글라스", 2)));

        TextView addLabel = root.findViewById(R.id.checklistCommonAddWrapper).findViewById(R.id.dashedAddCardLabel);
        addLabel.setText(R.string.checklist_add_manual);
        root.findViewById(R.id.checklistCommonAddWrapper).setOnClickListener(v -> {
            AddItemSheet sheet = new AddItemSheet();
            sheet.setOnItemAddedListener((label, priority) -> {
                String[] labels = {getString(R.string.review_priority_high),
                        getString(R.string.review_priority_mid), getString(R.string.review_priority_low)};
                addSection(sections, priority, labels[priority], Collections.singletonList(new ChecklistItem(label, priority)));
            });
            sheet.show(getParentFragmentManager(), "add_item");
        });

        return root;
    }

    private void addSection(LinearLayout sections, int level, String label, List<ChecklistItem> items) {
        View header = LayoutInflater.from(getContext()).inflate(R.layout.molecule_section_header, sections, false);
        ((PriorityDotView) header.findViewById(R.id.sectionHeaderDot)).setLevel(level);
        ((TextView) header.findViewById(R.id.sectionHeaderLabel)).setText(label + " · " + items.size());
        LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (sections.getChildCount() > 0) headerLp.topMargin = dp(16);
        sections.addView(header, headerLp);

        for (ChecklistItem item : items) {
            View row = LayoutInflater.from(getContext()).inflate(R.layout.molecule_checklist_item_row, sections, false);
            ((TextView) row.findViewById(R.id.checklistItemLabel)).setText(item.label);
            ((CheckboxView) row.findViewById(R.id.checklistItemCheckbox)).setState(CheckboxView.UNCHECKED);

            if (item.assigneeInitial != null) {
                AvatarView avatar = row.findViewById(R.id.checklistItemAvatar);
                avatar.setVisibility(View.VISIBLE);
                avatar.setInitial(item.assigneeInitial);
            }
            if (item.restrictionType >= 0) {
                RestrictionTagView tag = row.findViewById(R.id.checklistItemRestrictionTag);
                tag.setVisibility(View.VISIBLE);
                tag.setType(item.restrictionType);
            }
            sections.addView(row);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
