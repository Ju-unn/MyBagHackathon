package com.example.mybaghackathon.ui.settings;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.NotificationSettingsRepository;
import com.example.mybaghackathon.model.NotificationSettings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// NotificationSettingsContract.Presenter 구현체 — notificationSettingsRepository 호출을
// 백그라운드 스레드에서 실행하고 결과를 메인 스레드의 View로 전달한다.
public class NotificationSettingsPresenter implements NotificationSettingsContract.Presenter {

    private final NotificationSettingsContract.View view;
    private final NotificationSettingsRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean destroyed;

    public NotificationSettingsPresenter(NotificationSettingsContract.View view,
                                          NotificationSettingsRepository repository) {
        this.view = view;
        this.repository = repository;
    }

    @Override
    public void loadSettings() {
        executor.execute(() -> {
            AppResult<NotificationSettings> result = repository.getSettings();
            postToView(() -> {
                if (result.isSuccess()) {
                    view.showSettings(result.getData());
                } else {
                    view.showError(result.getError().getMessage());
                }
            });
        });
    }

    @Override
    public void updateD7(boolean enabled) {
        update(enabled, null, null);
    }

    @Override
    public void updateD3(boolean enabled) {
        update(null, enabled, null);
    }

    @Override
    public void updateD1(boolean enabled) {
        update(null, null, enabled);
    }

    private void update(Boolean d7Enabled, Boolean d3Enabled, Boolean d1Enabled) {
        executor.execute(() -> {
            AppResult<Void> result = repository.updateSettings(
                    d7Enabled, d3Enabled, d1Enabled, null, null, null);
            if (!result.isSuccess()) {
                postToView(() -> view.showError(result.getError().getMessage()));
            }
        });
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        executor.shutdownNow();
    }

    private void postToView(Runnable action) {
        mainHandler.post(() -> {
            if (destroyed) {
                return;
            }
            action.run();
        });
    }
}
