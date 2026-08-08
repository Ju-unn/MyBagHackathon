package com.example.mybaghackathon.ui.splash;

// S01 스플래시 화면의 View/Presenter 계약
public interface SplashContract {

    interface View {
        void navigateToMain();
        void navigateToLogin();
    }

    interface Presenter {
        // 알림 권한을 물어볼 필요가 없을 때 — 최소 노출 시간을 채운 뒤 다음 화면으로 이동한다
        void proceedAfterDelay();

        // 권한 요청 응답을 받은 직후 — 지연 없이 바로 다음 화면으로 이동한다
        void proceedNow();

        // Activity가 파괴될 때 호출 — 이후 View 콜백을 막는다
        void onDestroy();
    }
}
