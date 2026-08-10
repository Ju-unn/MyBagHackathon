package com.example.mybaghackathon.ui.feedback;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.data.repository.WeatherRepository;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.Weather;
import com.example.mybaghackathon.model.WeatherFeedback;
import com.example.mybaghackathon.model.WeatherForecast;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherFeedbackPresenterTest {

    @Test
    public void pendingFeedback_showsPendingStateAndStopsLoading() {
        RecordingView view = new RecordingView();
        QueuedExecutor executor = new QueuedExecutor();
        WeatherFeedbackPresenter presenter = presenter(
                view, executor, new WeatherFeedback(false, "2026-08-11", null, null, null));

        presenter.loadFeedback(3L);
        presenter.retry();
        assertTrue(view.loading);
        assertTrue(executor.size() == 1);
        executor.runNext();

        assertFalse(view.loading);
        assertTrue(view.tripShown);
        assertTrue(view.pending);
        assertFalse(view.loadError);
    }

    @Test
    public void emptyReadyFeedback_showsEmptyState() {
        RecordingView view = new RecordingView();
        QueuedExecutor executor = new QueuedExecutor();
        WeatherFeedbackPresenter presenter = presenter(
                view, executor, new WeatherFeedback(true, null, " ", null, ""));

        presenter.loadFeedback(3L);
        executor.runNext();

        assertFalse(view.loading);
        assertTrue(view.empty);
    }

    private WeatherFeedbackPresenter presenter(
            RecordingView view, QueuedExecutor executor, WeatherFeedback feedback) {
        Trip trip = new Trip();
        trip.setTripId(3L);
        TripRepository tripRepository = proxy(
                TripRepository.class, "getTripDetail", AppResult.success(trip));
        WeatherRepository weatherRepository = (WeatherRepository) Proxy.newProxyInstance(
                WeatherRepository.class.getClassLoader(),
                new Class[]{WeatherRepository.class},
                (proxy, method, args) -> {
                    if ("getForecastByTrip".equals(method.getName())) {
                        return AppResult.success(
                                new WeatherForecast(true, null, Collections.emptyList()));
                    }
                    if ("getFeedback".equals(method.getName())) {
                        return AppResult.success(feedback);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        return new WeatherFeedbackPresenter(
                view, tripRepository, weatherRepository, executor, new DirectDispatcher());
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, String methodName, Object result) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type},
                (proxy, method, args) -> {
                    if (methodName.equals(method.getName())) return result;
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static final class RecordingView implements WeatherFeedbackContract.View {
        private boolean loading;
        private boolean loadError;
        private boolean tripShown;
        private boolean pending;
        private boolean empty;
        @Override public void showLoading(boolean loading) { this.loading = loading; }
        @Override public void showLoadError(String message) { loadError = true; }
        @Override public void showTrip(Trip trip) { tripShown = true; }
        @Override public void showForecast(List<Weather> weather) { }
        @Override public void showFeedback(WeatherFeedback feedback) { }
        @Override public void showPending(String message) { pending = true; }
        @Override public void showEmpty() { empty = true; }
        @Override public void showError(String message) { }
        @Override public void showRetryableError(String message) { }
    }

    private static final class DirectDispatcher implements WeatherFeedbackPresenter.UiDispatcher {
        @Override public void post(Runnable action) { action.run(); }
        @Override public void clear() { }
    }

    private static final class QueuedExecutor extends AbstractExecutorService {
        private final Queue<Runnable> tasks = new ArrayDeque<>();
        private boolean shutdown;
        @Override public void execute(Runnable command) { tasks.add(command); }
        private void runNext() { tasks.remove().run(); }
        private int size() { return tasks.size(); }
        @Override public void shutdown() { shutdown = true; }
        @Override public List<Runnable> shutdownNow() {
            shutdown = true;
            List<Runnable> remaining = new ArrayList<>(tasks);
            tasks.clear();
            return remaining;
        }
        @Override public boolean isShutdown() { return shutdown; }
        @Override public boolean isTerminated() { return shutdown && tasks.isEmpty(); }
        @Override public boolean awaitTermination(long timeout, TimeUnit unit) {
            return isTerminated();
        }
    }
}
