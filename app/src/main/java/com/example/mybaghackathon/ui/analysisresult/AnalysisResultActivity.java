package com.example.mybaghackathon.ui.analysisresult;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityAnalysisResultBinding;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.RestrictedItem;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.analyzing.AnalyzingActivity;
import com.example.mybaghackathon.ui.overlay.EditFieldSheet;
import com.example.mybaghackathon.ui.review.ScheduleReviewActivity;
import com.example.mybaghackathon.ui.upload.ScheduleUploadActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * S07 · 1차 결과 확인 — AI 분석 직후 여행지·일정·숙소·이동수단만 빠르게
 * 확인하는 화면. "다시 분석하기"로 재분석, "다음"으로 상세 검토(S08)로 진행.
 */
public class AnalysisResultActivity extends AppCompatActivity {

    private ActivityAnalysisResultBinding binding;
    private long analysisId;
    private long[] uploadIds;
    private String roomName;
    private int memberCount;
    private ArrayList<RestrictedItem> restrictedItems;
    private ArrayList<PackingItem> recommendedItems;
    private String destinationCountry;
    private String destinationCity;
    private String startDate;
    private String endDate;
    private String accommodationName;
    private String transportMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalysisResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        binding.confirmTopAppBar.topAppBarCompactTitle.setText(R.string.schedule_confirm_title);
        binding.confirmTopAppBar.topAppBarBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        analysisId = intent.getLongExtra(AnalyzingActivity.EXTRA_ANALYSIS_ID, -1);
        uploadIds = intent.getLongArrayExtra(ScheduleUploadActivity.EXTRA_UPLOAD_IDS);
        destinationCountry = intent.getStringExtra(AnalyzingActivity.EXTRA_DESTINATION_COUNTRY);
        destinationCity = intent.getStringExtra(AnalyzingActivity.EXTRA_DESTINATION_CITY);
        startDate = intent.getStringExtra(AnalyzingActivity.EXTRA_START_DATE);
        endDate = intent.getStringExtra(AnalyzingActivity.EXTRA_END_DATE);
        transportMode = intent.getStringExtra(AnalyzingActivity.EXTRA_TRANSPORT_MODE);
        accommodationName = intent.getStringExtra(AnalyzingActivity.EXTRA_ACCOMMODATION_NAME);
        roomName = intent.getStringExtra(ScheduleUploadActivity.EXTRA_ROOM_NAME);
        memberCount = intent.getIntExtra(ScheduleUploadActivity.EXTRA_MEMBER_COUNT, 0);
        restrictedItems = (ArrayList<RestrictedItem>) intent.getSerializableExtra(AnalyzingActivity.EXTRA_RESTRICTED_ITEMS);
        recommendedItems = (ArrayList<PackingItem>) intent.getSerializableExtra(AnalyzingActivity.EXTRA_RECOMMENDED_ITEMS);

        bindField(binding.confirmDestinationCard, R.string.schedule_confirm_destination_label,
                formatDestination(destinationCity, destinationCountry));
        binding.confirmDestinationCard.findViewById(R.id.reviewFieldEdit)
                .setOnClickListener(v -> showDestinationEditDialog());

        bindField(binding.confirmScheduleCard, R.string.schedule_confirm_schedule_label,
                formatDateRange(startDate, endDate));
        binding.confirmScheduleCard.findViewById(R.id.reviewFieldEdit)
                .setOnClickListener(v -> showScheduleEditDialog());

        bindField(binding.confirmLodgingCard, R.string.schedule_confirm_lodging_label,
                TextUtils.isEmpty(accommodationName) ? getString(R.string.accommodation_none) : accommodationName);
        binding.confirmLodgingCard.findViewById(R.id.reviewFieldEdit)
                .setOnClickListener(v -> showLodgingEditDialog());

