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
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.atoms.PriorityDotView;
import com.example.mybaghackathon.ui.atoms.RestrictionTagView;
import com.example.mybaghackathon.ui.atoms.WeatherIconView;
import com.example.mybaghackathon.ui.roomdetail.RoomDetailActivity;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

/**
 * S08 · 일정 검토 — STEP 1(일정 확인) · STEP 2(준비물 체크) 두 단계로 나뉜
 * 검토 화면. "방 생성 완료"를 누르면 이 시점에 방이 실제로 생성되고
 * RoomDetailActivity(S09)로 이동함.
 */
public class ScheduleReviewActivity extends AppCompatActivity {

    private ActivityScheduleReviewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        binding.reviewTopAppBar.topAppBarTitle.setText(R.string.review_title);

        bindReviewField(binding.reviewDestinationCard, R.string.review_destination_label, "도쿄, 일본");
        bindReviewField(binding.reviewDatesCard, R.string.review_dates_label, "3.14 — 3.18 · 4박5일");
        // 두 카드 높이를 맞추기 위해 둘 다 같은 크기로, 한 줄에 들어가도록 통일
        for (View card : new View[]{binding.reviewDestinationCard, binding.reviewDatesCard}) {
            TextView value = card.findViewById(R.id.reviewFieldValue);
            value.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14);
            value.setMaxLines(1);
        }

        LinearLayout weatherList = binding.reviewWeatherList;
        addWeatherRow(weatherList, "3.14(토)", 0, "맑음 12°");
        addWeatherRow(weatherList, "3.15(일)", 0, "맑음 14°");
        addWeatherRow(weatherList, "3.16(월)", 2, "흐림 11°");

        View warningBanner = binding.reviewRestrictionWarning;
        ((TextView) warningBanner.findViewById(R.id.warningBannerText)).setText(R.string.review_restriction_warning);

        final LinearLayout sections = binding.reviewItemSections;
        addPrioritySection(sections, 0, getString(R.string.review_priority_high), Arrays.asList(
                new ChecklistItem("여권", 0), new ChecklistItem("항공권", 0)));
        addPrioritySection(sections, 1, getString(R.string.review_priority_mid), Arrays.asList(
                new ChecklistItem("보조배터리", 1), new ChecklistItem("우산", 1)));
        addPrioritySection(sections, 2, getString(R.string.review_priority_low), Arrays.asList(
                new ChecklistItem("상비약", 2)));

        MaterialButton generate = binding.reviewBottomCta.bottomCtaPrimary;
        generate.setText(R.string.review_generate);
        generate.setOnClickListener(v -> {
            startActivity(new Intent(this, RoomDetailActivity.class));
            finish();
        });
    }

    private void bindReviewField(View card, int labelRes, String value) {
        ((TextView) card.findViewById(R.id.reviewFieldLabel)).setText(labelRes);
        ((TextView) card.findViewById(R.id.reviewFieldValue)).setText(value);
        card.findViewById(R.id.reviewFieldEdit).setVisibility(View.GONE);
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
        ((TextView) header.findViewById(R.id.sectionHeaderLabel)).setText(label);
        LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (sections.getChildCount() > 0) headerLp.topMargin = dp(16);
        sections.addView(header, headerLp);

        for (ChecklistItem item : items) {
            View row = LayoutInflater.from(this).inflate(R.layout.molecule_checklist_item_row, sections, false);
            ((TextView) row.findViewById(R.id.checklistItemLabel)).setText(item.label);
            if (item.label.equals("보조배터리")) {
                RestrictionTagView tag = row.findViewById(R.id.checklistItemRestrictionTag);
                tag.setType(RestrictionTagView.CABIN_ONLY);
                tag.setVisibility(View.VISIBLE);
            }
            sections.addView(row);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
