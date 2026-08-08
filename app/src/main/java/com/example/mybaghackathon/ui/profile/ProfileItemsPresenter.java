package com.example.mybaghackathon.ui.profile;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.DefaultItemRepository;
import com.example.mybaghackathon.model.UserDefaultItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// ProfileItemsContract.Presenter 구현체 — defaultItemRepository 호출을 백그라운드
// 스레드에서 실행하고 결과를 메인 스레드의 View로 전달한다
public class ProfileItemsPresenter implements ProfileItemsContract.Presenter {

    private final ProfileItemsContract.View view;
    private final DefaultItemRepository defaultItemRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean destroyed = false;

    public ProfileItemsPresenter(ProfileItemsContract.View view, DefaultItemRepository defaultItemRepository) {
        this.view = view;
        this.defaultItemRepository = defaultItemRepository;
    }

    @Override
    public void loadItems() {
        executor.execute(() -> {
            AppResult<List<UserDefaultItem>> result = defaultItemRepository.listItems();
            postToView(() -> {
                if (result.isSuccess()) {
                    view.showItems(result.getData());
                } else {
                    view.showError(result.getError().getMessage());
                }
            });
        });
    }

    @Override
    public void addItem(String itemName, int priorityLevel) {
        executor.execute(() -> {
            AppResult<Long> result =
                    defaultItemRepository.addItem(itemName, null, PriorityLevels.toApiValue(priorityLevel));
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
    public void renameItem(long defaultItemId, String newLabel, int priorityLevel) {
        executor.execute(() -> {
            AppResult<Void> result = defaultItemRepository.updateItem(
                    defaultItemId, newLabel, null, PriorityLevels.toApiValue(priorityLevel));
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
