package com.example.mybaghackathon.ui.analyzing;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityAnalyzingBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.review.ScheduleReviewActivity;

/**
 * S07 · 분석 중 — 4단계 AI 진행 피드를 보여주고, 끝나면 자동으로
 * 일정 확인(Schedule Review) 화면으로 넘어감.
 *
 * 기능: 진행 단계 텍스트를 순서대로 강조 표시하면서(Handler로 지연 실행)
 * AI가 일정을 분석하는 것처럼 연출한 뒤 ScheduleReviewActivity로 자동 이동.
 */
public class AnalyzingActivity extends AppCompatActivity {

    private static final int[] STEPS = {
            R.string.analyzing_step1, R.string.analyzing_step2,
            R.string.analyzing_step3, R.string.analyzing_step4};
    private static final long STEP_DELAY_MS = 700L;

    private ActivityAnalyzingBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView[] stepViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalyzingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        LinearLayout list = binding.analyzingStepList;
        stepViews = new TextView[STEPS.length];
        for (int i = 0; i < STEPS.length; i++) {
            TextView tv = new TextView(this);
            tv.setText(getString(STEPS[i]));
            tv.setTextAppearance(R.style.TextAppearance_Bag_BodyL);
            tv.setTextColor(ContextCompat.getColor(this, R.color.bag_text_tertiary_safe));
            tv.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(10);
            tv.setLayoutParams(lp);
            stepViews[i] = tv;
            list.addView(tv);
        }

        advanceStep(0);
    }

    private void advanceStep(int index) {
        if (index >= stepViews.length) {
            handler.postDelayed(() -> {
                if (isFinishing()) return;
                startActivity(new Intent(this, ScheduleReviewActivity.class));
                finish();
            }, STEP_DELAY_MS);
            return;
        }
        stepViews[index].setTextColor(ContextCompat.getColor(this, R.color.bag_text_primary));
        stepViews[index].setTypeface(null, android.graphics.Typeface.BOLD);
        handler.postDelayed(() -> advanceStep(index + 1), STEP_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
