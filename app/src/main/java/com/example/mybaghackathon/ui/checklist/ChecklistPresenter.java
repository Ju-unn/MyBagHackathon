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
    private int expectedMemberCount = 1;
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
        runRepositoryAction(() -> unassignOrRemoveRow(item, userId));
    }

    // 서버의 단일 담당자 변경(assign.php → ChecklistRepository::assign)은 완료 상태를 건드리지
    // 않는다(다중 배정 동기화의 reassignForSync만 초기화함) — 그래서 체크된 물품의 담당자를
    // 바꾸거나 해제해도 is_completed가 그대로 남아 진행률이 안 줄어드는 문제가 있었다.
    // 담당자가 실제로 바뀌는데 이미 체크돼 있으면, assign 전에 먼저 체크를 해제한다.
    private AppResult<?> assignAndResetCompletionIfNeeded(PackingItem item, Long newAssigneeUserId) {
        boolean assigneeChanging = !java.util.Objects.equals(item.getAssigneeUserId(), newAssigneeUserId);
        if (assigneeChanging && item.isCompleted()) {
            AppResult<Boolean> toggleResult = packingRepository.toggleCheck(item.getPackingItemId());
            if (!toggleResult.isSuccess()) {
                return toggleResult;
            }
        }
        return packingRepository.assign(item.getPackingItemId(), newAssigneeUserId);
    }

    // 다중 배정된 물품(item_group_id를 공유하는 row가 2개 이상)에서 담당자 한 명만 담당
    // 해제하는 경우는 assign(null)로 "미지정" row를 남기면 안 된다 — 그 row는 그 사람 몫으로
    // 복제된 것이라, 방장이 공용 리스트에서 인원을 뺄 때(assignMultiple) row 자체가 삭제되는 것과
    // 똑같이 이 row도 통째로 지워야 진행률 총 개수가 맞다. 단독 배정 물품은 기존처럼 "미지정"으로
    // 남겨 방장이 다시 배정할 수 있게 한다.
    private AppResult<?> unassignOrRemoveRow(PackingItem item, Long newAssigneeUserId) {
        if (newAssigneeUserId == null && isMultiAssigneeGroup(item)) {
            return packingRepository.deleteItem(item.getPackingItemId());
        }
        return assignAndResetCompletionIfNeeded(item, newAssigneeUserId);
    }

    private boolean isMultiAssigneeGroup(PackingItem item) {
        long groupId = item.getItemGroupId();
        if (groupId <= 0L) {
            return false;
        }
        int count = 0;
        for (PackingItem candidate : items) {
            if (candidate != null && candidate.getItemGroupId() == groupId) {
                count++;
                if (count > 1) {
                    return true;
                }
            }
        }
        return false;
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
    public void deleteGroup(List<PackingItem> group) {
        if (group == null || group.isEmpty()) {
            return;
        }
        for (PackingItem item : group) {
            if (item == null || !canDelete(item)) {
                view.showError(permissionMessage(group.get(0)));
                return;
            }
        }
        deleteGroupOnServer(group);
    }

    // 공용 담당 해제와 개인 기본 물품 원본 삭제를 한 번에 처리 — 담당 해제 후 개인 물품이
    // 다시 단독으로 내 목록에 재등장하지 않도록 순서대로 서버에 반영한다.
    @Override
    public void removeMergedItem(PackingItem personalItem, PackingItem commonItem) {
        if (personalItem == null || commonItem == null) {
            return;
        }
        if (!ChecklistItemVisibility.isAssignedCommon(commonItem, currentUserId)
                || !ChecklistItemVisibility.isOwnedPersonal(personalItem, currentUserId)) {
            view.showError(permissionMessage(commonItem));
            return;
        }
        runRepositoryAction(() -> {
            AppResult<?> unassignResult = unassignOrRemoveRow(commonItem, null);
            if (!unassignResult.isSuccess()) {
                return unassignResult;
            }
            return packingRepository.deleteItem(personalItem.getPackingItemId());
        });
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
        Integer expected = trip.getExpectedMemberCount();
        expectedMemberCount = expected == null || expected <= 0
                ? Math.max(1, loadedMembers.size())
                : expected;
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

    // group 안의 row를 순서대로 삭제 요청하되, 전부 같은 loading 사이클(같은 executor 작업)
    // 안에서 처리해 loading 가드에 막혀 첫 row 이후가 조용히 무시되는 일이 없게 한다.
    private void deleteGroupOnServer(List<PackingItem> group) {
        if (destroyed || loading) {
            return;
        }
        startLoading();
        executor.execute(() -> {
            try {
                for (PackingItem item : group) {
                    AppResult<Void> result = packingRepository.deleteItem(item.getPackingItemId());
                    if (!result.isSuccess()) {
                        post(() -> view.showRetryableError(
                                messageOf(result, "항목을 삭제하지 못했습니다.")));
                        break;
                    }
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
        view.showChecklist(
                tripName,
                new ArrayList<>(items),
                new ArrayList<>(members),
                expectedMemberCount,
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
