package com.example.mybaghackathon.ui.checklist;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.PackingRepository;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.TripMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 체크리스트 조회와 CRUD, 담당 지정 및 삭제 실행 취소 상태를 관리한다. */
public class ChecklistPresenter implements ChecklistContract.Presenter {

    private final ChecklistContract.View view;
    private final TripRepository tripRepository;
    private final PackingRepository packingRepository;
    private final long currentUserId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<PackingItem> items = new ArrayList<>();
    private final List<TripMember> members = new ArrayList<>();

    private volatile boolean destroyed;
    private boolean loading;
    private long tripId = -1L;
    private boolean isHost;
    private String tripName = "여행 체크리스트";
    private PendingDelete pendingDelete;

    public ChecklistPresenter(
            ChecklistContract.View view,
            TripRepository tripRepository,
            PackingRepository packingRepository,
            long currentUserId
    ) {
        this.view = view;
        this.tripRepository = tripRepository;
        this.packingRepository = packingRepository;
        this.currentUserId = currentUserId;
    }

    @Override
    public void loadChecklist(long tripId, boolean initialHost) {
        this.tripId = tripId;
        this.isHost = initialHost;
        refreshChecklist();
    }

    @Override
    public void refreshChecklist() {
        if (destroyed || loading) {
            return;
        }
        if (tripId <= 0L) {
            view.showError("여행방 정보가 없어 체크리스트를 불러올 수 없습니다.");
            return;
        }

        loading = true;
        executor.execute(this::loadChecklistOnWorker);
    }

    @Override
    public void addItem(String name, int priorityLevel, String scope) {
        if (!hasText(name)) {
            view.showError("추가할 항목 이름을 입력해주세요.");
            return;
        }
        runRepositoryAction(() -> packingRepository.addItem(
                tripId, name.trim(), null, priorityCode(priorityLevel), scope));
    }

    @Override
    public void updateItem(PackingItem item, String name, int priorityLevel) {
        if (item == null || !hasText(name)) {
            view.showError("수정할 항목 이름을 확인해주세요.");
            return;
        }
        runRepositoryAction(() -> packingRepository.updateItem(
                item.getPackingItemId(), name.trim(), null, priorityCode(priorityLevel), null));
    }

    @Override
    public void toggleItem(PackingItem item) {
        if (item != null) {
            runRepositoryAction(() -> packingRepository.toggleCheck(item.getPackingItemId()));
        }
    }

    @Override
    public void assignItem(PackingItem item, Long userId) {
        if (item != null) {
            runRepositoryAction(() -> packingRepository.assign(item.getPackingItemId(), userId));
        }
    }

    @Override
    public void requestDelete(PackingItem item) {
        if (destroyed || item == null) {
            return;
        }

        if (pendingDelete != null) {
            PendingDelete previous = pendingDelete;
            pendingDelete = null;
            deleteOnServer(previous.item);
        }

        int originalIndex = items.indexOf(item);
        if (originalIndex < 0) {
            return;
        }
        items.remove(originalIndex);
        pendingDelete = new PendingDelete(item, originalIndex);
        publishChecklist();
        view.showDeleteUndo(item);
    }

    @Override
    public void undoDelete(PackingItem item) {
        if (!matchesPending(item)) {
            return;
        }
        PendingDelete pending = pendingDelete;
        pendingDelete = null;
        items.add(Math.min(pending.originalIndex, items.size()), pending.item);
        publishChecklist();
    }

    @Override
    public void confirmDelete(PackingItem item) {
        if (!matchesPending(item)) {
            return;
        }
        PendingDelete pending = pendingDelete;
        pendingDelete = null;
        deleteOnServer(pending.item);
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        mainHandler.removeCallbacksAndMessages(null);
        executor.shutdownNow();
    }

