package com.example.mybaghackathon.ui.analyzing;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityAnalyzingBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.analysisresult.AnalysisResultActivity;

/**
 * S06 · 분석 중 — 진행 문구를 한 줄씩 페이드 아웃/페이드 인으로 교체하며
 * 보여주고, 4단계가 모두 끝나면 자동으로 1차 결과 확인(S07) 화면으로 넘어감.
 */
public class AnalyzingActivity extends AppCompatActivity {

    private static final int[] STEPS = {
            R.string.analyzing_step1, R.string.analyzing_step2,
            R.string.analyzing_step3, R.string.analyzing_step4};
    private static final long FADE_DURATION_MS = 300L;
    private static final long DISPLAY_DURATION_MS = 900L;

    private ActivityAnalyzingBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView stepText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalyzingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        stepText = binding.analyzingStepText;
        stepText.setAlpha(0f);
        showStep(0);
    }

    private void showStep(int index) {
        if (index >= STEPS.length) {
            handler.postDelayed(() -> {
                if (isFinishing()) return;
                startActivity(new Intent(this, AnalysisResultActivity.class));
                finish();
            }, DISPLAY_DURATION_MS);
            return;
        }

        stepText.setText(STEPS[index]);
        stepText.animate().alpha(1f).setDuration(FADE_DURATION_MS).withEndAction(() ->
                handler.postDelayed(() ->
                        stepText.animate().alpha(0f).setDuration(FADE_DURATION_MS).withEndAction(() ->
                                showStep(index + 1)).start(),
                        DISPLAY_DURATION_MS)).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        stepText.animate().cancel();
    }
}
