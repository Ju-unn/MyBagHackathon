package com.example.mybaghackathon.ui.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;

/** S15 · Notification Settings — D-7/D-3/D-1 toggles + missing-date warning. */
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