    private void loadChecklistOnWorker() {
        try {
            AppResult<Trip> tripResult = tripRepository.getTripDetail(tripId);
            if (!tripResult.isSuccess() || tripResult.getData() == null) {
                postLoadFailure(messageOf(tripResult, "여행방 정보를 불러오지 못했습니다."));
                return;
            }

            AppResult<List<PackingItem>> itemResult = packingRepository.listItems(tripId, null);
            if (!itemResult.isSuccess()) {
                postLoadFailure(messageOf(itemResult, "체크리스트를 불러오지 못했습니다."));
                return;
            }

            Trip trip = tripResult.getData();
            List<TripMember> loadedMembers = trip.getMembers() == null
                    ? Collections.emptyList()
                    : new ArrayList<>(trip.getMembers());
            List<PackingItem> loadedItems = activeItems(itemResult.getData());
            post(() -> applyLoadedData(trip, loadedMembers, loadedItems));
        } catch (RuntimeException error) {
            postLoadFailure("서버 응답을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private void applyLoadedData(
            Trip trip,
            List<TripMember> loadedMembers,
            List<PackingItem> loadedItems
    ) {
        loading = false;
        if (hasText(trip.getTripName())) {
            tripName = trip.getTripName().trim();
        }
        members.clear();
        members.addAll(loadedMembers);
        items.clear();
        items.addAll(loadedItems);
        pendingDelete = null;
        if (currentUserId > 0L) {
            isHost = trip.getOwnerUserId() == currentUserId;
        }
        publishChecklist();
    }

    private void runRepositoryAction(RepositoryAction action) {
        if (destroyed) {
            return;
        }
        if (tripId <= 0L) {
            view.showError("여행방 정보가 없습니다.");
            return;
        }
        executor.execute(() -> {
            try {
                AppResult<?> result = action.run();
                if (!result.isSuccess()) {
                    postError(messageOf(result, "요청을 처리하지 못했습니다."));
                    return;
                }
                loadChecklistOnWorker();
            } catch (RuntimeException error) {
                postError("서버 응답을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.");
            }
        });
    }

    private void deleteOnServer(PackingItem item) {
        if (destroyed) {
            return;
        }
        executor.execute(() -> {
            try {
                AppResult<Void> result = packingRepository.deleteItem(item.getPackingItemId());
                if (!result.isSuccess()) {
                    postError(messageOf(result, "항목을 삭제하지 못했습니다."));
                }
                loadChecklistOnWorker();
            } catch (RuntimeException error) {
                postError("서버 응답을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.");
                loadChecklistOnWorker();
            }
        });
    }

    private void publishChecklist() {
        int memberCount = Math.max(1, members.size());
        view.showChecklist(
                tripName,
                new ArrayList<>(items),
                new ArrayList<>(members),
                memberCount,
                isHost
        );
    }

    private void postLoadFailure(String message) {
        post(() -> {
            loading = false;
            view.showError(message);
        });
    }

    private void postError(String message) {
        post(() -> view.showError(message));
    }

    private void post(Runnable action) {
        mainHandler.post(() -> {
            if (!destroyed) {
                action.run();
            }
        });
    }

    private boolean matchesPending(PackingItem item) {
        return pendingDelete != null
                && item != null
                && pendingDelete.item.getPackingItemId() == item.getPackingItemId();
    }

    private List<PackingItem> activeItems(List<PackingItem> source) {
        List<PackingItem> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (PackingItem item : source) {
            if (!"DELETED".equalsIgnoreCase(item.getItemStatus())
                    && !"EXCLUDED".equalsIgnoreCase(item.getItemStatus())) {
                result.add(item);
            }
        }
        return result;
    }

    private String priorityCode(int priorityLevel) {
        switch (priorityLevel) {
            case 1:
                return "RECOMMENDED";
            case 2:
                return "OPTIONAL";
            default:
                return "REQUIRED";
        }
    }

    private String messageOf(AppResult<?> result, String fallback) {
        return result.getError() == null || !hasText(result.getError().getMessage())
                ? fallback
                : result.getError().getMessage();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private interface RepositoryAction {
        AppResult<?> run();
    }

    private static final class PendingDelete {
        private final PackingItem item;
        private final int originalIndex;

        private PendingDelete(PackingItem item, int originalIndex) {
            this.item = item;
            this.originalIndex = originalIndex;
        }
    }
}
