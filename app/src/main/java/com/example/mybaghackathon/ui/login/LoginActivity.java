package com.example.mybaghackathon.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.MainActivity;
import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.data.repository.AuthRepository;
import com.example.mybaghackathon.databinding.ActivityLoginBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.invite.InviteJoinActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.model.ClientError;
import com.kakao.sdk.common.model.ClientErrorCause;
import com.kakao.sdk.user.UserApiClient;

import kotlin.Unit;

public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    public static final String EXTRA_POST_LOGIN_INVITE_CODE = "post_login_invite_code";
    private static final String TAG = "KakaoLogin";
    private static final String PRIVACY_POLICY_URL = "https://mybag.duckdns.org/privacy-policy.html";
    private static final String TERMS_URL = "https://mybag.duckdns.org/terms.html";

    private ActivityLoginBinding binding;
    private LoginContract.Presenter presenter;
    // 복구 확인 다이얼로그에서 "복구하기"를 눌렀을 때 같은 토큰으로 재시도하기 위해 보관
    private String pendingKakaoAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPaddingNoTop(this, binding.getRoot());

        AuthRepository authRepository = ((MyBagApplication) getApplication()).getAppContainer().authRepository;
        presenter = new LoginPresenter(this, authRepository);

        binding.loginKakaoButton.setOnClickListener(v -> startKakaoLogin());

        // 필수 동의 게이팅 — 둘 다 체크해야 카카오 버튼 활성화
        binding.agreePrivacy.setOnCheckedChangeListener((b, checked) -> updateLoginButtonState());
        binding.agreeTerms.setOnCheckedChangeListener((b, checked) -> updateLoginButtonState());
        binding.viewPrivacy.setOnClickListener(v -> openUrl(PRIVACY_POLICY_URL));
        binding.viewTerms.setOnClickListener(v -> openUrl(TERMS_URL));
        updateLoginButtonState();
    }

    // 개인정보처리방침·이용약관 필수 동의가 모두 체크됐을 때만 로그인 버튼 활성화
    private void updateLoginButtonState() {
        boolean bothAgreed = binding.agreePrivacy.isChecked() && binding.agreeTerms.isChecked();
        binding.loginKakaoButton.setEnabled(bothAgreed);
        binding.loginKakaoButton.setAlpha(bothAgreed ? 1f : 0.4f);
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    private void startKakaoLogin() {
        if (!binding.agreePrivacy.isChecked() || !binding.agreeTerms.isChecked()) {
            Toast.makeText(this, R.string.login_agree_required, Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);

        if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
            UserApiClient.getInstance().loginWithKakaoTalk(this, (token, error) -> {
                if (error != null) {
                    Log.e(TAG, "loginWithKakaoTalk 실패", error);
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
            Log.e(TAG, "카카오 토큰 획득 실패(계정로그인 포함)", error);
            setLoading(false);
            if (!isCancelled(error)) {
                showError("카카오 로그인에 실패했습니다.");
            }
            return Unit.INSTANCE;
        }
        pendingKakaoAccessToken = token.getAccessToken();
        presenter.login(pendingKakaoAccessToken, binding.agreePrivacy.isChecked(), binding.agreeTerms.isChecked(), false);
        return Unit.INSTANCE;
    }

    private boolean isCancelled(Throwable error) {
        return error instanceof ClientError
                && ((ClientError) error).getReason() == ClientErrorCause.Cancelled;
    }

    // ===== LoginContract.View =====

    @Override
    public void setLoading(boolean loading) {
        if (loading) {
            binding.loginKakaoButton.setEnabled(false);
        } else {
            updateLoginButtonState();
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showRestoreConfirmation(String withdrawnAt) {
        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Bag_ConfirmDialog)
                .setTitle(R.string.login_restore_dialog_title)
                .setMessage(R.string.login_restore_dialog_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.login_restore_confirm, (dialog, which) ->
                        presenter.login(pendingKakaoAccessToken, binding.agreePrivacy.isChecked(), binding.agreeTerms.isChecked(), true))
                .show();
    }

    @Override
    public void navigateToMain() {
        String inviteCode = getIntent().getStringExtra(EXTRA_POST_LOGIN_INVITE_CODE);
        if (inviteCode != null && !inviteCode.trim().isEmpty()) {
            Intent inviteIntent = new Intent(this, InviteJoinActivity.class);
            inviteIntent.putExtra(InviteJoinActivity.EXTRA_INVITE_CODE, inviteCode.trim());
            startActivity(inviteIntent);
            finish();
            return;
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
