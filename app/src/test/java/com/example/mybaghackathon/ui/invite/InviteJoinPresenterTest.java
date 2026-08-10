package com.example.mybaghackathon.ui.invite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.mybaghackathon.common.AppError;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.TripRepository;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public class InviteJoinPresenterTest {

    @Test
    public void validCode_joinsAndOpensTrip() {
        RecordingView view = new RecordingView();
        QueuedExecutor executor = new QueuedExecutor();
        InviteJoinPresenter presenter = presenter(
                view, executor, AppResult.success(27L));

        presenter.join(" CODE-1 ");

        assertTrue(view.loading);
        assertEquals(1, executor.size());
        executor.runNext();
        assertEquals(27L, view.openedTripId);
        assertFalse(view.errorShown);
    }

    @Test
    public void emptyCode_showsValidationErrorWithoutRequest() {
        RecordingView view = new RecordingView();
        QueuedExecutor executor = new QueuedExecutor();
        InviteJoinPresenter presenter = presenter(
                view, executor, AppResult.success(27L));

        presenter.join("  ");

        assertTrue(view.errorShown);
        assertEquals(0, executor.size());
    }

    @Test
    public void apiFailure_showsServerMessageAndAllowsRetry() {
        RecordingView view = new RecordingView();
        QueuedExecutor executor = new QueuedExecutor();
        InviteJoinPresenter presenter = presenter(
                view, executor, AppResult.failure(new AppError(404, "만료된 초대입니다.")));

        presenter.join("OLD-CODE");
        executor.runNext();

        assertTrue(view.errorShown);
        assertEquals("만료된 초대입니다.", view.errorMessage);

        presenter.retry();
        assertEquals(1, executor.size());
    }

    @Test
    public void repeatedTapWhileJoining_doesNotDuplicateRequest() {
        RecordingView view = new RecordingView();
        QueuedExecutor executor = new QueuedExecutor();
        InviteJoinPresenter presenter = presenter(
                view, executor, AppResult.success(27L));

        presenter.join("CODE-1");
        presenter.retry();

        assertEquals(1, executor.size());
    }

    private InviteJoinPresenter presenter(
            RecordingView view,
            QueuedExecutor executor,
            AppResult<Long> joinResult
    ) {
        TripRepository repository = (TripRepository) Proxy.newProxyInstance(
                TripRepository.class.getClassLoader(),
                new Class<?>[]{TripRepository.class},
                (proxy, method, args) -> joinResult
        );
        return new InviteJoinPresenter(view, repository, executor, new ImmediateDispatcher());
    }

    private static final class RecordingView implements InviteJoinContract.View {
        private boolean loading;
        private boolean errorShown;
        private String errorMessage;
        private long openedTripId = -1L;

        @Override
        public void showLoading() {
            loading = true;
        }

        @Override
        public void showJoinError(String message) {
            errorShown = true;
            errorMessage = message;
        }

        @Override
        public void openTrip(long tripId) {
            openedTripId = tripId;
        }
    }

    private static final class ImmediateDispatcher implements InviteJoinPresenter.UiDispatcher {
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

        int size() {
            return tasks.size();
        }

        void runNext() {
            tasks.remove().run();
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
}
