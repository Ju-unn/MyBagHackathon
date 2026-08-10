package com.example.mybaghackathon.ui.splash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.MainActivity;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.databinding.ActivitySplashBinding;
import com.example.mybaghackathon.ui.login.LoginActivity;

/**
 * S01 · 스플래시 — 콜드 스타트 시 잠깐 보여주는 브랜드 화면. 화면 표시와 알림 권한
 * 요청만 담당하고, 로그인 여부 판단 및 다음 화면 결정은 SplashPresenter가 처리함(MVP).
 */
public class SplashActivity extends AppCompatActivity implements SplashContract.View {

    private ActivitySplashBinding binding;
    private SplashContract.Presenter presenter;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> presenter.proceedNow());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 풀블리드 이미지 한 장짜리 화면이라 시스템 바 패딩을 넣지 않음(다른 화면과 달리
        // 여백이 생기면 이미지가 안으로 밀려 배경색이 하단에 노출됨).
        EdgeToEdge.enable(this);
        // API 29+에서는 3버튼 내비게이션 바 위에 시스템이 자동으로 옅은 스크림을 덧그려서
        // navigationBarColor를 지정해도 흰 줄처럼 보임 — 스크림을 꺼서 이미지가 그대로 비치게 함.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }

        presenter = new SplashPresenter(this,
                ((MyBagApplication) getApplication()).getAppContainer().tokenStorage);

        requestNotificationPermission();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    // Android 13(API 33)+ 에서는 POST_NOTIFICATIONS를 런타임에 승인받아야 FCM 알림이 표시됨.
    // 권한을 물어봐야 하는 경우엔 사용자가 허용/거부를 선택한 뒤 지연 없이 다음 화면으로 이동한다.
    private void requestNotificationPermission() {
        boolean needsRequest = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED;

        if (needsRequest) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            presenter.proceedAfterDelay();
        }
    }

    // ===== SplashContract.View =====

    @Override
    public void navigateToMain() {
        if (isFinishing()) return;
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void navigateToLogin() {
        if (isFinishing()) return;
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
