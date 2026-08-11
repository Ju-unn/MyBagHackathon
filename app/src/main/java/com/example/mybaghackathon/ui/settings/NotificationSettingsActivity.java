package com.example.mybaghackathon.ui.settings;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.databinding.ActivityNotificationSettingsBinding;
import com.example.mybaghackathon.model.NotificationSettings;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;

/**
 * S16 · 알림 설정 — D-7/D-3/D-1 토글 + 날짜 누락 경고.
 *
 * 기능: NotificationSettingsPresenter가 불러온 서버 설정을 스위치에 반영하고,
 * 사용자가 스위치를 바꾸면 즉시 서버에 저장한다.
 */
public class NotificationSettingsActivity extends AppCompatActivity implements NotificationSettingsContract.View {

    private ActivityNotificationSettingsBinding binding;
    private NotificationSettingsContract.Presenter presenter;
    // 서버에서 불러온 값을 스위치에 반영하는 동안 리스너가 다시 서버로 저장 요청을 보내지 않도록 막는 플래그
    private boolean applyingRemoteState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        TextView title = binding.notifTopBar.topAppBarCompactTitle;
        title.setText(R.string.notif_title);
        binding.notifTopBar.topAppBarBack.setOnClickListener(v -> finish());

        presenter = new NotificationSettingsPresenter(this,
                ((MyBagApplication) getApplication()).getAppContainer().notificationSettingsRepository);

        binding.notifSwitchD7.setOnCheckedChangeListener((button, checked) -> {
            if (!applyingRemoteState) {
                presenter.updateD7(checked);
            }
        });
        binding.notifSwitchD3.setOnCheckedChangeListener((button, checked) -> {
            if (!applyingRemoteState) {
                presenter.updateD3(checked);
            }
        });
        binding.notifSwitchD1.setOnCheckedChangeListener((button, checked) -> {
            if (!applyingRemoteState) {
                presenter.updateD1(checked);
            }
        });

        presenter.loadSettings();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        binding = null;
        super.onDestroy();
    }

    // ===== NotificationSettingsContract.View =====

    @Override
    public void showSettings(NotificationSettings settings) {
        if (binding == null) {
            return;
        }
        applyingRemoteState = true;
        binding.notifSwitchD7.setChecked(settings.isDDay7Enabled());
        binding.notifSwitchD3.setChecked(settings.isDDay3Enabled());
        binding.notifSwitchD1.setChecked(settings.isDDay1Enabled());
        applyingRemoteState = false;
        setSwitchesLoading(false);
    }

    @Override
    public void showError(String message) {
        if (isFinishing()) {
            return;
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (binding != null) {
            setSwitchesLoading(false);
        }
    }

    // 서버 값이 도착하기 전까지 스위치를 비활성/반투명 상태로 두어, XML 기본값에서
    // 실제 서버 값으로 스위치가 눈에 띄게 움직이는 것을 방지한다.
    private void setSwitchesLoading(boolean loading) {
        float alpha = loading ? 0.4f : 1f;
        binding.notifSwitchD7.setEnabled(!loading);
        binding.notifSwitchD3.setEnabled(!loading);
        binding.notifSwitchD1.setEnabled(!loading);
        binding.notifSwitchD7.setAlpha(alpha);
        binding.notifSwitchD3.setAlpha(alpha);
        binding.notifSwitchD1.setAlpha(alpha);
    }
}
