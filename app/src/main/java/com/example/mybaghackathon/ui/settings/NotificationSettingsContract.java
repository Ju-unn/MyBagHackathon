package com.example.mybaghackathon.ui.settings;

import com.example.mybaghackathon.model.NotificationSettings;

// S16 알림 설정 화면의 View/Presenter 계약
public interface NotificationSettingsContract {

    interface View {
        void showSettings(NotificationSettings settings);

        void showError(String message);
    }

    interface Presenter {
        void loadSettings();

        void updateD7(boolean enabled);

        void updateD3(boolean enabled);

        void updateD1(boolean enabled);

        // Activity가 파괴될 때 호출 — 이후 View 콜백을 막는다
        void onDestroy();
    }
}
