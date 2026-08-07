package com.example.mybaghackathon.ui.splash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.MainActivity;
import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.databinding.ActivitySplashBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.login.LoginActivity;

/**
 * S01 · 스플래시 — 콜드 스타트 시 잠깐 보여주는 브랜드 화면. 이후 로그인
 * 화면으로 라우팅함.
 *
 * 기능: 일정 시간(1초) 뒤 LoginActivity로 자동 이동하는 스플래시 화면.
 * FCM 알림 수신을 위한 POST_NOTIFICATIONS 런타임 권한도 여기서 요청함.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1000L;

    private ActivitySplashBinding binding;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        requestNotificationPermission();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing()) return;

            boolean isLoggedIn = ((MyBagApplication) getApplication())
                    .getAppContainer().tokenStorage.isLoggedIn();
            Intent next = new Intent(this, isLoggedIn ? MainActivity.class : LoginActivity.class);
            startActivity(next);
            finish();
        }, SPLASH_DELAY_MS);
    }

    // Android 13(API 33)+ 에서는 POST_NOTIFICATIONS를 런타임에 승인받아야 FCM 알림이 표시됨
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }
}
