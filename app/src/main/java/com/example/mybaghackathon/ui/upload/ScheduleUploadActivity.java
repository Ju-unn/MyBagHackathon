package com.example.mybaghackathon.ui.upload;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.ui.analyzing.AnalyzingActivity;
import com.example.mybaghackathon.ui.atoms.ChipView;
import com.google.android.material.button.MaterialButton;

/**
 * S06 · 일정 업로드 — 사진 업로드 슬롯 2개(일정표 + 숙소 예약 확인서) +
 * 국내/해외 토글.
 *
 * 기능: 업로드 안내 카드 2개(일정/숙소)의 문구를 채우고, 국내/해외 칩 토글,
 * "분석 시작" 버튼으로 AnalyzingActivity로 이동하는 화면.
 */
public class ScheduleUploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_upload);

        TextView title = findViewById(R.id.topAppBarCompactTitle);
        title.setText(R.string.upload_title);
        findViewById(R.id.topAppBarBack).setOnClickListener(v -> finish());

        View scheduleCard = findViewById(R.id.uploadScheduleCard);
        ((TextView) scheduleCard.findViewById(R.id.uploadGuideTitle)).setText(R.string.upload_guide_schedule_title);
        ((TextView) scheduleCard.findViewById(R.id.uploadGuideDesc)).setText(R.string.upload_guide_schedule_desc);

        View hotelCard = findViewById(R.id.uploadHotelCard);
        ((TextView) hotelCard.findViewById(R.id.uploadGuideTitle)).setText(R.string.upload_guide_hotel_title);
        ((TextView) hotelCard.findViewById(R.id.uploadGuideDesc)).setText(R.string.upload_guide_hotel_desc);

        ChipView domestic = findViewById(R.id.uploadSegmentDomestic);
        ChipView intl = findViewById(R.id.uploadSegmentIntl);
        domestic.setOnClickListener(v -> {
            domestic.setActive(true);
            intl.setActive(false);
        });
        intl.setOnClickListener(v -> {
            intl.setActive(true);
            domestic.setActive(false);
        });

        MaterialButton startAnalysis = findViewById(R.id.bottomCtaPrimary);
        startAnalysis.setText(R.string.upload_start_analysis);
        startAnalysis.setOnClickListener(v -> {
            startActivity(new Intent(this, AnalyzingActivity.class));
            finish();
        });
    }
}
