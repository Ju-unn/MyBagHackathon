package com.example.mybaghackathon.ui.feedback;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.data.repository.WeatherRepository;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.WeatherFeedback;
import com.example.mybaghackathon.model.WeatherForecast;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** S10의 여행 정보, 날씨와 의식주 팁 조회를 담당한다. */
public class WeatherFeedbackPresenter implements WeatherFeedbackContract.Presenter {

    private final WeatherFeedbackContract.View view;
    private final TripRepository tripRepository;
    private final WeatherRepository weatherRepository;
    private final ExecutorService executor;
    private final UiDispatcher uiDispatcher;

    private volatile boolean destroyed;
    private boolean loading;
    private long tripId = -1L;

    public WeatherFeedbackPresenter(
            WeatherFeedbackContract.View view,
            TripRepository tripRepository,
            WeatherRepository weatherRepository
    ) {
        this(view, tripRepository, weatherRepository,
                Executors.newSingleThreadExecutor(), new AndroidUiDispatcher());
    }

    WeatherFeedbackPresenter(
            WeatherFeedbackContract.View view,
            TripRepository tripRepository,
            WeatherRepository weatherRepository,
            ExecutorService executor,
            UiDispatcher uiDispatcher
    ) {
        this.view = view;
        this.tripRepository = tripRepository;
        this.weatherRepository = weatherRepository;
        this.executor = executor;
        this.uiDispatcher = uiDispatcher;
    }

    @Override
    public void loadFeedback(long tripId) {
        this.tripId = tripId;
        retry();
    }

    @Override
    public void restoreFeedbackContext(long tripId) {
        this.tripId = tripId;
    }

    @Override
    public void retry() {
        if (destroyed || loading) {
            return;
        }
        if (tripId <= 0L) {
            view.showLoadError("여행방 정보가 없어 팁을 불러올 수 없습니다.");
            return;
        }
        loading = true;
        view.showLoading(true);
        executor.execute(this::loadFeedbackOnWorker);
    }

    private void loadFeedbackOnWorker() {
        try {
            AppResult<Trip> tripResult = tripRepository.getTripDetail(tripId);
            if (!tripResult.isSuccess() || tripResult.getData() == null) {
                postLoadError(messageOf(tripResult, "여행 정보를 불러오지 못했습니다."));
                return;
            }

            Trip trip = tripResult.getData();
            post(() -> view.showTrip(trip));

            AppResult<WeatherForecast> forecastResult = weatherRepository.getForecastByTrip(tripId);
            if (forecastResult.isSuccess() && forecastResult.getData() != null) {
                WeatherForecast forecast = forecastResult.getData();
                post(() -> view.showForecast(forecast.isReady() && forecast.getDays() != null
                        ? forecast.getDays() : Collections.emptyList()));
            } else {
                post(() -> view.showForecast(Collections.emptyList()));
                postRetryableError(messageOf(forecastResult, "날씨 예보를 불러오지 못했습니다."));
            }

            AppResult<WeatherFeedback> feedbackResult = weatherRepository.getFeedback(tripId);
            if (!feedbackResult.isSuccess() || feedbackResult.getData() == null) {
                postLoadError(messageOf(feedbackResult, "날씨 팁을 불러오지 못했습니다."));
                return;
            }

            WeatherFeedback feedback = feedbackResult.getData();
            if (!feedback.isReady()) {
                postComplete(() -> view.showPending(pendingMessage(feedback.getNextRefreshAt())));
            } else if (isEmpty(feedback)) {
                postComplete(view::showEmpty);
            } else {
                postComplete(() -> view.showFeedback(feedback));
            }
        } catch (RuntimeException error) {
            postLoadError("서버 응답을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        uiDispatcher.clear();
        executor.shutdownNow();
    }

    private void post(Runnable action) {
        uiDispatcher.post(() -> {
            if (!destroyed) {
                action.run();
            }
        });
    }

    private void postError(String message) {
        post(() -> view.showError(message));
    }

    private void postRetryableError(String message) {
        post(() -> view.showRetryableError(message));
    }

    private void postLoadError(String message) {
        post(() -> {
            loading = false;
            view.showLoading(false);
            view.showLoadError(message);
        });
    }

    private void postComplete(Runnable render) {
        post(() -> {
            loading = false;
            view.showLoading(false);
            render.run();
        });
    }

    private String messageOf(AppResult<?> result, String fallback) {
        return result.getError() == null || !hasText(result.getError().getMessage())
                ? fallback
                : result.getError().getMessage();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // ready=false일 때 보여줄 안내 문구. next_refresh_at 없으면(체크포인트가 이미 다 지남) 날짜 없이 안내
    private String pendingMessage(String nextRefreshAt) {
        return hasText(nextRefreshAt)
                ? "현재 날씨 정보를 받아올 수 없어 " + nextRefreshAt + "에 갱신됩니다."
                : "현재 날씨 정보를 받아올 수 없어 곧 갱신됩니다.";
    }

    private boolean isEmpty(WeatherFeedback feedback) {
        return !hasText(feedback.getClothing())
                && !hasText(feedback.getFood())
                && !hasText(feedback.getAccommodationNotes());
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
}
