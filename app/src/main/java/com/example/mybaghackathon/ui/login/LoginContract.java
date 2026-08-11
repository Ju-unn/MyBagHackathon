package com.example.mybaghackathon.ui.login;

// S02 로그인 화면의 View/Presenter 계약
public interface LoginContract {

    interface View {
        void setLoading(boolean loading);
        void showError(String message);
        void navigateToMain();
    }

    interface Presenter {
        // 카카오 access_token으로 서버 로그인을 시도한다. 필수 동의 값도 함께 전달
        void login(String kakaoAccessToken, boolean privacyAgreed, boolean termsAgreed);

        // Activity가 파괴될 때 호출 — 이후 View 콜백을 막는다
        void onDestroy();
    }
}
