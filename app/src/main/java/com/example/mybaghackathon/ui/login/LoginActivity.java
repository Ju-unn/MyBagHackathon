package com.example.mybaghackathon.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.BuildConfig;
import com.example.mybaghackathon.MainActivity;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.data.repository.AuthRepository;
import com.example.mybaghackathon.databinding.ActivityLoginBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.model.ClientError;
import com.kakao.sdk.common.model.ClientErrorCause;
import com.kakao.sdk.user.UserApiClient;

import kotlin.Unit;

public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    private static final String TAG = "KakaoLogin";

    private ActivityLoginBinding binding;
    private LoginContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        AuthRepository authRepository = ((MyBagApplication) getApplication()).getAppContainer().authRepository;
        presenter = new LoginPresenter(this, authRepository);

        binding.loginKakaoButton.setOnClickListener(v -> startKakaoLogin());
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    private void startKakaoLogin() {
        setLoading(true);

        if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
            UserApiClient.getInstance().loginWithKakaoTalk(this, (token, error) -> {
                if (error != null) {
                    if (isCancelled(error)) {
                        setLoading(false);
                        return Unit.INSTANCE;
                    }
                    // 카카오톡 앱 로그인이 실패하면 카카오계정(웹) 로그인으로 대체
                    UserApiClient.getInstance().loginWithKakaoAccount(this, this::onKakaoTokenResult);
                } else {
                    onKakaoTokenResult(token, null);
                }
                return Unit.INSTANCE;
            });
        } else {
            UserApiClient.getInstance().loginWithKakaoAccount(this, this::onKakaoTokenResult);
        }
    }

    private Unit onKakaoTokenResult(OAuthToken token, Throwable error) {
        if (error != null) {
            Log.e(TAG, "카카오 SDK 로그인 실패", error);
            setLoading(false);
            if (!isCancelled(error)) {
                showError("카카오 로그인에 실패했습니다.");
            }
            return Unit.INSTANCE;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "카카오 액세스 토큰 발급: " + token.getAccessToken());
        }
        presenter.login(token.getAccessToken());
        return Unit.INSTANCE;
    }

    private boolean isCancelled(Throwable error) {
        return error instanceof ClientError
                && ((ClientError) error).getReason() == ClientErrorCause.Cancelled;
    }

    // ===== LoginContract.View =====

    @Override
    public void setLoading(boolean loading) {
        binding.loginKakaoButton.setEnabled(!loading);
    }

    @Override
    public void showError(String message) {
        Log.e(TAG, "서버 로그인 실패: " + message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToMain() {
        Log.d(TAG, "서버 로그인 성공, MainActivity로 이동");
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
