package com.example.mybaghackathon.ui.analysisresult;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityAnalysisResultBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.analyzing.AnalyzingActivity;
import com.example.mybaghackathon.ui.review.ScheduleReviewActivity;

/**
 * S07 · 1차 결과 확인 — AI 분석 직후 여행지·일정·숙소·이동수단만 빠르게
 * 확인하는 화면. "다시 분석하기"로 재분석, "다음"으로 상세 검토(S08)로 진행.
 */
public class AnalysisResultActivity extends AppCompatActivity {

    private ActivityAnalysisResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalysisResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        binding.confirmTopAppBar.topAppBarCompactTitle.setText(R.string.schedule_confirm_title);
        binding.confirmTopAppBar.topAppBarBack.setOnClickListener(v -> finish());

        bindField(binding.confirmDestinationCard, R.string.schedule_confirm_destination_label, "도쿄, 일본");
        bindField(binding.confirmScheduleCard, R.string.schedule_confirm_schedule_label, "3.14 — 3.18 · 4박5일");
        bindField(binding.confirmLodgingCard, R.string.schedule_confirm_lodging_label, "도쿄 신주쿠 호텔");
        bindField(binding.confirmTransportCard, R.string.schedule_confirm_transport_label, "항공권");

        binding.confirmRetryButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AnalyzingActivity.class));
            finish();
        });
        binding.confirmNextButton.setOnClickListener(v -> {
            startActivity(new Intent(this, ScheduleReviewActivity.class));
            finish();
        });
    }

    private void bindField(View card, int labelRes, String value) {
        ((TextView) card.findViewById(R.id.reviewFieldLabel)).setText(labelRes);
        ((TextView) card.findViewById(R.id.reviewFieldValue)).setText(value);
    }
}
