package com.example.mybaghackathon.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mybaghackathon.BuildConfig;
import com.example.mybaghackathon.MainActivity;
import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.AppContainer;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.ui.roomdetail.RoomDetailActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

// FCM 토큰 발급/갱신과 data 메시지 수신을 처리한다. 서버는 data 페이로드만 보내므로
// (notification 페이로드 없음) 알림 채널·문구·딥링크는 전부 이 서비스가 직접 구성한다.
// 여기서는 프로필 알림 설정(D-7/D-3/D-1) 범위인 DEPARTURE_D*와 방 삭제(TRIP_DELETED) 타입을 처리한다.
// data 필드는 mybagbackend의 DepartureNotificationService/TripService 기준. CHECKLIST_ASSIGNED 등
// 다른 타입은 담당 기능 구현 시 별도로 추가될 예정이라 여기서는 조용히 무시한다.
public class FcmMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "trip_notifications";
    private static final String DEPARTURE_PREFIX = "DEPARTURE_D";
    private static final String TYPE_TRIP_DELETED = "TRIP_DELETED";

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

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        Map<String, String> data = message.getData();
        String type = data.get("type");
        if (type == null) {
            return;
        }

        if (type.equals(TYPE_TRIP_DELETED)) {
            // 방이 이미 삭제됐으니 RoomDetail이 아니라 홈으로 보낸다
            showNotification(valueOf(data, "trip_name"), getString(R.string.fcm_trip_deleted_body), null);
            return;
        }

        if (!type.startsWith(DEPARTURE_PREFIX)) {
            // 담당자 지정(CHECKLIST_ASSIGNED) 등 다른 타입은 다루지 않는다
            return;
        }

        int days = parseDays(type);
        String title = getString(R.string.fcm_departure_title_format, days, valueOf(data, "trip_name"));
        String body = getString(R.string.fcm_departure_body_format, valueOf(data, "destination_city"));

        showNotification(title, body, data.get("trip_id"));
    }

    private void showNotification(String title, String body, String tripId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (!notificationManager.areNotificationsEnabled()) {
            // 사용자가 알림 권한을 거절했거나 시스템 설정에서 알림을 껐으면 그리지 않는다
            return;
        }

        ensureChannel();

        PendingIntent contentIntent = buildContentIntent(tripId);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (contentIntent != null) {
            builder.setContentIntent(contentIntent);
        }

        int notificationId = tripId != null ? tripId.hashCode() : (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }

    // tripId가 없으면(예: 방이 이미 삭제된 TRIP_DELETED) RoomDetail로 딥링크할 곳이 없으니 홈으로 보낸다
    private PendingIntent buildContentIntent(String tripId) {
        long parsedTripId = 0;
        if (tripId != null) {
            try {
                parsedTripId = Long.parseLong(tripId);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        Intent intent;
        if (tripId != null) {
            intent = new Intent(this, RoomDetailActivity.class);
            intent.putExtra(RoomDetailActivity.EXTRA_TRIP_ID, parsedTripId);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getActivity(this, (int) parsedTripId, intent, flags);
    }

    private void ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null || manager.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, getString(R.string.fcm_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(getString(R.string.fcm_channel_desc));
        manager.createNotificationChannel(channel);
    }

    private int parseDays(String type) {
        // "DEPARTURE_D7" -> 7
        try {
            return Integer.parseInt(type.substring(DEPARTURE_PREFIX.length()));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return 0;
        }
    }

    private String valueOf(Map<String, String> data, String key) {
        String value = data.get(key);
        return value == null ? "" : value;
    }
}
