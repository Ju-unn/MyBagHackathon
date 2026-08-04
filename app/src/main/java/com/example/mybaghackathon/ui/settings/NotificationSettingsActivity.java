package com.example.mybaghackathon.ui.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;

/**
 * S15 · 알림 설정 — D-7/D-3/D-1 토글 + 날짜 누락 경고.
 *
 * 기능: 경고 배너 문구를 채워 넣는 정도만 하는, 아직 토글 로직은 붙지 않은
 * 알림 설정 화면.
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        TextView title = findViewById(R.id.topAppBarCompactTitle);
        title.setText(R.string.notif_title);
        findViewById(R.id.topAppBarBack).setOnClickListener(v -> finish());

        View banner = findViewById(R.id.notifWarningBanner);
        ((TextView) banner.findViewById(R.id.warningBannerText)).setText(R.string.notif_warning);
    }
}
