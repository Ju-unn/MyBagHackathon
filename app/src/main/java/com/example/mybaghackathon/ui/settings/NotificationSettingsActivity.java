package com.example.mybaghackathon.ui.settings;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.databinding.ActivityNotificationSettingsBinding;
import com.example.mybaghackathon.model.NotificationSettings;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * S16 · 알림 설정 — 알림 받기 마스터 스위치 + D-7/D-3/D-1 토글 + 날짜 누락 경고.
 *
 * 기능: NotificationSettingsPresenter가 불러온 서버 설정을 스위치에 반영하고,
 * 사용자가 스위치를 바꾸면 즉시 서버에 저장한다. 알림 받기 스위치는 D-7/D-3/D-1을
 * 한 번에 켜고 끄는 앱 레벨 설정이다(기기 알림 권한 자체는 앱이 스스로 끌 수 없으므로,
 * 권한이 꺼져 있을 땐 그 사실을 안내만 하고 실제 on/off는 항상 앱 안에서 즉시 처리한다).
 */
public class NotificationSettingsActivity extends AppCompatActivity implements NotificationSettingsContract.View {

    private ActivityNotificationSettingsBinding binding;
    private NotificationSettingsContract.Presenter presenter;
    // 서버에서 불러온 값(또는 마스터 스위치로 인한 일괄 반영)을 스위치에 세팅하는 동안
    // 리스너가 다시 서버로 저장 요청을 보내지 않도록 막는 플래그
    private boolean applyingRemoteState;
    // 사용자가 실제로 원하는 D-7/D-3/D-1 설정값(서버 저장값). 기기 알림 권한이 꺼져 있으면
    // 화면에는 항상 꺼진 상태로만 보여주고, 이 값은 권한이 다시 켜졌을 때 그대로 복원하기 위해 따로 들고 있는다.
    private boolean lastD7Enabled;
    private boolean lastD3Enabled;
    private boolean lastD1Enabled;

