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
import com.example.mybaghackathon.ui.atoms.CheckboxView;
import com.example.mybaghackathon.ui.atoms.PriorityDotView;
import com.example.mybaghackathon.ui.atoms.WeatherIconView;
import com.example.mybaghackathon.ui.overlay.AddItemSheet;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** S08 · Schedule Review — confirm AI-read destination/dates/weather, edit the item list. */
public class ScheduleReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_review);

        TextView title = findViewById(R.id.topAppBarCompactTitle);
        title.setText(R.string.review_title);
        findViewById(R.id.topAppBarBack).setOnClickListener(v -> finish());

        bindReviewField(R.id.reviewDestinationCard, R.string.review_destination_label, "도쿄, 일본");
        bindReviewField(R.id.reviewDatesCard, R.string.review_dates_label, "3.15(일) — 3.19(목)");

        LinearLayout weatherList = findViewById(R.id.reviewWeatherList);
        addWeatherRow(weatherList, "3.15(일)", 0, "맑음 14°");
        addWeatherRow(weatherList, "3.16(월)", 1, "비 11°");
        addWeatherRow(weatherList, "3.17(화)", 2, "흐림 12°");

        final LinearLayout sections = findViewById(R.id.reviewItemSections);
        addPrioritySection(sections, 0, getString(R.string.review_priority_high), Arrays.asList(
                new ChecklistItem("여권", 0), new ChecklistItem("항공권", 0), new ChecklistItem("충전기", 0)));
        addPrioritySection(sections, 1, getString(R.string.review_priority_mid), Arrays.asList(
                new ChecklistItem("우산", 1), new ChecklistItem("보조배터리", 1)));
        addPrioritySection(sections, 2, getString(R.string.review_priority_low), Arrays.asList(
                new ChecklistItem("선글라스", 2)));

        findViewById(R.id.reviewAddItemLink).setOnClickListener(v -> {
            AddItemSheet sheet = new AddItemSheet();
            sheet.setOnItemAddedListener((label, priority) -> {
                String[] labels = {getString(R.string.review_priority_high),
                        getString(R.string.review_priority_mid), getString(R.string.review_priority_low)};
                addPrioritySection(sections, priority, labels[priority],
                        Collections.singletonList(new ChecklistItem(label, priority)));
            });
            sheet.show(getSupportFragmentManager(), "add_item");
        });

        LinearLayout excludedList = findViewById(R.id.reviewExcludedList);
        addExcludedRow(excludedList, "두꺼운 패딩");

        MaterialButton generate = findViewById(R.id.bottomCtaPrimary);
        generate.setText(R.string.review_generate);
        generate.setOnClickListener(v -> {
            startActivity(new Intent(this,
                    com.example.mybaghackathon.ui.feedback.WeatherFeedbackActivity.class));
            finish();
        });
    }

    private void bindReviewField(int cardContainerId, int labelRes, String value) {
        View card = findViewById(cardContainerId);
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
