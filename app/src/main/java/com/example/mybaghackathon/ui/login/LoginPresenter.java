package com.example.mybaghackathon.ui.login;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.BuildConfig;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.AuthRepository;
import com.example.mybaghackathon.model.User;
import com.google.firebase.messaging.FirebaseMessaging;

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
    public void login(String kakaoAccessToken, boolean privacyAgreed, boolean termsAgreed) {
        view.setLoading(true);
        executor.execute(() -> {
            AppResult<User> result = authRepository.loginWithKakao(kakaoAccessToken, privacyAgreed, termsAgreed);
            mainHandler.post(() -> {
                if (destroyed) return;
                view.setLoading(false);
                if (result.isSuccess()) {
                    registerFcmToken();
                    view.navigateToMain();
                } else {
                    view.showError(result.getError().getMessage());
                }
            });
        });
    }

    // onNewToken()은 토큰이 새로 생성/갱신될 때만 불려서, 로그인 전에 이미 발급된 토큰은
    // 서버에 등록될 기회가 없을 수 있다 — 로그인 성공 직후 현재 토큰을 직접 가져와 등록해
    // 그 틈을 메운다. Activity가 곧바로 화면을 전환/종료하므로 presenter의 executor(로그인
    // 완료와 함께 곧 shutdown됨)에 묶지 않고 별도 스레드에서 fire-and-forget으로 처리한다.
    private void registerFcmToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            }
            String token = task.getResult();
            new Thread(() ->
                    authRepository.registerFcmToken(token, null, "ANDROID", BuildConfig.VERSION_NAME)
            ).start();
        });
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        executor.shutdown();
    }
}