        bindField(binding.confirmTransportCard, R.string.schedule_confirm_transport_label,
                formatTransportMode(transportMode));
        binding.confirmTransportCard.findViewById(R.id.reviewFieldEdit)
                .setOnClickListener(v -> showTransportEditDialog());

        binding.confirmRetryButton.setOnClickListener(v -> {
            Intent retry = new Intent(this, AnalyzingActivity.class);
            retry.putExtra(ScheduleUploadActivity.EXTRA_UPLOAD_IDS, uploadIds);
            retry.putExtra(ScheduleUploadActivity.EXTRA_ROOM_NAME, roomName);
            retry.putExtra(ScheduleUploadActivity.EXTRA_MEMBER_COUNT, memberCount);
            startActivity(retry);
            finish();
        });
        binding.confirmNextButton.setOnClickListener(v -> {
            Intent next = new Intent(this, ScheduleReviewActivity.class);
            next.putExtra(AnalyzingActivity.EXTRA_ANALYSIS_ID, analysisId);
            next.putExtra(ScheduleUploadActivity.EXTRA_ROOM_NAME, roomName);
            next.putExtra(ScheduleUploadActivity.EXTRA_MEMBER_COUNT, memberCount);
            next.putExtra(AnalyzingActivity.EXTRA_RESTRICTED_ITEMS, restrictedItems);
            next.putExtra(AnalyzingActivity.EXTRA_RECOMMENDED_ITEMS, recommendedItems);
            next.putExtra(AnalyzingActivity.EXTRA_DESTINATION_COUNTRY, destinationCountry);
            next.putExtra(AnalyzingActivity.EXTRA_DESTINATION_CITY, destinationCity);
            next.putExtra(AnalyzingActivity.EXTRA_START_DATE, startDate);
            next.putExtra(AnalyzingActivity.EXTRA_END_DATE, endDate);
            startActivity(next);
            finish();
        });
    }

    private void bindField(View card, int labelRes, String value) {
        ((TextView) card.findViewById(R.id.reviewFieldLabel)).setText(labelRes);
        ((TextView) card.findViewById(R.id.reviewFieldValue)).setText(value);
    }

    private String formatDestination(String city, String country) {
        if (TextUtils.isEmpty(city) && TextUtils.isEmpty(country)) {
            return getString(R.string.value_unknown);
        }
        if (TextUtils.isEmpty(city)) return country;
        if (TextUtils.isEmpty(country)) return city;
        return city + ", " + country;
    }

    // "yyyy-MM-dd" 두 개를 "M.d — M.d · N박M일" 형식으로 바꾼다. 파싱 실패 시 원본 그대로 보여줌
    private String formatDateRange(String startDate, String endDate) {
        if (TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)) {
            return getString(R.string.value_unknown);
        }
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            iso.setLenient(false);
            Date start = iso.parse(startDate);
            Date end = iso.parse(endDate);

            SimpleDateFormat display = new SimpleDateFormat("M.d", Locale.KOREA);
            long nights = TimeUnit.MILLISECONDS.toDays(end.getTime() - start.getTime());
            return display.format(start) + " — " + display.format(end)
                    + " · " + nights + "박" + (nights + 1) + "일";
        } catch (ParseException e) {
            return startDate + " — " + endDate;
        }
    }

    private String formatTransportMode(String transportMode) {
        if ("AIR".equals(transportMode)) return getString(R.string.transport_mode_air);
        if ("OTHER".equals(transportMode)) return getString(R.string.transport_mode_other);
        return getString(R.string.value_unknown);
    }

    private void showDestinationEditDialog() {
        ArrayList<String> hints = new ArrayList<>();
        hints.add(getString(R.string.schedule_confirm_destination_label));
        ArrayList<String> values = new ArrayList<>();
        values.add(formatDestination(destinationCity, destinationCountry));

        EditFieldSheet sheet = EditFieldSheet.newInstance(
                getString(R.string.schedule_confirm_destination_label), hints, values);
        sheet.setOnFieldsSavedListener(result -> {
            String value = result.get(0);
            if (value.isEmpty()) return true;
            int commaIndex = value.indexOf(',');
            destinationCity = commaIndex >= 0 ? value.substring(0, commaIndex).trim() : value;
            destinationCountry = commaIndex >= 0 ? value.substring(commaIndex + 1).trim() : "";
            bindField(binding.confirmDestinationCard, R.string.schedule_confirm_destination_label,
                    formatDestination(destinationCity, destinationCountry));
            return true;
        });
        sheet.show(getSupportFragmentManager(), "edit_destination");
    }

    // 일정은 S08 날씨 조회에 실제로 쓰이는 값이라, 자유 텍스트 대신 시작일/종료일을 각각 입력받음
    private void showScheduleEditDialog() {
        ArrayList<String> hints = new ArrayList<>();
        hints.add(getString(R.string.schedule_confirm_start_date_hint));
        hints.add(getString(R.string.schedule_confirm_end_date_hint));
        ArrayList<String> values = new ArrayList<>();
        values.add(startDate == null ? "" : startDate);
        values.add(endDate == null ? "" : endDate);

        EditFieldSheet sheet = EditFieldSheet.newInstance(
                getString(R.string.schedule_confirm_schedule_label), hints, values, new boolean[]{true, true});
        sheet.setOnFieldsSavedListener(result -> {
            String newStart = result.get(0);
            String newEnd = result.get(1);
            if (!isValidIsoDate(newStart) || !isValidIsoDate(newEnd)) {
                Toast.makeText(this, R.string.schedule_confirm_invalid_date, Toast.LENGTH_SHORT).show();
                return false;
            }
            startDate = newStart;
            endDate = newEnd;
            bindField(binding.confirmScheduleCard, R.string.schedule_confirm_schedule_label,
                    formatDateRange(startDate, endDate));
            return true;
        });
        sheet.show(getSupportFragmentManager(), "edit_schedule");
    }

    private void showLodgingEditDialog() {
        ArrayList<String> hints = new ArrayList<>();
        hints.add(getString(R.string.schedule_confirm_lodging_label));
        ArrayList<String> values = new ArrayList<>();
        values.add(TextUtils.isEmpty(accommodationName) ? "" : accommodationName);

        EditFieldSheet sheet = EditFieldSheet.newInstance(
                getString(R.string.schedule_confirm_lodging_label), hints, values);
        sheet.setOnFieldsSavedListener(result -> {
            accommodationName = result.get(0);
            bindField(binding.confirmLodgingCard, R.string.schedule_confirm_lodging_label,
                    TextUtils.isEmpty(accommodationName) ? getString(R.string.accommodation_none) : accommodationName);
            return true;
        });
        sheet.show(getSupportFragmentManager(), "edit_lodging");
    }

    // 이동수단은 S08로 넘어가지 않는 화면 표시 전용 값이라, 수정 후엔 코드(AIR/OTHER) 대신
    // 사용자가 입력한 텍스트를 그대로 들고 있다가 그대로 보여줌
    private void showTransportEditDialog() {
        ArrayList<String> hints = new ArrayList<>();
        hints.add(getString(R.string.schedule_confirm_transport_label));
        ArrayList<String> values = new ArrayList<>();
        values.add(formatTransportMode(transportMode));

        EditFieldSheet sheet = EditFieldSheet.newInstance(
                getString(R.string.schedule_confirm_transport_label), hints, values);
        sheet.setOnFieldsSavedListener(result -> {
            transportMode = result.get(0);
            bindField(binding.confirmTransportCard, R.string.schedule_confirm_transport_label, transportMode);
            return true;
        });
        sheet.show(getSupportFragmentManager(), "edit_transport");
    }

    private boolean isValidIsoDate(String value) {
        if (value.isEmpty()) return false;
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            iso.setLenient(false);
            iso.parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
