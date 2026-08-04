package com.example.mybaghackathon.ui.review;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.data.ChecklistItem;
import com.example.mybaghackathon.databinding.ActivityScheduleReviewBinding;
import com.example.mybaghackathon.ui.atoms.CheckboxView;
import com.example.mybaghackathon.ui.atoms.PriorityDotView;
import com.example.mybaghackathon.ui.atoms.WeatherIconView;
import com.example.mybaghackathon.ui.overlay.AddItemSheet;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * S08 · 일정 확인 — AI가 읽어낸 목적지/날짜/날씨를 확인하고, 항목 목록을 수정함.
 *
 * 기능: 목적지/날짜 카드, 날씨 목록, 우선순위별 체크리스트 섹션, 제외된
 * 항목 목록을 전부 동적으로 채워주고, "생성" 버튼으로 WeatherFeedbackActivity로
 * 이동하는 화면.
 */
public class ScheduleReviewActivity extends AppCompatActivity {

    private ActivityScheduleReviewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView title = binding.reviewTopBar.topAppBarCompactTitle;
        title.setText(R.string.review_title);
        binding.reviewTopBar.topAppBarBack.setOnClickListener(v -> finish());

        bindReviewField(binding.reviewDestinationCard, R.string.review_destination_label, "도쿄, 일본");
        bindReviewField(binding.reviewDatesCard, R.string.review_dates_label, "3.15(일) — 3.19(목)");

        LinearLayout weatherList = binding.reviewWeatherList;
        addWeatherRow(weatherList, "3.15(일)", 0, "맑음 14°");
        addWeatherRow(weatherList, "3.16(월)", 1, "비 11°");
        addWeatherRow(weatherList, "3.17(화)", 2, "흐림 12°");

        final LinearLayout sections = binding.reviewItemSections;
        addPrioritySection(sections, 0, getString(R.string.review_priority_high), Arrays.asList(
                new ChecklistItem("여권", 0), new ChecklistItem("항공권", 0), new ChecklistItem("충전기", 0)));
        addPrioritySection(sections, 1, getString(R.string.review_priority_mid), Arrays.asList(
                new ChecklistItem("우산", 1), new ChecklistItem("보조배터리", 1)));
        addPrioritySection(sections, 2, getString(R.string.review_priority_low), Arrays.asList(
                new ChecklistItem("선글라스", 2)));

        binding.reviewAddItemLink.setOnClickListener(v -> {
            AddItemSheet sheet = new AddItemSheet();
            sheet.setOnItemAddedListener((label, priority) -> {
                String[] labels = {getString(R.string.review_priority_high),
                        getString(R.string.review_priority_mid), getString(R.string.review_priority_low)};
                addPrioritySection(sections, priority, labels[priority],
                        Collections.singletonList(new ChecklistItem(label, priority)));
            });
            sheet.show(getSupportFragmentManager(), "add_item");
        });

        LinearLayout excludedList = binding.reviewExcludedList;
        addExcludedRow(excludedList, "두꺼운 패딩");

        MaterialButton generate = binding.reviewBottomCta.bottomCtaPrimary;
        generate.setText(R.string.review_generate);
        generate.setOnClickListener(v -> {
            startActivity(new Intent(this,
                    com.example.mybaghackathon.ui.feedback.WeatherFeedbackActivity.class));
            finish();
        });
    }

    private void bindReviewField(View card, int labelRes, String value) {
        ((TextView) card.findViewById(R.id.reviewFieldLabel)).setText(labelRes);
        ((TextView) card.findViewById(R.id.reviewFieldValue)).setText(value);
    }

    private void addWeatherRow(LinearLayout list, String date, int weatherType, String status) {
        View row = LayoutInflater.from(this).inflate(R.layout.molecule_weather_day_row, list, false);
        ((TextView) row.findViewById(R.id.weatherRowDate)).setText(date);
        ((WeatherIconView) row.findViewById(R.id.weatherRowIcon)).setType(weatherType);
        ((TextView) row.findViewById(R.id.weatherRowStatus)).setText(status);
        if (list.getChildCount() > 0) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) row.getLayoutParams();
            lp.topMargin = dp(8);
        }
        list.addView(row);
    }

    private void addPrioritySection(LinearLayout sections, int level, String label, List<ChecklistItem> items) {
        View header = LayoutInflater.from(this).inflate(R.layout.molecule_section_header, sections, false);
        ((PriorityDotView) header.findViewById(R.id.sectionHeaderDot)).setLevel(level);
        ((TextView) header.findViewById(R.id.sectionHeaderLabel))
                .setText(label + " · " + items.size());
        LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (sections.getChildCount() > 0) headerLp.topMargin = dp(16);
        sections.addView(header, headerLp);

        for (ChecklistItem item : items) {
            View row = LayoutInflater.from(this).inflate(R.layout.molecule_checklist_item_row, sections, false);
            ((TextView) row.findViewById(R.id.checklistItemLabel)).setText(item.label);
            ((CheckboxView) row.findViewById(R.id.checklistItemCheckbox)).setState(CheckboxView.CHECKED);
            sections.addView(row);
        }
    }

    private void addExcludedRow(LinearLayout list, String label) {
        View row = LayoutInflater.from(this).inflate(R.layout.molecule_checklist_item_row, list, false);
        ((TextView) row.findViewById(R.id.checklistItemLabel)).setText(label);
        CheckboxView checkbox = row.findViewById(R.id.checklistItemCheckbox);
        checkbox.setState(CheckboxView.EXCLUDED);
        row.setAlpha(0.6f);
        list.addView(row);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
