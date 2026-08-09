package com.example.mybaghackathon.ui;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

// 로딩 화면에서 문구 여러 개를 페이드 아웃/페이드 인으로 계속 반복 재생 (서버 응답 시간을 미리 알 수 없어서)
public class StepTextAnimator {

    private static final long FADE_DURATION_MS = 300L;
    private static final long DISPLAY_DURATION_MS = 900L;

    private final TextView textView;
    private final int[] steps;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public StepTextAnimator(TextView textView, int[] steps) {
        this.textView = textView;
        this.steps = steps;
    }

    public void start() {
        textView.setAlpha(0f);
        showStep(0);
    }

    public void stop() {
        handler.removeCallbacksAndMessages(null);
        textView.animate().cancel();
    }

    private void showStep(int index) {
        int step = index % steps.length;
        textView.setText(steps[step]);
        textView.animate().alpha(1f).setDuration(FADE_DURATION_MS).withEndAction(() ->
                handler.postDelayed(() ->
                        textView.animate().alpha(0f).setDuration(FADE_DURATION_MS).withEndAction(() ->
                                showStep(index + 1)).start(),
                        DISPLAY_DURATION_MS)).start();
    }
}
