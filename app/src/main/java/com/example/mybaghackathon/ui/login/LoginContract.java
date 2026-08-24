package com.example.mybaghackathon.ui.login;

// S02 로그인 화면의 View/Presenter 계약
public interface LoginContract {

    interface View {
        void setLoading(boolean loading);
        void showError(String message);
        void navigateToMain();

        // 탈퇴 후 유예기간(30일) 내 계정으로 로그인 시도한 경우 — 복구 여부를 확인하는 다이얼로그를 띄운다
        void showRestoreConfirmation(String withdrawnAt);
    }

    interface Presenter {
        // 카카오 access_token으로 서버 로그인을 시도한다. 필수 동의 값도 함께 전달.
        // restoreConfirmed=true는 복구 확인 다이얼로그를 거친 뒤 재시도할 때만 true로 보낸다
        void login(String kakaoAccessToken, boolean privacyAgreed, boolean termsAgreed, boolean restoreConfirmed);

        // Activity가 파괴될 때 호출 — 이후 View 콜백을 막는다
        void onDestroy();
    }
}
