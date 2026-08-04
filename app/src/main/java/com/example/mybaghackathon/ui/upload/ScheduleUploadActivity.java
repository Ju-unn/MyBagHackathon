package com.example.mybaghackathon.ui.upload;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityScheduleUploadBinding;
import com.example.mybaghackathon.ui.analyzing.AnalyzingActivity;
import com.google.android.material.button.MaterialButton;

/**
 * S06 · 일정 업로드 — 사진 업로드 슬롯 2개(일정표 + 숙소 예약 확인서) +
 * 국내/해외 토글.
 *
 * 기능: 업로드 안내 카드 2개(일정/숙소)의 문구를 채우고, 국내/해외 칩 토글,
 * "분석 시작" 버튼으로 AnalyzingActivity로 이동하는 화면.
 */
public class ScheduleUploadActivity extends AppCompatActivity {

    private ActivityScheduleUploadBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View scheduleCard = binding.uploadScheduleCard;
        ((TextView) scheduleCard.findViewById(R.id.uploadGuideTitle)).setText(R.string.upload_guide_schedule_title);
        ((TextView) scheduleCard.findViewById(R.id.uploadGuideDesc)).setText(R.string.upload_guide_schedule_desc);

        View hotelCard = binding.uploadHotelCard;
        ((TextView) hotelCard.findViewById(R.id.uploadGuideTitle)).setText(R.string.upload_guide_hotel_title);
        ((TextView) hotelCard.findViewById(R.id.uploadGuideDesc)).setText(R.string.upload_guide_hotel_desc);

        TextView domestic = binding.uploadSegmentDomestic;
        TextView intl = binding.uploadSegmentIntl;
        domestic.setOnClickListener(v -> {
            setSegmentSelected(domestic, true);
            setSegmentSelected(intl, false);
        });
        intl.setOnClickListener(v -> {
            setSegmentSelected(intl, true);
            setSegmentSelected(domestic, false);
        });

        MaterialButton startAnalysis = binding.uploadBottomCta.bottomCtaPrimary;
        startAnalysis.setText(R.string.upload_start_analysis);
        startAnalysis.setOnClickListener(v -> {
            startActivity(new Intent(this, AnalyzingActivity.class));
            finish();
        });
    }

    private void setSegmentSelected(TextView segment, boolean selected) {
        if (selected) {
            segment.setBackgroundResource(R.drawable.bg_segment_selected);
            segment.setTextAppearance(R.style.TextAppearance_Bag_TitleS);
        } else {
            segment.setBackground(null);
            segment.setTextAppearance(R.style.TextAppearance_Bag_BodyM);
            segment.setTextColor(ContextCompat.getColor(this, R.color.bag_text_tertiary_safe));
        }
    }
}