    // 거부 직후 shouldShowRequestPermissionRationale()을 확인하면 소프트 거부(true, 시스템이
    // 다음에도 다이얼로그를 다시 띄워줌)와 영구 거부(false, 이제 설정으로 가야만 함)를 정확히
    // 구분할 수 있다(방금 막 요청을 했기 때문에 "한 번도 안 물어본 상태"와 헷갈릴 일이 없음).
    // 소프트 거부라면 시스템 다이얼로그 자체가 이미 사용자의 선택을 받은 것이므로 별도 안내
    // 없이 그대로 두고, 영구 거부일 때만 설정 이동 확인창을 띄운다.
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                applySwitchStates();
                boolean permanentlyDenied = !granted && !ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.POST_NOTIFICATIONS);
                if (permanentlyDenied) {
                    showPermissionSettingsDialog();
                }
            });

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

        binding.notifSwitchMaster.setOnCheckedChangeListener((button, checked) -> {
            if (!applyingRemoteState) {
                onMasterSwitchToggledByUser(checked);
            }
        });
        binding.notifSwitchD7.setOnCheckedChangeListener((button, checked) -> {
            if (!applyingRemoteState) {
                lastD7Enabled = checked;
                presenter.updateD7(checked);
                applySwitchStates();
                if (checked) {
                    requestNotificationPermissionIfNeeded();
                }
            }
        });
        binding.notifSwitchD3.setOnCheckedChangeListener((button, checked) -> {
            if (!applyingRemoteState) {
                lastD3Enabled = checked;
                presenter.updateD3(checked);
                applySwitchStates();
                if (checked) {
                    requestNotificationPermissionIfNeeded();
                }
            }
        });
        binding.notifSwitchD1.setOnCheckedChangeListener((button, checked) -> {
            if (!applyingRemoteState) {
                lastD1Enabled = checked;
                presenter.updateD1(checked);
                applySwitchStates();
                if (checked) {
                    requestNotificationPermissionIfNeeded();
                }
            }
        });

        presenter.loadSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 시스템 설정 화면에서 권한을 바꾸고 돌아왔을 수 있으니 매번 실제 상태로 다시 동기화한다
        applySwitchStates();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        binding = null;
        super.onDestroy();
    }

    // 사용자가 알림 받기 스위치를 직접 조작했을 때만 호출된다. D-7/D-3/D-1을 함께
    // 켜고 끄는 건 항상 앱 안에서 즉시 처리하고, 켜는 방향일 때만 필요하면 권한을 요청한다
    // (끄는 건 앱 자체 설정이라 시스템 설정으로 보낼 이유가 없다).
    private void onMasterSwitchToggledByUser(boolean turningOn) {
        lastD7Enabled = turningOn;
        lastD3Enabled = turningOn;
        lastD1Enabled = turningOn;
        presenter.updateMaster(turningOn);
        applySwitchStates();

        if (turningOn) {
            requestNotificationPermissionIfNeeded();
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        boolean needsRuntimeRequest = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED;
        if (needsRuntimeRequest) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        // 런타임 권한은 있는데도 알림이 꺼져 있는 경우(권한 영구 거부, 채널 차단, 구버전
        // 시스템 알림 OFF 등)는 앱이 직접 켤 수 없으니 확인창을 띄운 뒤 시스템 알림 설정으로 보낸다
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            showPermissionSettingsDialog();
        }
    }

    private void showPermissionSettingsDialog() {
        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Bag_ConfirmDialog)
                .setTitle(R.string.notif_permission_dialog_title)
                .setMessage(R.string.notif_permission_dialog_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.notif_permission_dialog_confirm,
                        (dialog, which) -> openAppNotificationSettings())
                .show();
    }

    private void openAppNotificationSettings() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
        }
        startActivity(intent);
    }

    // 기기 알림 권한이 꺼져 있으면 저장된 설정값(lastD7/D3/D1Enabled)과 무관하게 네 스위치를
    // 전부 꺼진 상태로 보여준다 — 권한이 없으면 어차피 알림이 안 오니 화면도 그 사실을 그대로
    // 반영한다. 권한이 다시 켜지면 저장해둔 값으로 원래 상태를 복원한다. 스위치 자체는 계속
    // 조작 가능하게 두고(setEnabled로 막지 않음), 켤 때만 필요하면 권한 요청이 트리거된다.
    private void applySwitchStates() {
        if (binding == null) {
            return;
        }
        boolean granted = NotificationManagerCompat.from(this).areNotificationsEnabled();
        applyingRemoteState = true;
        binding.notifSwitchD7.setChecked(granted && lastD7Enabled);
        binding.notifSwitchD3.setChecked(granted && lastD3Enabled);
        binding.notifSwitchD1.setChecked(granted && lastD1Enabled);
        binding.notifSwitchMaster.setChecked(granted && (lastD7Enabled || lastD3Enabled || lastD1Enabled));
        applyingRemoteState = false;
        binding.notifMasterHint.setVisibility(granted ? View.GONE : View.VISIBLE);
    }

    // ===== NotificationSettingsContract.View =====

    @Override
    public void showSettings(NotificationSettings settings) {
        if (binding == null) {
            return;
        }
        lastD7Enabled = settings.isDDay7Enabled();
        lastD3Enabled = settings.isDDay3Enabled();
        lastD1Enabled = settings.isDDay1Enabled();
        applySwitchStates();
        revealSwitches();
    }

    @Override
    public void showError(String message) {
        if (isFinishing()) {
            return;
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (binding != null) {
            revealSwitches();
        }
    }

    // 스위치는 XML에서 visibility="gone" 상태로 시작한다. gone인 동안에는
    // SwitchCompat이 한 번도 레이아웃되지 않은 상태(isLaidOut() == false)라
    // setChecked()가 애니메이션 없이 즉시 썸 위치를 반영하고, 그 다음에야
    // visible로 전환하기 때문에 서버 값이 도착할 때 스위치가 움직이는 게
    // 화면에 보이지 않는다.
    private void revealSwitches() {
        binding.notifSwitchMaster.setVisibility(View.VISIBLE);
        binding.notifSwitchD7.setVisibility(View.VISIBLE);
        binding.notifSwitchD3.setVisibility(View.VISIBLE);
        binding.notifSwitchD1.setVisibility(View.VISIBLE);
    }
}
