package com.example.mybaghackathon.ui.splash;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.data.local.TokenStorage;

// SplashContract.Presenter 구현체 — 로그인 여부(tokenStorage)를 확인해 다음 화면을 정하고,
// 메인 스레드의 View에 이동을 알린다
public class SplashPresenter implements SplashContract.Presenter {

    private static final long SPLASH_DELAY_MS = 1000L;

    private final SplashContract.View view;
    private final TokenStorage tokenStorage;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean destroyed = false;

    public SplashPresenter(SplashContract.View view, TokenStorage tokenStorage) {
        this.view = view;
        this.tokenStorage = tokenStorage;
    }

    @Override
    public void proceedAfterDelay() {
        mainHandler.postDelayed(this::proceedNow, SPLASH_DELAY_MS);
    }

    @Override
    public void proceedNow() {
        if (destroyed) return;
        if (tokenStorage.isLoggedIn()) {
            view.navigateToMain();
        } else {
            view.navigateToLogin();
        }
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        mainHandler.removeCallbacksAndMessages(null);
    }
}
