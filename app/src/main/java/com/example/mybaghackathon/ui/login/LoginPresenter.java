package com.example.mybaghackathon.ui.login;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.AuthRepository;
import com.example.mybaghackathon.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// LoginContract.Presenter 구현체 — authRepository.loginWithKakao() 호출을
// 백그라운드 스레드에서 실행하고 결과를 메인 스레드의 View로 전달한다
public class LoginPresenter implements LoginContract.Presenter {

    private final LoginContract.View view;
    private final AuthRepository authRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean destroyed = false;

    public LoginPresenter(LoginContract.View view, AuthRepository authRepository) {
        this.view = view;
        this.authRepository = authRepository;
    }

    @Override
    public void login(String kakaoAccessToken) {
        view.setLoading(true);
        executor.execute(() -> {
            AppResult<User> result = authRepository.loginWithKakao(kakaoAccessToken);
            mainHandler.post(() -> {
                if (destroyed) return;
                view.setLoading(false);
                if (result.isSuccess()) {
                    view.navigateToMain();
                } else {
                    view.showError(result.getError().getMessage());
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        executor.shutdown();
    }
}
