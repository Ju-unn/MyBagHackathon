package com.example.mybaghackathon.ui.review;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.mapper.WeatherMapper;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.data.repository.WeatherRepository;
import com.example.mybaghackathon.databinding.ActivityScheduleReviewBinding;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.RestrictedItem;
import com.example.mybaghackathon.model.TripInvite;
import com.example.mybaghackathon.model.Weather;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.analyzing.AnalyzingActivity;
import com.example.mybaghackathon.ui.atoms.CheckboxView;
import com.example.mybaghackathon.ui.atoms.PriorityDotView;
import com.example.mybaghackathon.ui.atoms.RestrictionTagView;
import com.example.mybaghackathon.ui.atoms.WeatherIconView;
import com.example.mybaghackathon.ui.roomdetail.RoomDetailActivity;
import com.example.mybaghackathon.ui.upload.ScheduleUploadActivity;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * S08 · 일정 검토 — STEP 1(일정 확인) · STEP 2(준비물 체크) 두 단계로 나뉜
 * 검토 화면. "방 생성 완료"를 누르면 이 시점에 방이 실제로 생성되고
 * RoomDetailActivity(S09)로 이동함.
 */
public class ScheduleReviewActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "trip_id";

    private static final String SCOPE_COMMON = "COMMON";
    private static final String SCOPE_PERSONAL = "PERSONAL";

    private ActivityScheduleReviewBinding binding;
    private String roomName;
    private int memberCount;
    private long analysisId;
    private ArrayList<RestrictedItem> restrictedItems;
    private ArrayList<PackingItem> recommendedItems;
    private String destinationCountry;
    private String destinationCity;
    private String startDate;
    private String endDate;
    private WeatherRepository weatherRepository;
    private TripRepository tripRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<String, String> itemScopeByName = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        Intent intent = getIntent();
        roomName = intent.getStringExtra(ScheduleUploadActivity.EXTRA_ROOM_NAME);
        memberCount = intent.getIntExtra(ScheduleUploadActivity.EXTRA_MEMBER_COUNT, 0);
        analysisId = intent.getLongExtra(AnalyzingActivity.EXTRA_ANALYSIS_ID, -1);
        restrictedItems = (ArrayList<RestrictedItem>) intent.getSerializableExtra(AnalyzingActivity.EXTRA_RESTRICTED_ITEMS);
        recommendedItems = (ArrayList<PackingItem>) intent.getSerializableExtra(AnalyzingActivity.EXTRA_RECOMMENDED_ITEMS);
        destinationCountry = intent.getStringExtra(AnalyzingActivity.EXTRA_DESTINATION_COUNTRY);
        destinationCity = intent.getStringExtra(AnalyzingActivity.EXTRA_DESTINATION_CITY);
        startDate = intent.getStringExtra(AnalyzingActivity.EXTRA_START_DATE);
        endDate = intent.getStringExtra(AnalyzingActivity.EXTRA_END_DATE);

        weatherRepository = ((MyBagApplication) getApplication()).getAppContainer().weatherRepository;
        tripRepository = ((MyBagApplication) getApplication()).getAppContainer().tripRepository;

        binding.reviewTopAppBar.topAppBarTitle.setText(R.string.review_title);

        bindReviewField(binding.reviewDestinationCard, R.string.review_destination_label,
                formatDestination(destinationCity, destinationCountry));
        bindReviewField(binding.reviewDatesCard, R.string.review_dates_label,
                formatDateRange(startDate, endDate));
        // 두 카드 높이를 맞추기 위해 둘 다 같은 크기로, 한 줄에 들어가도록 통일
        for (View card : new View[]{binding.reviewDestinationCard, binding.reviewDatesCard}) {
            TextView value = card.findViewById(R.id.reviewFieldValue);
            value.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14);
            value.setMaxLines(1);
        }

        LinearLayout weatherList = binding.reviewWeatherList;
        loadWeather(weatherList, destinationCity, startDate, endDate);

        setupRestrictionWarning();

        setupRecommendedItems();

        MaterialButton generate = binding.reviewBottomCta.bottomCtaPrimary;
        generate.setText(R.string.review_generate);
        generate.setOnClickListener(v -> onGenerateClicked(generate));
    }

    private void onGenerateClicked(MaterialButton button) {
        button.setEnabled(false);
        executor.execute(() -> {
            AppResult<TripInvite> result = tripRepository.createTrip(roomName, memberCount, analysisId, itemScopeByName);
            runOnUiThread(() -> {
                if (isFinishing()) return;
                if (result.isSuccess()) {
                    Intent roomDetailIntent = new Intent(this, RoomDetailActivity.class);
                    roomDetailIntent.putExtra(EXTRA_TRIP_ID, result.getData().getTripId());
                    startActivity(roomDetailIntent);
                    finish();
                } else {
                    button.setEnabled(true);
                    Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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

    private void setupRecommendedItems() {
        LinearLayout sections = binding.reviewItemSections;
        if (recommendedItems == null) return;

        List<PackingItem> required = new ArrayList<>();
        List<PackingItem> recommended = new ArrayList<>();
        List<PackingItem> optional = new ArrayList<>();
        for (PackingItem item : recommendedItems) {
            String priority = item.getPriority();
            if ("REQUIRED".equals(priority)) required.add(item);
            else if ("OPTIONAL".equals(priority)) optional.add(item);
            else recommended.add(item); // RECOMMENDED 또는 알 수 없는 값은 중간 취급
        }

        addPrioritySection(sections, 0, getString(R.string.review_priority_high), required);
        addPrioritySection(sections, 1, getString(R.string.review_priority_mid), recommended);
        addPrioritySection(sections, 2, getString(R.string.review_priority_low), optional);
    }

    private void addPrioritySection(LinearLayout sections, int level, String label, List<PackingItem> items) {
        if (items.isEmpty()) return; // 그 등급에 항목이 없으면 섹션 자체를 안 만듦

        View header = LayoutInflater.from(this).inflate(R.layout.molecule_section_header, sections, false);
        ((PriorityDotView) header.findViewById(R.id.sectionHeaderDot)).setLevel(level);
        ((TextView) header.findViewById(R.id.sectionHeaderLabel)).setText(label);
        LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (sections.getChildCount() > 0) headerLp.topMargin = dp(16);
        sections.addView(header, headerLp);

        for (PackingItem item : items) {
            View row = LayoutInflater.from(this).inflate(R.layout.molecule_checklist_item_row, sections, false);
            ((TextView) row.findViewById(R.id.checklistItemLabel)).setText(item.getItemName());

            RestrictedItem restriction = findRestriction(item.getItemName());
            if (restriction != null) {
                Integer tagType = restrictionTagType(restriction.getRestrictionType());
                if (tagType != null) {
                    RestrictionTagView tag = row.findViewById(R.id.checklistItemRestrictionTag);
                    tag.setType(tagType);
                    tag.setVisibility(View.VISIBLE);
                }
            }

            itemScopeByName.put(item.getItemName(), SCOPE_PERSONAL);
            CheckboxView checkbox = row.findViewById(R.id.checklistItemCheckbox);
            checkbox.setState(CheckboxView.UNCHECKED);
            checkbox.setOnCheckChangeListener(newState ->
                    itemScopeByName.put(item.getItemName(),
                            newState == CheckboxView.CHECKED ? SCOPE_COMMON : SCOPE_PERSONAL));

            sections.addView(row);
        }
    }

    private RestrictedItem findRestriction(String itemName) {
        if (restrictedItems == null) return null;
        for (RestrictedItem restriction : restrictedItems) {
            if (restriction.getItemName().equals(itemName)) return restriction;
        }
        return null;
    }

    // RestrictionTagView는 3종류만 표현 가능 — LIMITED/CAUTION은 태그 없이 넘어감
    private Integer restrictionTagType(String type) {
        if ("PROHIBITED".equals(type)) return RestrictionTagView.PROHIBITED;
        if ("CARRY_ON_ONLY".equals(type)) return RestrictionTagView.CABIN_ONLY;
        if ("CHECKED_ONLY".equals(type)) return RestrictionTagView.CHECKED_ONLY;
        return null;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String formatDestination(String city, String country) {
        if (city == null || city.isEmpty()) return country == null ? getString(R.string.value_unknown) : country;
        if (country == null || country.isEmpty()) return city;
        return city + ", " + country;
    }

    // "yyyy-MM-dd" 두 개를 "M.d — M.d · N박M일" 형식으로 바꾼다. 파싱 실패 시 원본 그대로 보여줌
    private String formatDateRange(String startDate, String endDate) {
        if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            return getString(R.string.value_unknown);
        }
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            iso.setLenient(false);
            Date start = iso.parse(startDate);
            Date end = iso.parse(endDate);
            SimpleDateFormat display = new SimpleDateFormat("M.d", Locale.KOREA);
            long nights = TimeUnit.MILLISECONDS.toDays(end.getTime() - start.getTime());
            return display.format(start) + " — " + display.format(end) + " · " + nights + "박" + (nights + 1) + "일";
        } catch (ParseException e) {
            return startDate + " — " + endDate;
        }
    }

    private void loadWeather(LinearLayout weatherList, String city, String startDate, String endDate) {
        if (city == null || city.isEmpty() || startDate == null || endDate == null) return;

        executor.execute(() -> {
            AppResult<List<Weather>> result = weatherRepository.getForecast(city, startDate, endDate);
            runOnUiThread(() -> {
                if (isFinishing() || !result.isSuccess()) return; // 실패해도 날씨 섹션만 비워둠, 나머지 화면은 그대로
                for (Weather day : result.getData()) {
                    addWeatherRow(weatherList, formatWeatherDate(day.getDate()),
                            WeatherMapper.toIconType(day.getCondition()),
                            formatWeatherStatus(day.getCondition(), day.getTempMax()));
                }
            });
        });
    }

    private String formatWeatherDate(String isoDate) {
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            iso.setLenient(false);
            Date date = iso.parse(isoDate);
            return new SimpleDateFormat("M.d(E)", Locale.KOREA).format(date);
        } catch (ParseException e) {
            return isoDate;
        }
    }

    private String formatWeatherStatus(String condition, double tempMax) {
        String label;
        if ("rain".equals(condition)) label = getString(R.string.weather_condition_rain);
        else if ("cloud".equals(condition)) label = getString(R.string.weather_condition_cloud);
        else if ("snow".equals(condition)) label = getString(R.string.weather_condition_snow);
        else label = getString(R.string.weather_condition_sun);
        return label + " " + Math.round(tempMax) + "°";
    }

    private void setupRestrictionWarning() {
        View warningBanner = binding.reviewRestrictionWarning;
        if (restrictedItems == null || restrictedItems.isEmpty()) {
            warningBanner.setVisibility(View.GONE);
            return;
        }

        String firstItemName = restrictedItems.get(0).getItemName();
        String bannerText = restrictedItems.size() == 1
                ? getString(R.string.review_restriction_warning_single, firstItemName)
                : getString(R.string.review_restriction_warning_multiple, firstItemName, restrictedItems.size());
        ((TextView) warningBanner.findViewById(R.id.warningBannerText)).setText(bannerText);

        warningBanner.setOnClickListener(v -> showRestrictionDialog());
    }

    private void showRestrictionDialog() {
        StringBuilder message = new StringBuilder();
        for (RestrictedItem item : restrictedItems) {
            if (message.length() > 0) message.append("\n\n");
            message.append(item.getItemName()).append(" — ").append(restrictionTypeLabel(item.getRestrictionType()));
            if (item.getReason() != null && !item.getReason().isEmpty()) {
                message.append("\n").append(item.getReason());
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.review_restriction_dialog_title)
                .setMessage(message.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private String restrictionTypeLabel(String type) {
        if (type == null) return "";
        switch (type) {
            case "PROHIBITED": return getString(R.string.restriction_prohibited);
            case "CARRY_ON_ONLY": return getString(R.string.restriction_cabin_only);
            case "CHECKED_ONLY": return getString(R.string.restriction_checked_only);
            case "LIMITED": return getString(R.string.restriction_limited);
            case "CAUTION": return getString(R.string.restriction_caution);
            default: return type;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
