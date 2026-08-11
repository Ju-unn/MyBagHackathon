package com.example.mybaghackathon.ui.checklist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.PackingRepository;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.TripInvite;
import com.example.mybaghackathon.model.TripMember;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public class ChecklistPresenterTest {

    private static final long TRIP_ID = 3L;
    private static final long CURRENT_USER_ID = 7L;

    @Test
    public void loadChecklist_publishesItemsMembersAndHostState() {
        Fixture fixture = new Fixture();

        fixture.presenter.loadChecklist(TRIP_ID, false);
        assertTrue(fixture.view.loading);
        fixture.executor.runNext();

        assertFalse(fixture.view.loading);
        assertEquals("테스트 여행", fixture.view.tripName);
        assertEquals(1, fixture.view.items.size());
        assertEquals(1, fixture.view.members.size());
        assertTrue(fixture.view.host);
    }

    @Test
    public void loadChecklist_excludesUncheckedAiPersonalRecommendation() {
        Fixture fixture = new Fixture();
        fixture.packingRepository.items.clear();

        PackingItem checked = fixture.item(31L, "AI", "COMMON");
        PackingItem unchecked = fixture.item(32L, "AI", "PERSONAL");
        PackingItem manual = fixture.item(33L, "USER", "PERSONAL");
        fixture.packingRepository.items.add(checked);
        fixture.packingRepository.items.add(unchecked);
        fixture.packingRepository.items.add(manual);

        fixture.loadInitialData();

        assertEquals(2, fixture.view.items.size());
        assertTrue(fixture.view.items.contains(checked));
        assertTrue(fixture.view.items.contains(manual));
        assertFalse(fixture.view.items.contains(unchecked));
    }

    @Test
    public void loadChecklist_oneCheckedOutOfFifteenPublishesOnlyOneRecommendation() {
        Fixture fixture = new Fixture();
        fixture.packingRepository.items.clear();
        PackingItem checked = fixture.item(100L, "AI", "COMMON");
        fixture.packingRepository.items.add(checked);
        for (int index = 1; index < 15; index++) {
            fixture.packingRepository.items.add(
                    fixture.item(100L + index, "AI", "PERSONAL"));
        }

        fixture.loadInitialData();

        assertEquals(1, fixture.view.items.size());
        assertTrue(fixture.view.items.contains(checked));
    }

    @Test
    public void duplicateAddWhileLoading_isIgnored() {
        Fixture fixture = new Fixture();
        fixture.loadInitialData();

        fixture.presenter.addItem("우산", 2, "PERSONAL");
        fixture.presenter.addItem("충전기", 0, "PERSONAL");

        assertEquals(1, fixture.executor.size());
        fixture.executor.runNext();
        assertEquals(1, fixture.packingRepository.addCalls);
        assertFalse(fixture.view.loading);
    }

    @Test
    public void deleteThenUndo_restoresItemWithoutServerCall() {
        Fixture fixture = new Fixture();
        fixture.loadInitialData();
        PackingItem item = fixture.view.items.get(0);

        fixture.presenter.requestDelete(item);
        assertEquals(0, fixture.view.items.size());
        assertTrue(fixture.view.deleteUndoShown);

        fixture.presenter.undoDelete(item);
        assertEquals(1, fixture.view.items.size());
        assertEquals(0, fixture.packingRepository.deleteCalls);
    }

    @Test
    public void nonHostCannotDeleteCommonItem() {
        Fixture fixture = new Fixture(false);
        PackingItem common = fixture.replaceWithCommonItem(CURRENT_USER_ID);
        fixture.loadInitialData();

        fixture.presenter.requestDelete(common);

        assertEquals(1, fixture.view.items.size());
        assertFalse(fixture.view.deleteUndoShown);
        assertTrue(fixture.view.errorShown);
    }

    @Test
    public void assignedUserCanUnassignCommonItem() {
        Fixture fixture = new Fixture(false);
        PackingItem common = fixture.replaceWithCommonItem(CURRENT_USER_ID);
        fixture.loadInitialData();

        fixture.presenter.assignItem(common, null);
        fixture.executor.runNext();

        assertEquals(1, fixture.packingRepository.assignCalls);
        assertEquals(null, fixture.packingRepository.lastAssigneeUserId);
    }

    @Test
    public void hostCanAssignCommonItemToMultipleMembers() {
        Fixture fixture = new Fixture(true);
        PackingItem common = fixture.replaceWithCommonItem(CURRENT_USER_ID);
        common.setItemGroupId(500L);
        fixture.loadInitialData();

        fixture.presenter.assignItems(
                common,
                java.util.Arrays.asList(CURRENT_USER_ID, 8L, 8L, null, -1L, 9L));
        fixture.executor.runNext();

        assertEquals(1, fixture.packingRepository.assignMultipleCalls);
        assertEquals(500L, fixture.packingRepository.lastGroupId);
        assertEquals(java.util.Arrays.asList(CURRENT_USER_ID, 8L, 9L),
                fixture.packingRepository.lastAssigneeUserIds);
    }

    @Test
    public void hostDeletesCommonItemImmediately_withoutUndoState() {
        Fixture fixture = new Fixture(true);
        PackingItem common = fixture.replaceWithCommonItem(CURRENT_USER_ID);
        fixture.loadInitialData();

        fixture.presenter.deleteItem(common);
        fixture.executor.runNext();

        assertEquals(1, fixture.packingRepository.deleteCalls);
        assertFalse(fixture.view.deleteUndoShown);
    }

    @Test
    public void nonHostCannotDeleteCommonItemImmediately() {
        Fixture fixture = new Fixture(false);
        PackingItem common = fixture.replaceWithCommonItem(99L);
        fixture.loadInitialData();

        fixture.presenter.deleteItem(common);

        assertEquals(0, fixture.packingRepository.deleteCalls);
        assertTrue(fixture.view.errorShown);
    }

    @Test
    public void reassignGroup_sendsFinalListToServerInOneSyncCall() {
        Fixture fixture = new Fixture(true);
        fixture.loadInitialData();

        PackingItem staying = fixture.commonItem(101L, CURRENT_USER_ID);
        PackingItem leavingA = fixture.commonItem(102L, 8L);
        PackingItem leavingB = fixture.commonItem(103L, 9L);
        staying.setItemGroupId(500L);
        leavingA.setItemGroupId(500L);
        leavingB.setItemGroupId(500L);
        List<PackingItem> group = java.util.Arrays.asList(staying, leavingA, leavingB);

        fixture.presenter.reassignGroup(group, java.util.Arrays.asList(CURRENT_USER_ID, 10L));
        fixture.executor.runNext();

        // 서버 assign.php가 그룹 전체를 트랜잭션으로 동기화하므로 최종 목록 한 번만 보낸다
        // (row 재사용/복제/삭제는 전부 서버 책임 — 클라이언트는 delete를 호출하지 않음)
        assertEquals(1, fixture.packingRepository.assignMultipleCalls);
        assertEquals(500L, fixture.packingRepository.lastGroupId);
        assertEquals(java.util.Arrays.asList(CURRENT_USER_ID, 10L), fixture.packingRepository.lastAssigneeUserIds);
        assertEquals(0, fixture.packingRepository.deleteCalls);
    }

    @Test
    public void reassignGroup_emptySelection_syncsEmptyListToUnassignAll() {
        Fixture fixture = new Fixture(true);
        fixture.loadInitialData();

        PackingItem itemA = fixture.commonItem(101L, 8L);
        PackingItem itemB = fixture.commonItem(102L, 9L);
        itemA.setItemGroupId(500L);
        itemB.setItemGroupId(500L);
        List<PackingItem> group = java.util.Arrays.asList(itemA, itemB);

        fixture.presenter.reassignGroup(group, Collections.emptyList());
        fixture.executor.runNext();

        assertEquals(1, fixture.packingRepository.assignMultipleCalls);
        assertEquals(Collections.emptyList(), fixture.packingRepository.lastAssigneeUserIds);
        assertEquals(0, fixture.packingRepository.assignCalls);
        assertEquals(0, fixture.packingRepository.deleteCalls);
    }

    @Test
    public void reassignGroup_rejectsRowsFromDifferentServerGroups() {
        Fixture fixture = new Fixture(true);
        fixture.loadInitialData();

        PackingItem itemA = fixture.commonItem(101L, 8L);
        PackingItem itemB = fixture.commonItem(102L, 9L);
        itemA.setItemGroupId(500L);
        itemB.setItemGroupId(600L);

        fixture.presenter.reassignGroup(
                java.util.Arrays.asList(itemA, itemB),
                java.util.Arrays.asList(CURRENT_USER_ID, 10L));

        assertEquals(0, fixture.packingRepository.assignMultipleCalls);
        assertTrue(fixture.view.errorShown);
    }

    @Test
    public void nonHostCannotAssignCommonItemToMultipleMembers() {
        Fixture fixture = new Fixture(false);
        PackingItem common = fixture.replaceWithCommonItem(CURRENT_USER_ID);
        fixture.loadInitialData();

        fixture.presenter.assignItems(common, java.util.Arrays.asList(CURRENT_USER_ID, 8L));

        assertEquals(0, fixture.packingRepository.assignMultipleCalls);
        assertTrue(fixture.view.errorShown);
    }

    private static final class Fixture {
        private final RecordingView view = new RecordingView();
        private final FakeTripRepository tripRepository = new FakeTripRepository();
        private final FakePackingRepository packingRepository = new FakePackingRepository();
        private final QueuedExecutor executor = new QueuedExecutor();
        private final ChecklistPresenter presenter;

        private Fixture() {
            this(true);
        }

        private Fixture(boolean currentUserIsHost) {
            Trip trip = new Trip();
            trip.setTripId(TRIP_ID);
            trip.setOwnerUserId(currentUserIsHost ? CURRENT_USER_ID : 99L);
            trip.setTripName("테스트 여행");
            trip.setMembers(Collections.singletonList(
                    new TripMember(CURRENT_USER_ID, "여행자", null,
                            "OWNER", "ACTIVE", null)));
            tripRepository.trip = trip;

            PackingItem item = new PackingItem();
            item.setPackingItemId(11L);
            item.setTripId(TRIP_ID);
            item.setCreatedByUserId(CURRENT_USER_ID);
            item.setItemName("여권");
            item.setScope("PERSONAL");
            item.setAssigneeUserId(CURRENT_USER_ID);
            item.setItemStatus("ACTIVE");
            packingRepository.items.add(item);

            presenter = new ChecklistPresenter(
                    view,
                    tripRepository,
                    packingRepository,
                    CURRENT_USER_ID,
                    executor,
                    new DirectDispatcher()
            );
        }

        private PackingItem replaceWithCommonItem(long assigneeUserId) {
            packingRepository.items.clear();
            PackingItem item = new PackingItem();
            item.setPackingItemId(22L);
            item.setTripId(TRIP_ID);
            item.setCreatedByUserId(99L);
            item.setItemName("여권");
            item.setScope("COMMON");
            item.setAssigneeUserId(assigneeUserId);
            item.setItemStatus("ACTIVE");
            packingRepository.items.add(item);
            return item;
        }

        private void loadInitialData() {
            presenter.loadChecklist(TRIP_ID, false);
            executor.runNext();
        }

        private PackingItem commonItem(long packingItemId, long assigneeUserId) {
            PackingItem item = new PackingItem();
            item.setPackingItemId(packingItemId);
            item.setTripId(TRIP_ID);
            item.setCreatedByUserId(99L);
            item.setItemName("속옷");
            item.setScope("COMMON");
            item.setAssigneeUserId(assigneeUserId);
            item.setItemStatus("ACTIVE");
            return item;
        }

        private PackingItem item(long packingItemId, String source, String scope) {
            PackingItem item = new PackingItem();
            item.setPackingItemId(packingItemId);
            item.setTripId(TRIP_ID);
            item.setCreatedByUserId(CURRENT_USER_ID);
            item.setItemName("item-" + packingItemId);
            item.setSource(source);
            item.setScope(scope);
            item.setItemStatus("ACTIVE");
            return item;
        }
    }

    private static final class RecordingView implements ChecklistContract.View {
        private String tripName;
        private List<PackingItem> items = new ArrayList<>();
        private List<TripMember> members = new ArrayList<>();
        private boolean host;
        private boolean loading;
        private boolean deleteUndoShown;
        private boolean errorShown;

        @Override
        public void showChecklist(String tripName, List<PackingItem> items,
                                  List<TripMember> members, int memberCount, boolean isHost) {
            this.tripName = tripName;
            this.items = items;
            this.members = members;
            this.host = isHost;
        }

        @Override
        public void showError(String message) {
            errorShown = true;
        }

        @Override
        public void showRetryableError(String message) {
        }

        @Override
        public void showLoading(boolean loading) {
            this.loading = loading;
        }

        @Override
        public void showDeleteUndo(PackingItem item) {
            deleteUndoShown = true;
        }
    }

    private static final class DirectDispatcher implements ChecklistPresenter.UiDispatcher {
        @Override
        public void post(Runnable action) {
            action.run();
        }

        @Override
        public void clear() {
        }
    }

    private static final class QueuedExecutor extends AbstractExecutorService {
        private final Queue<Runnable> tasks = new ArrayDeque<>();
        private boolean shutdown;

        @Override
        public void execute(Runnable command) {
            tasks.add(command);
        }

        private void runNext() {
            Runnable task = tasks.remove();
            task.run();
        }

        private int size() {
            return tasks.size();
        }

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown = true;
            List<Runnable> remaining = new ArrayList<>(tasks);
            tasks.clear();
            return remaining;
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown && tasks.isEmpty();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return isTerminated();
        }
    }

    private static final class FakeTripRepository implements TripRepository {
        private Trip trip;

        @Override
        public AppResult<Trip> getTripDetail(long tripId) {
            return AppResult.success(trip);
        }

        @Override
        public AppResult<TripInvite> createTrip(String tripName, int expectedMemberCount,
                                                 long analysisId, Map<String, String> scopes) {
            return AppResult.success(null);
        }

        @Override
        public AppResult<List<Trip>> listMyTrips() {
            return AppResult.success(Collections.emptyList());
        }

        @Override
        public AppResult<List<Trip>> listArchivedTrips() {
            return AppResult.success(Collections.emptyList());
        }

        @Override
        public AppResult<List<TripMember>> listMembers(long tripId) {
            return AppResult.success(Collections.emptyList());
        }

        @Override
        public AppResult<Long> joinByCode(String inviteCode) {
            return AppResult.success(0L);
        }

        @Override
        public AppResult<Void> deleteTrip(long tripId) {
            return AppResult.success(null);
        }

        @Override
        public AppResult<Void> leaveTrip(long tripId) {
            return AppResult.success(null);
        }
    }

    private static final class FakePackingRepository implements PackingRepository {
        private final List<PackingItem> items = new ArrayList<>();
        private int addCalls;
        private int deleteCalls;
        private int assignCalls;
        private Long lastAssigneeUserId;
        private int assignMultipleCalls;
        private long lastGroupId;
        private List<Long> lastAssigneeUserIds;

        @Override
        public AppResult<List<PackingItem>> listItems(long tripId, String since) {
            return AppResult.success(new ArrayList<>(items));
        }

        @Override
        public AppResult<Long> addItem(long tripId, String itemName, String category,
                                       String priority, String scope) {
            addCalls++;
            return AppResult.success(12L);
        }

        @Override
        public AppResult<Boolean> toggleCheck(long itemId) {
            return AppResult.success(true);
        }

        @Override
        public AppResult<Void> assign(long itemId, Long assigneeUserId) {
            assignCalls++;
            lastAssigneeUserId = assigneeUserId;
            return AppResult.success(null);
        }

        @Override
        public AppResult<Void> assignMultiple(long itemGroupId, List<Long> assigneeUserIds) {
            assignMultipleCalls++;
            lastGroupId = itemGroupId;
            lastAssigneeUserIds = assigneeUserIds;
            return AppResult.success(null);
        }

        @Override
        public AppResult<Void> updateItem(long itemId, String itemName, String category,
                                          String priority, String scope) {
            return AppResult.success(null);
        }

        @Override
        public AppResult<Void> deleteItem(long itemId) {
            deleteCalls++;
            return AppResult.success(null);
        }

        @Override
        public AppResult<Void> generate(long tripId, long analysisId,
                                        Map<String, String> itemScopeByName) {
            return AppResult.success(null);
        }
    }
}
