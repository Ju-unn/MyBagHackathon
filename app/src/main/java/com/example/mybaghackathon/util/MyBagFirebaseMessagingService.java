package com.example.mybaghackathon.util;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

// FCM data 메시지 수신 + 토큰 갱신. 서버(FcmApiClient::send)는 notification 페이로드를 안 보내고 data만 사용
public class MyBagFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // TODO: ApiClient(Retrofit)/NotificationRepository 구현되면 여기서
        // POST /api/auth/fcm-token.php 호출 — 요청 {"token","device_id","platform":"ANDROID","app_version"}
        // 응답 {"success":true,"message":"FCM 토큰 등록 완료","data":[]}
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Map<String, String> data = message.getData();
        // TODO: data 페이로드 파싱 후 NotificationCompat으로 로컬 알림 표시
    }
}
