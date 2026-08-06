package com.example.mybaghackathon.service;

import android.provider.Settings;

import com.example.mybaghackathon.BuildConfig;
import com.example.mybaghackathon.app.AppContainer;
import com.example.mybaghackathon.app.MyBagApplication;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

// FCM 토큰 발급/갱신과 data 메시지 수신을 처리. 알림 UI(아이콘·채널·매니페스트 등록)는 안드로이드팀이 붙일 예정
public class FcmMessagingService extends FirebaseMessagingService {

    // 토큰이 새로 발급/갱신되면 서버에 등록한다. 로그인 전이면 서버가 401을 줄 것이므로 스킵
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        AppContainer appContainer = ((MyBagApplication) getApplication()).getAppContainer();
        if (!appContainer.tokenStorage.isLoggedIn()) {
            return;
        }

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        appContainer.authRepository.registerFcmToken(token, deviceId, "ANDROID", BuildConfig.VERSION_NAME);
    }

    // data 메시지 수신 지점. 실제 알림 표시(채널 생성, 아이콘, 딥링크 등)는 안드로이드팀이 여기 이어서 구현
    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        // TODO(android팀): message.getData()를 읽어 로컬 알림으로 표시
    }
}
