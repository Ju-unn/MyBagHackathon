package com.example.mybaghackathon.ui.profile;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.local.UserStorage;
import com.example.mybaghackathon.data.repository.AuthRepository;
import com.example.mybaghackathon.data.repository.DefaultItemRepository;
import com.example.mybaghackathon.model.User;
import com.example.mybaghackathon.model.UserDefaultItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// ProfileContract.Presenter 구현체 — defaultItemRepository/authRepository 호출을
// 백그라운드 스레드에서 실행하고 결과를 메인 스레드의 View로 전달한다. 사용자 정보는
// UserStorage(로컬 저장소) 조회라 네트워크 스레드 없이 바로 읽는다.
public class ProfilePresenter implements ProfileContract.Presenter {

    private static final int PREVIEW_COUNT = 4;

    private final ProfileContract.View view;
    private final DefaultItemRepository defaultItemRepository;
    private final AuthRepository authRepository;
    private final UserStorage userStorage;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean destroyed = false;

    public ProfilePresenter(ProfileContract.View view, DefaultItemRepository defaultItemRepository,
                             AuthRepository authRepository, UserStorage userStorage) {
        this.view = view;
        this.defaultItemRepository = defaultItemRepository;
        this.authRepository = authRepository;
        this.userStorage = userStorage;
    }

    @Override
    public void loadItems() {
        User user = userStorage.getUser();
        view.showUser(user);

        executor.execute(() -> {
            AppResult<List<UserDefaultItem>> result = defaultItemRepository.listItems();
            postToView(() -> {
                if (result.isSuccess()) {
                    List<UserDefaultItem> items = result.getData();
                    int previewCount = Math.min(PREVIEW_COUNT, items.size());
                    view.showItemPreview(items.subList(0, previewCount), items.size());
                } else {
                    view.showError(result.getError().getMessage());
                }
            });
        });
    }

    @Override
    public void renameItem(long defaultItemId, String newLabel) {
        executor.execute(() -> {
            AppResult<Void> result = defaultItemRepository.updateItem(defaultItemId, newLabel, null, null);
            postToView(() -> {
                if (result.isSuccess()) {
                    loadItems();
                } else {
                    view.showError(result.getError().getMessage());
                }
            });
        });
    }

    @Override
    public void deleteItem(long defaultItemId) {
        executor.execute(() -> {
            AppResult<Void> result = defaultItemRepository.deleteItem(defaultItemId);
            postToView(() -> {
                if (result.isSuccess()) {
                    loadItems();
                } else {
                    view.showError(result.getError().getMessage());
                }
            });
        });
    }

    @Override
    public void logout() {
        executor.execute(() -> {
            AppResult<Void> result = authRepository.logout();
            postToView(() -> {
                if (result.isSuccess()) {
                    view.navigateToLogin();
                } else {
                    view.showError(result.getError().getMessage());
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        executor.shutdown();
    }

    private void postToView(Runnable action) {
        mainHandler.post(() -> {
            if (destroyed) return;
            action.run();
        });
    }
}
