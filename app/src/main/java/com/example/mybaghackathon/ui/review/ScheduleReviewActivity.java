package com.example.mybaghackathon.ui.review;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.databinding.ActivityScheduleReviewBinding;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.RestrictedItem;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.analyzing.AnalyzingActivity;
import com.example.mybaghackathon.ui.atoms.CheckboxView;
import com.example.mybaghackathon.ui.atoms.PriorityDotView;
import com.example.mybaghackathon.ui.atoms.RestrictionTagView;
import com.example.mybaghackathon.ui.atoms.WeatherIconView;
import com.example.mybaghackathon.ui.overlay.RestrictionInfoSheet;
import com.example.mybaghackathon.ui.roomdetail.RoomDetailActivity;
import com.example.mybaghackathon.ui.upload.ScheduleUploadActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * S08 · 일정 검토 — 화면 표시(View)만 담당. 날씨/방생성 API 호출, 준비물 그룹핑,
 * 반입규정 매칭 같은 로직은 ReviewPresenter가 처리함(MVP). "방 생성 완료"를 누르면
 * 이 시점에 방이 실제로 생성되고 RoomDetailActivity(S09)로 이동함.
 */
public class ScheduleReviewActivity extends AppCompatActivity implements ReviewContract.View {

    public static final String EXTRA_TRIP_ID = "trip_id";

    private ActivityScheduleReviewBinding binding;
    private ReviewContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        presenter = new ReviewPresenter(this, getApplicationContext(),
                ((MyBagApplication) getApplication()).getAppContainer().weatherRepository,
                ((MyBagApplication) getApplication()).getAppContainer().tripRepository);

        binding.reviewTopAppBar.topAppBarTitle.setText(R.string.review_title);

