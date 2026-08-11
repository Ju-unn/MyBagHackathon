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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 체크리스트 조회와 CRUD, 담당 지정 및 삭제 실행 취소 상태를 관리한다. */
public class ChecklistPresenter implements ChecklistContract.Presenter {

    private final ChecklistContract.View view;
    private final TripRepository tripRepository;
    private final PackingRepository packingRepository;
    private final long currentUserId;
    private final ExecutorService executor;
    private final UiDispatcher uiDispatcher;
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
        this(view, tripRepository, packingRepository, currentUserId,
                Executors.newSingleThreadExecutor(), new AndroidUiDispatcher());
    }

    ChecklistPresenter(
            ChecklistContract.View view,
            TripRepository tripRepository,
            PackingRepository packingRepository,
            long currentUserId,
            ExecutorService executor,
            UiDispatcher uiDispatcher
    ) {
        this.view = view;
        this.tripRepository = tripRepository;
        this.packingRepository = packingRepository;
        this.currentUserId = currentUserId;
        this.executor = executor;
        this.uiDispatcher = uiDispatcher;
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

        startLoading();
        executor.execute(this::loadChecklistOnWorker);
    }

    @Override
    public void addItem(String name, int priorityLevel, String scope) {
        if (!hasText(name)) {
            view.showError("추가할 항목 이름을 입력해주세요.");
            return;
        }
        if ("COMMON".equalsIgnoreCase(scope) && !isHost) {
            view.showError("공용 물품은 방장만 추가할 수 있습니다.");
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
        if (!canEdit(item)) {
            view.showError(permissionMessage(item));
            return;
        }
        runRepositoryAction(() -> packingRepository.updateItem(
                item.getPackingItemId(), name.trim(), null, priorityCode(priorityLevel), null));
    }

    @Override
    public void toggleItem(PackingItem item) {
        if (item == null) {
            return;
        }
        boolean canToggle = ChecklistItemVisibility.isCommon(item)
                ? isHost || ChecklistItemVisibility.isAssignedCommon(item, currentUserId)
                : ChecklistItemVisibility.isOwnedPersonal(item, currentUserId);
        if (!canToggle) {
            view.showError(permissionMessage(item));
            return;
        }
        runRepositoryAction(() -> packingRepository.toggleCheck(item.getPackingItemId()));
    }

    @Override
    public void assignItem(PackingItem item, Long userId) {
        if (item == null || !ChecklistItemVisibility.isCommon(item)) {
            return;
        }
        boolean assigning = userId != null;
        boolean assignedToCurrentUser = ChecklistItemVisibility.isAssignedCommon(item, currentUserId);
        if ((assigning && !isHost) || (!assigning && !isHost && !assignedToCurrentUser)) {
            view.showError("공용 물품의 담당자는 방장 또는 현재 담당자만 변경할 수 있습니다.");
            return;
        }
        runRepositoryAction(() -> packingRepository.assign(item.getPackingItemId(), userId));
    }

    @Override
    public void assignItems(PackingItem item, List<Long> userIds) {
        if (item == null || !ChecklistItemVisibility.isCommon(item)) {
            return;
        }
        if (!isHost) {
            view.showError("공용 물품을 여러 명에게 배정하는 건 방장만 할 수 있습니다.");
            return;
        }
        List<Long> desiredIds = normalizedUserIds(userIds);
        if (desiredIds.isEmpty()) {
            view.showError("배정할 멤버를 선택해주세요.");
            return;
        }
        runRepositoryAction(() ->
                packingRepository.assignMultiple(item.getItemGroupId(), desiredIds));
    }

    // 서버 assign.php가 그룹(원본+복제 row)을 최종 목록으로 트랜잭션 동기화하므로 호출 한 번이면 된다
    @Override
    public void reassignGroup(List<PackingItem> groupItems, List<Long> userIds) {
        if (groupItems == null || groupItems.isEmpty()) {
            return;
        }
        if (!isHost) {
            view.showError("공용 물품을 여러 명에게 배정하는 건 방장만 할 수 있습니다.");
            return;
        }
        if (!isConsistentCommonGroup(groupItems)) {
            view.showError("물품 그룹 정보가 올바르지 않습니다. 목록을 새로고침해주세요.");
            return;
        }
        List<Long> desiredIds = normalizedUserIds(userIds);
        long groupId = groupItems.get(0).getItemGroupId();
        runRepositoryAction(() -> packingRepository.assignMultiple(groupId, desiredIds));
    }

    private List<Long> normalizedUserIds(List<Long> userIds) {
        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>();
        if (userIds != null) {
            for (Long userId : userIds) {
                if (userId != null && userId > 0L) {
                    uniqueIds.add(userId);
                }
            }
        }
        return new ArrayList<>(uniqueIds);
    }

    private boolean isConsistentCommonGroup(List<PackingItem> groupItems) {
        long groupId = groupItems.get(0).getItemGroupId();
        if (groupId <= 0L) {
            return false;
        }
        for (PackingItem item : groupItems) {
            if (item == null
                    || !ChecklistItemVisibility.isCommon(item)
                    || item.getItemGroupId() != groupId) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void requestDelete(PackingItem item) {
        if (destroyed || item == null || loading) {
            return;
        }
        if (!canDelete(item)) {
            view.showError(permissionMessage(item));
            return;
        }

        if (pendingDelete != null) {
            view.showError("이전 삭제의 실행 취소 안내가 끝난 후 다시 시도해주세요.");
            return;
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
    public void deleteItem(PackingItem item) {
        if (item == null) {
            return;
        }
        if (!canDelete(item)) {
            view.showError(permissionMessage(item));
            return;
        }
        deleteOnServer(item);
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        uiDispatcher.clear();
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
        finishLoading();
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
        if (destroyed || loading) {
            return;
        }
        if (tripId <= 0L) {
            view.showError("여행방 정보가 없습니다.");
            return;
        }
        startLoading();
        executor.execute(() -> {
            try {
                AppResult<?> result = action.run();
                if (!result.isSuccess()) {
                    postLoadFailure(messageOf(result, "요청을 처리하지 못했습니다."));
                    return;
                }
                loadChecklistOnWorker();
            } catch (RuntimeException error) {
                postLoadFailure("서버 응답을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.");
            }
        });
    }

    private void deleteOnServer(PackingItem item) {
        if (destroyed || loading) {
            return;
        }
        startLoading();
        executor.execute(() -> {
            try {
                AppResult<Void> result = packingRepository.deleteItem(item.getPackingItemId());
                if (!result.isSuccess()) {
                    post(() -> view.showRetryableError(
                            messageOf(result, "항목을 삭제하지 못했습니다.")));
                }
                loadChecklistOnWorker();
            } catch (RuntimeException error) {
                post(() -> view.showRetryableError(
                        "서버 응답을 처리하지 못했습니다. 잠시 후 다시 시도해주세요."));
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
            finishLoading();
            view.showRetryableError(message);
        });
    }

    private void startLoading() {
        loading = true;
        view.showLoading(true);
    }

    private void finishLoading() {
        loading = false;
        view.showLoading(false);
    }

    private void post(Runnable action) {
        uiDispatcher.post(() -> {
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
                    && !"EXCLUDED".equalsIgnoreCase(item.getItemStatus())
                    && !ChecklistItemSelection.isUnselectedRecommendation(item)) {
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

    private boolean canEdit(PackingItem item) {
        return ChecklistItemVisibility.isCommon(item)
                ? isHost
                : ChecklistItemVisibility.isOwnedPersonal(item, currentUserId);
    }

    private boolean canDelete(PackingItem item) {
        return canEdit(item);
    }

    private String permissionMessage(PackingItem item) {
        return ChecklistItemVisibility.isCommon(item)
                ? "공용 물품은 방장만 변경할 수 있습니다."
                : "내 개인 물품만 변경할 수 있습니다.";
    }

    private interface RepositoryAction {
        AppResult<?> run();
    }

    interface UiDispatcher {
        void post(Runnable action);

        void clear();
    }

    private static final class AndroidUiDispatcher implements UiDispatcher {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void post(Runnable action) {
            handler.post(action);
        }

        @Override
        public void clear() {
            handler.removeCallbacksAndMessages(null);
        }
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
