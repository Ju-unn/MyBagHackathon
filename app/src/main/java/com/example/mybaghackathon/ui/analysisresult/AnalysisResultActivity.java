package com.example.mybaghackathon.ui.analysisresult;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityAnalysisResultBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.analyzing.AnalyzingActivity;
import com.example.mybaghackathon.ui.review.ScheduleReviewActivity;
import com.example.mybaghackathon.ui.upload.ScheduleUploadActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        String destinationCountry = intent.getStringExtra(AnalyzingActivity.EXTRA_DESTINATION_COUNTRY);
        String destinationCity = intent.getStringExtra(AnalyzingActivity.EXTRA_DESTINATION_CITY);
        String startDate = intent.getStringExtra(AnalyzingActivity.EXTRA_START_DATE);
        String endDate = intent.getStringExtra(AnalyzingActivity.EXTRA_END_DATE);
        String transportMode = intent.getStringExtra(AnalyzingActivity.EXTRA_TRANSPORT_MODE);
        String accommodationName = intent.getStringExtra(AnalyzingActivity.EXTRA_ACCOMMODATION_NAME);

        bindField(binding.confirmDestinationCard, R.string.schedule_confirm_destination_label,
                formatDestination(destinationCity, destinationCountry));
        bindField(binding.confirmScheduleCard, R.string.schedule_confirm_schedule_label,
                formatDateRange(startDate, endDate));
        bindField(binding.confirmLodgingCard, R.string.schedule_confirm_lodging_label,
                TextUtils.isEmpty(accommodationName) ? getString(R.string.accommodation_none) : accommodationName);
        bindField(binding.confirmTransportCard, R.string.schedule_confirm_transport_label,
                formatTransportMode(transportMode));

        binding.confirmRetryButton.setOnClickListener(v -> {
            Intent retry = new Intent(this, AnalyzingActivity.class);
            retry.putExtra(ScheduleUploadActivity.EXTRA_UPLOAD_IDS, uploadIds);
            startActivity(retry);
            finish();
        });
        binding.confirmNextButton.setOnClickListener(v -> {
            Intent next = new Intent(this, ScheduleReviewActivity.class);
            next.putExtra(AnalyzingActivity.EXTRA_ANALYSIS_ID, analysisId);
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
}