        setLabel(binding.reviewDestinationCard, R.string.review_destination_label);
        setLabel(binding.reviewDatesCard, R.string.review_dates_label);
        // 두 카드 높이를 맞추기 위해 둘 다 한 줄에 들어가도록 통일. 여행 기간이 길어지면
        // "N박M일" 자릿수가 늘어나 14sp로는 안 들어갈 수 있어 10~14sp 범위에서 자동으로
        // 줄어들게 하고(오토사이즈), 그래도 못 들어가는 극단적인 경우엔 말줄임표로 처리.
        for (View card : new View[]{binding.reviewDestinationCard, binding.reviewDatesCard}) {
            TextView value = card.findViewById(R.id.reviewFieldValue);
            value.setMaxLines(1);
            value.setEllipsize(TextUtils.TruncateAt.END);
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    value, 10, 14, 1, TypedValue.COMPLEX_UNIT_SP);
        }

        binding.reviewRestrictionWarning.setOnClickListener(v ->
                RestrictionInfoSheet.newInstance(new ArrayList<>(presenter.getRestrictedItems()))
                        .show(getSupportFragmentManager(), "restriction_info"));

        MaterialButton generate = binding.reviewBottomCta.bottomCtaPrimary;
        generate.setText(R.string.review_generate);
        generate.setOnClickListener(v -> presenter.onGenerateClicked());

        Intent intent = getIntent();
        String roomName = intent.getStringExtra(ScheduleUploadActivity.EXTRA_ROOM_NAME);
        int memberCount = intent.getIntExtra(ScheduleUploadActivity.EXTRA_MEMBER_COUNT, 0);
        long analysisId = intent.getLongExtra(AnalyzingActivity.EXTRA_ANALYSIS_ID, -1);
        @SuppressWarnings("unchecked")
        ArrayList<RestrictedItem> restrictedItems =
                (ArrayList<RestrictedItem>) intent.getSerializableExtra(AnalyzingActivity.EXTRA_RESTRICTED_ITEMS);
        @SuppressWarnings("unchecked")
        ArrayList<PackingItem> recommendedItems =
                (ArrayList<PackingItem>) intent.getSerializableExtra(AnalyzingActivity.EXTRA_RECOMMENDED_ITEMS);
        String destinationCountry = intent.getStringExtra(AnalyzingActivity.EXTRA_DESTINATION_COUNTRY);
        String destinationCity = intent.getStringExtra(AnalyzingActivity.EXTRA_DESTINATION_CITY);
        String startDate = intent.getStringExtra(AnalyzingActivity.EXTRA_START_DATE);
        String endDate = intent.getStringExtra(AnalyzingActivity.EXTRA_END_DATE);

        presenter.init(roomName, memberCount, analysisId, restrictedItems, recommendedItems,
                destinationCountry, destinationCity, startDate, endDate);
    }

    private void setLabel(View card, int labelRes) {
        ((TextView) card.findViewById(R.id.reviewFieldLabel)).setText(labelRes);
        card.findViewById(R.id.reviewFieldEdit).setVisibility(View.GONE);
    }

    // ===== ReviewContract.View =====

    @Override
    public void showDestination(String value) {
        ((TextView) binding.reviewDestinationCard.findViewById(R.id.reviewFieldValue)).setText(value);
    }

    @Override
    public void showDateRange(String value) {
        ((TextView) binding.reviewDatesCard.findViewById(R.id.reviewFieldValue)).setText(value);
    }

    @Override
    public void showRestrictionWarning(String text) {
        View warningBanner = binding.reviewRestrictionWarning;
        warningBanner.setVisibility(View.VISIBLE);
        binding.reviewRestrictionDisclaimer.setVisibility(View.VISIBLE);
        ((TextView) warningBanner.findViewById(R.id.warningBannerText)).setText(text);
    }

    @Override
    public void hideRestrictionWarning() {
        binding.reviewRestrictionWarning.setVisibility(View.GONE);
        binding.reviewRestrictionDisclaimer.setVisibility(View.GONE);
    }

    @Override
    public void addWeatherRow(String date, int weatherType, String status) {
        LinearLayout list = binding.reviewWeatherList;
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

    @Override
    public void showWeatherLimitNotice() {
        binding.reviewWeatherLimitNotice.setVisibility(View.VISIBLE);
    }

    @Override
    public void showRequiredItems(List<ReviewContract.ItemView> items) {
        addPrioritySection(0, getString(R.string.review_priority_high), items);
    }

    @Override
    public void showRecommendedItems(List<ReviewContract.ItemView> items) {
        addPrioritySection(1, getString(R.string.review_priority_mid), items);
    }

    @Override
    public void showOptionalItems(List<ReviewContract.ItemView> items) {
        addPrioritySection(2, getString(R.string.review_priority_low), items);
    }

    private void addPrioritySection(int level, String label, List<ReviewContract.ItemView> items) {
        if (items.isEmpty()) return; // 그 등급에 항목이 없으면 섹션 자체를 안 만듦

        LinearLayout sections = binding.reviewItemSections;

        View header = LayoutInflater.from(this).inflate(R.layout.molecule_section_header, sections, false);
        ((PriorityDotView) header.findViewById(R.id.sectionHeaderDot)).setLevel(level);
        TextView headerLabel = header.findViewById(R.id.sectionHeaderLabel);
        int[] selectedCount = {0};
        updateSelectionCount(headerLabel, label, selectedCount[0], items.size());
        LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (sections.getChildCount() > 0) headerLp.topMargin = dp(16);
        sections.addView(header, headerLp);

        // 항목들을 카드 하나로 묶어서 다른 화면의 카드들과 톤을 맞춤
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_lg_border);
        card.setPadding(dp(12), dp(4), dp(12), dp(4));

        for (int i = 0; i < items.size(); i++) {
            ReviewContract.ItemView item = items.get(i);
            if (i > 0) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));
                divider.setBackgroundColor(getColor(R.color.bag_border_divider));
                card.addView(divider);
            }

            View row = LayoutInflater.from(this).inflate(R.layout.molecule_checklist_item_row, card, false);
            ((TextView) row.findViewById(R.id.checklistItemLabel)).setText(item.itemName);

            if (item.restrictionTagType != null) {
                RestrictionTagView tag = row.findViewById(R.id.checklistItemRestrictionTag);
                tag.setType(item.restrictionTagType);
                tag.setVisibility(View.VISIBLE);
            }

            CheckboxView checkbox = row.findViewById(R.id.checklistItemCheckbox);
            checkbox.setState(CheckboxView.UNCHECKED);
            boolean[] selected = {false};
            checkbox.setOnCheckChangeListener(newState -> {
                boolean nextSelected = newState == CheckboxView.CHECKED;
                if (selected[0] != nextSelected) {
                    selected[0] = nextSelected;
                    selectedCount[0] += nextSelected ? 1 : -1;
                    updateSelectionCount(
                            headerLabel, label, selectedCount[0], items.size());
                }
                presenter.onItemScopeToggled(item.itemName, nextSelected);
            });

            card.addView(row);
        }

        sections.addView(card);
        bindSectionToggle(header, card, label);
    }

    private void updateSelectionCount(
            TextView headerLabel,
            String label,
            int selected,
            int total
    ) {
        headerLabel.setText(getString(
                R.string.review_priority_selected_count_format,
                label,
                selected,
                total));
    }

    private void bindSectionToggle(View header, View content, String label) {
        ImageView toggle = header.findViewById(R.id.sectionHeaderToggle);
        boolean[] expanded = {true};
        header.setOnClickListener(v -> {
            expanded[0] = !expanded[0];
            content.setVisibility(expanded[0] ? View.VISIBLE : View.GONE);
            toggle.setRotation(expanded[0] ? 90f : 0f);
            header.setContentDescription(getString(expanded[0]
                    ? R.string.checklist_section_collapse
                    : R.string.checklist_section_expand, label));
        });
        header.setContentDescription(getString(R.string.checklist_section_collapse, label));
    }

    @Override
    public void setGenerating(boolean generating) {
        binding.reviewBottomCta.bottomCtaPrimary.setEnabled(!generating);
        binding.reviewLoadingOverlay.setVisibility(generating ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showGenerateError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToRoomDetail(long tripId, String roomName, String inviteCode) {
        Intent roomDetailIntent = new Intent(this, RoomDetailActivity.class);
        roomDetailIntent.putExtra(EXTRA_TRIP_ID, tripId);
        roomDetailIntent.putExtra(RoomDetailActivity.EXTRA_ROOM_NAME, roomName);
        roomDetailIntent.putExtra(RoomDetailActivity.EXTRA_INVITE_CODE, inviteCode);
        // 방 만들기를 공용여행 탭에서 시작했을 수도 있으니, 방 상세에서 뒤로 나갈 때
        // 그 탭이 아니라 홈 탭으로 보이게 하라는 표시를 남겨 전달한다.
        roomDetailIntent.putExtra(RoomDetailActivity.EXTRA_FROM_CREATION, true);
        startActivity(roomDetailIntent);
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
