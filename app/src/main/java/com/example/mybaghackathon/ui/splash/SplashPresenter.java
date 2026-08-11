package com.example.mybaghackathon.ui.splash;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.data.local.TokenStorage;
import com.example.mybaghackathon.data.repository.AuthRepository;
import com.kakao.sdk.auth.TokenManagerProvider;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Unit;

// SplashContract.Presenter 구현체 — 로그인 여부(tokenStorage)를 확인해 다음 화면을 정하고,
// 메인 스레드의 View에 이동을 알린다. 이미 로그인된 상태라면 카카오 SDK에 남아있는 세션으로
// 프로필(닉네임/이미지)을 조용히 재조회해 로컬 캐시를 최신화한다(화면 전환은 기다리지 않음).
public class SplashPresenter implements SplashContract.Presenter {

    private static final long SPLASH_DELAY_MS = 1000L;

    private final SplashContract.View view;
    private final TokenStorage tokenStorage;
    private final AuthRepository authRepository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private volatile boolean destroyed = false;

    public SplashPresenter(SplashContract.View view, TokenStorage tokenStorage, AuthRepository authRepository) {
        this.view = view;
        this.tokenStorage = tokenStorage;
        this.authRepository = authRepository;
    }

    @Override
    public void proceedAfterDelay() {
        mainHandler.postDelayed(this::proceedNow, SPLASH_DELAY_MS);
    }

    @Override
    public void proceedNow() {
        if (destroyed) return;
        if (tokenStorage.isLoggedIn()) {
            refreshProfileInBackground();
            view.navigateToMain();
        } else {
            view.navigateToLogin();
        }
    }

    // 카카오 세션이 남아있으면(로그인 창 없이) 최신 액세스 토큰을 받아 kakao_login.php를
    // 다시 호출한다 — 서버가 그 안에서 upsert하며 닉네임/프로필 이미지를 최신화해준다.
    // 카카오 세션이 없거나 실패해도 화면 전환은 이미 진행 중이라 사용자는 영향받지 않고,
    // 캐시된(이전) 프로필이 그대로 보일 뿐이다.
    private void refreshProfileInBackground() {
        UserApiClient.getInstance().accessTokenInfo((info, error) -> {
            if (destroyed || error != null) {
                return Unit.INSTANCE;
            }
            OAuthToken kakaoToken = TokenManagerProvider.getInstance().getManager().getToken();
            if (kakaoToken == null) {
                return Unit.INSTANCE;
            }
            // 이미 최초 로그인 때 동의한 사용자의 조용한 프로필 갱신 — 동의 값 false로 넘겨
            // 서버가 기존 동의 기록(agreed_at)을 건드리지 않게 함(서버는 true일 때만 기록)
            executor.execute(() -> authRepository.loginWithKakao(kakaoToken.getAccessToken(), false, false));
            return Unit.INSTANCE;
        });
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        mainHandler.removeCallbacksAndMessages(null);
        executor.shutdown();
    }
}
