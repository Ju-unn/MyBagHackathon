package com.example.mybaghackathon.ui.roomdetail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.mybaghackathon.common.AppError;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.PackingRepository;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.data.repository.WeatherRepository;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.Weather;
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

public class RoomDetailPresenterTest {

    @Test
    public void successfulLoad_showsContentAndEmptyWeatherState() {
        RecordingView view = new RecordingView();
        QueuedExecutor executor = new QueuedExecutor();
        RoomDetailPresenter presenter = presenter(
                view, executor, AppResult.success(trip()),
                AppResult.success(new WeatherForecast(true, null, Collections.emptyList())));

        presenter.loadRoom(3L, false, "ABC");
        presenter.retry();

        assertTrue(view.loading);
        assertEquals(1, executor.size());
        executor.runNext();
        assertFalse(view.loading);
        assertTrue(view.tripShown);
        assertTrue(view.weatherEmpty);
        assertFalse(view.loadError);
    }

    @Test
    public void tripFailure_showsFullRetryState() {
        RecordingView view = new RecordingView();
        QueuedExecutor executor = new QueuedExecutor();
        RoomDetailPresenter presenter = presenter(
                view, executor,
                AppResult.failure(new AppError(500, "방 조회 실패")),
                AppResult.success(new WeatherForecast(true, null, Collections.emptyList())));

        presenter.loadRoom(3L, false, null);
        executor.runNext();

        assertFalse(view.loading);
        assertTrue(view.loadError);
        assertFalse(view.tripShown);
    }

    private RoomDetailPresenter presenter(
            RecordingView view, QueuedExecutor executor,
            AppResult<Trip> tripResult, AppResult<WeatherForecast> forecastResult) {
        TripRepository tripRepository = proxy(TripRepository.class, "getTripDetail", tripResult);
        WeatherRepository weatherRepository = proxy(
                WeatherRepository.class, "getForecastByTrip", forecastResult);
        PackingRepository packingRepository = proxy(
                PackingRepository.class, "listItems", AppResult.success(Collections.emptyList()));
        return new RoomDetailPresenter(
                view, tripRepository, weatherRepository, packingRepository, 7L,
                executor, new DirectDispatcher());
    }

    private Trip trip() {
        Trip trip = new Trip();
        trip.setTripId(3L);
        trip.setOwnerUserId(7L);
        trip.setTripName("테스트 여행");
        trip.setMembers(Collections.emptyList());
        return trip;
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, String supportedMethod, Object result) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type},
                (proxy, method, args) -> {
                    if (supportedMethod.equals(method.getName())) return result;
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static final class RecordingView implements RoomDetailContract.View {
        private boolean loading;
        private boolean loadError;
        private boolean tripShown;
        private boolean weatherEmpty;
        @Override public void showLoading(boolean loading) { this.loading = loading; }
        @Override public void showLoadError(String message) { loadError = true; }
        @Override public void showTrip(Trip trip, boolean isHost) { tripShown = true; }
        @Override public void showWeather(List<Weather> weather) { }
        @Override public void showWeatherPending(String message) { }
        @Override public void showWeatherEmpty() { weatherEmpty = true; }
        @Override public void showPackingRestrictions(List<PackingItem> items) { }
        @Override public void showError(String message) { }
        @Override public void showRetryableError(String message) { }
        @Override public void openWeatherFeedback(long tripId) { }
        @Override public void openChecklist(long tripId, int memberCount, boolean isHost) { }
        @Override public void showInviteShare(String inviteCode) { }
    }

    private static final class DirectDispatcher implements RoomDetailPresenter.UiDispatcher {
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
