package com.example.mybaghackathon.ui.feedback;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.data.repository.WeatherRepository;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.Weather;
import com.example.mybaghackathon.model.WeatherFeedback;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** S10의 여행 정보, 날씨와 의식주 팁 조회를 담당한다. */
public class WeatherFeedbackPresenter implements WeatherFeedbackContract.Presenter {

    private final WeatherFeedbackContract.View view;
    private final TripRepository tripRepository;
    private final WeatherRepository weatherRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean destroyed;

    public WeatherFeedbackPresenter(
            WeatherFeedbackContract.View view,
            TripRepository tripRepository,
            WeatherRepository weatherRepository
    ) {
        this.view = view;
        this.tripRepository = tripRepository;
        this.weatherRepository = weatherRepository;
    }

    @Override
    public void loadFeedback(long tripId) {
        if (tripId <= 0L) {
            view.showError("여행방 정보가 없어 팁을 불러올 수 없습니다.");
            return;
        }

        executor.execute(() -> {
            try {
                AppResult<Trip> tripResult = tripRepository.getTripDetail(tripId);
                if (!tripResult.isSuccess() || tripResult.getData() == null) {
                    postError(messageOf(tripResult, "여행 정보를 불러오지 못했습니다."));
                    return;
                }

                Trip trip = tripResult.getData();
                post(() -> view.showTrip(trip));

                if (hasText(trip.getDestinationCity())
                        && hasText(trip.getStartDate())
                        && hasText(trip.getEndDate())) {
                    AppResult<List<Weather>> forecastResult = weatherRepository.getForecast(
                            trip.getDestinationCity(), trip.getStartDate(), trip.getEndDate());
                    if (forecastResult.isSuccess()) {
                        List<Weather> weather = forecastResult.getData() == null
                                ? Collections.emptyList()
                                : forecastResult.getData();
                        post(() -> view.showForecast(weather));
                    }
                }

                AppResult<WeatherFeedback> feedbackResult = weatherRepository.getFeedback(tripId);
                if (feedbackResult.isSuccess() && feedbackResult.getData() != null) {
                    post(() -> view.showFeedback(feedbackResult.getData()));
                } else {
                    postError(messageOf(feedbackResult, "날씨 팁을 불러오지 못했습니다."));
                }
            } catch (RuntimeException error) {
                postError("서버 응답을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.");
            }
        });
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        mainHandler.removeCallbacksAndMessages(null);
        executor.shutdownNow();
    }

    private void post(Runnable action) {
        mainHandler.post(() -> {
            if (!destroyed) {
                action.run();
            }
        });
    }

    private void postError(String message) {
        post(() -> view.showError(message));
    }

    private String messageOf(AppResult<?> result, String fallback) {
        return result.getError() == null || !hasText(result.getError().getMessage())
                ? fallback
                : result.getError().getMessage();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
