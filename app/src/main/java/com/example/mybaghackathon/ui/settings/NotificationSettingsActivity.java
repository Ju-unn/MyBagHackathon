package com.example.mybaghackathon.ui.settings;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityNotificationSettingsBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;

/**
 * S15 · 알림 설정 — D-7/D-3/D-1 토글 + 날짜 누락 경고.
 *
 * 기능: 경고 배너 문구를 채워 넣는 정도만 하는, 아직 토글 로직은 붙지 않은
 * 알림 설정 화면.
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    private ActivityNotificationSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        TextView title = binding.notifTopBar.topAppBarCompactTitle;
        title.setText(R.string.notif_title);
        binding.notifTopBar.topAppBarBack.setOnClickListener(v -> finish());
    }
}
