package com.example.mybaghackathon.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivitySplashBinding;
import com.example.mybaghackathon.ui.login.LoginActivity;

/**
 * S01 · 스플래시 — 콜드 스타트 시 잠깐 보여주는 브랜드 화면. 이후 로그인
 * 화면으로 라우팅함.
 *
 * 기능: 일정 시간(1초) 뒤 LoginActivity로 자동 이동하는 스플래시 화면.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1000L;

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing()) return;
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }, SPLASH_DELAY_MS);
    }
}
