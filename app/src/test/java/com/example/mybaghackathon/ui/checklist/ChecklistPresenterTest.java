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

    private static final class Fixture {
        private final RecordingView view = new RecordingView();
        private final FakeTripRepository tripRepository = new FakeTripRepository();
        private final FakePackingRepository packingRepository = new FakePackingRepository();
        private final QueuedExecutor executor = new QueuedExecutor();
        private final ChecklistPresenter presenter;

        private Fixture() {
            Trip trip = new Trip();
            trip.setTripId(TRIP_ID);
            trip.setOwnerUserId(CURRENT_USER_ID);
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

        private void loadInitialData() {
            presenter.loadChecklist(TRIP_ID, false);
            executor.runNext();
        }
    }

    private static final class RecordingView implements ChecklistContract.View {
        private String tripName;
        private List<PackingItem> items = new ArrayList<>();
        private List<TripMember> members = new ArrayList<>();
        private boolean host;
        private boolean loading;
        private boolean deleteUndoShown;

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
    }

    private static final class FakePackingRepository implements PackingRepository {
        private final List<PackingItem> items = new ArrayList<>();
        private int addCalls;
        private int deleteCalls;

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
