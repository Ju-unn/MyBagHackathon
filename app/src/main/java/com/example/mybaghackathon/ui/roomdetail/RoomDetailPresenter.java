package com.example.mybaghackathon.ui.roomdetail;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.PackingRepository;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.data.repository.WeatherRepository;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.Weather;
import com.example.mybaghackathon.model.WeatherForecast;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** S09 방 상세 데이터 조회와 화면 이동 판단을 담당한다. */
public class RoomDetailPresenter implements RoomDetailContract.Presenter {

    private final RoomDetailContract.View view;
    private final TripRepository tripRepository;
    private final WeatherRepository weatherRepository;
    private final PackingRepository packingRepository;
    private final long currentUserId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean destroyed;
    private long tripId = -1L;
    private boolean isHost;
    private int memberCount = 1;
    private String inviteCode;

    public RoomDetailPresenter(
            RoomDetailContract.View view,
            TripRepository tripRepository,
            WeatherRepository weatherRepository,
            PackingRepository packingRepository,
            long currentUserId
    ) {
        this.view = view;
        this.tripRepository = tripRepository;
        this.weatherRepository = weatherRepository;
        this.packingRepository = packingRepository;
        this.currentUserId = currentUserId;
    }

    @Override
    public void loadRoom(long tripId, boolean initialHost, String inviteCode) {
        this.tripId = tripId;
        this.isHost = initialHost;
        this.inviteCode = inviteCode;

        if (tripId <= 0L) {
            view.showError("여행방 정보가 없어 상세 화면을 불러올 수 없습니다.");
            return;
        }

        executor.execute(() -> {
            try {
                AppResult<Trip> tripResult = tripRepository.getTripDetail(tripId);
                if (!tripResult.isSuccess() || tripResult.getData() == null) {
                    postError(messageOf(tripResult, "여행방 정보를 불러오지 못했습니다."));
                    return;
                }

                Trip trip = tripResult.getData();
                List<?> tripMembers = trip.getMembers();
                memberCount = Math.max(1, tripMembers == null ? 0 : tripMembers.size());
                if (currentUserId > 0L) {
                    isHost = trip.getOwnerUserId() == currentUserId;
                }
                post(() -> view.showTrip(trip, isHost));

                AppResult<WeatherForecast> weatherResult =
                        weatherRepository.getForecastByTrip(tripId);
                if (weatherResult.isSuccess() && weatherResult.getData() != null) {
                    WeatherForecast forecast = weatherResult.getData();
                    List<Weather> weather = forecast.isReady() && forecast.getDays() != null
                            ? forecast.getDays()
                            : Collections.emptyList();
                    post(() -> view.showWeather(weather));
                } else {
                    post(() -> view.showWeather(Collections.emptyList()));
                    postError(messageOf(weatherResult, "날씨 예보를 불러오지 못했습니다."));
                }

                AppResult<List<PackingItem>> packingResult = packingRepository.listItems(tripId, null);
                if (packingResult.isSuccess()) {
                    List<PackingItem> items = packingResult.getData() == null
                            ? Collections.emptyList()
                            : packingResult.getData();
                    post(() -> view.showPackingRestrictions(items));
                } else {
                    postError(messageOf(packingResult, "반입 제한 정보를 불러오지 못했습니다."));
                }
            } catch (RuntimeException error) {
                postError("서버 응답을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.");
            }
        });
    }

    @Override
    public void onTipsClicked() {
        if (tripId > 0L) {
            view.openWeatherFeedback(tripId);
        } else {
            view.showError("여행방 정보가 없습니다.");
        }
    }

    @Override
    public void onChecklistClicked() {
        if (tripId > 0L) {
            view.openChecklist(tripId, memberCount, isHost);
        } else {
            view.showError("여행방 정보가 없습니다.");
        }
    }

    @Override
    public void onInviteClicked() {
        if (!isHost) {
            view.showError("방장만 초대 링크를 공유할 수 있습니다.");
        } else if (!hasText(inviteCode)) {
            view.showError("초대 코드를 불러오지 못했습니다.");
        } else {
            view.showInviteShare(inviteCode.trim());
        }
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
