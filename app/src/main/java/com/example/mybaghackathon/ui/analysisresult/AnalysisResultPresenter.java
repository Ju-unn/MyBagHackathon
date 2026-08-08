package com.example.mybaghackathon.ui.analysisresult;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.AnalysisRepository;
import com.example.mybaghackathon.model.AnalysisResult;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.RestrictedItem;
import com.example.mybaghackathon.model.TripSchedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// AnalysisResultContract.Presenter 구현체 — 필드 포맷팅, 수정 처리, 그리고 "다음" 클릭 시
// /api/itinerary/confirm.php로 수정된 필드를 서버에 확정 반영한 뒤 다음 화면으로 넘김
public class AnalysisResultPresenter implements AnalysisResultContract.Presenter {

    private final AnalysisResultContract.View view;
    private final Context appContext;
    private final AnalysisRepository analysisRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean destroyed = false;

    private long analysisId;
    private String roomName;
    private int memberCount;
    private ArrayList<RestrictedItem> restrictedItems;
    private ArrayList<PackingItem> recommendedItems;
    private String destinationCountry;
    private String destinationCity;
    private String startDate;
    private String endDate;
    private String accommodationName;
    private String transportMode;

    public AnalysisResultPresenter(AnalysisResultContract.View view, Context appContext,
                                    AnalysisRepository analysisRepository) {
        this.view = view;
        this.appContext = appContext;
        this.analysisRepository = analysisRepository;
    }

    @Override
    public void init(long analysisId, String roomName, int memberCount,
                      ArrayList<RestrictedItem> restrictedItems, ArrayList<PackingItem> recommendedItems,
                      String destinationCountry, String destinationCity, String startDate, String endDate,
                      String accommodationName, String transportMode) {
        this.analysisId = analysisId;
        this.roomName = roomName;
        this.memberCount = memberCount;
        this.restrictedItems = restrictedItems;
        this.recommendedItems = recommendedItems;
        this.destinationCountry = destinationCountry;
        this.destinationCity = destinationCity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.accommodationName = accommodationName;
        this.transportMode = transportMode;

        view.showDestination(getDestinationDisplay());
        view.showSchedule(formatDateRange(startDate, endDate));
        view.showLodging(getLodgingDisplay());
        view.showTransport(getTransportDisplay());
    }

    @Override
    public String getDestinationDisplay() {
        return formatDestination(destinationCity, destinationCountry);
    }

    @Override
    public String getStartDate() {
        return startDate == null ? "" : startDate;
    }

    @Override
    public String getEndDate() {
        return endDate == null ? "" : endDate;
    }

    @Override
    public String getLodgingDisplay() {
        return isEmpty(accommodationName) ? appContext.getString(R.string.accommodation_none) : accommodationName;
    }

    @Override
    public String getTransportDisplay() {
        return formatTransportMode(transportMode);
    }

    @Override
    public void onDestinationEdited(String rawInput) {
        if (rawInput.isEmpty()) return;
        int commaIndex = rawInput.indexOf(',');
        destinationCity = commaIndex >= 0 ? rawInput.substring(0, commaIndex).trim() : rawInput;
        destinationCountry = commaIndex >= 0 ? rawInput.substring(commaIndex + 1).trim() : "";
        view.showDestination(getDestinationDisplay());
    }

    @Override
    public boolean onScheduleEdited(String newStart, String newEnd) {
        if (!isValidIsoDate(newStart) || !isValidIsoDate(newEnd)) {
            view.showInvalidDateError();
            return false;
        }
        startDate = newStart;
        endDate = newEnd;
        view.showSchedule(formatDateRange(startDate, endDate));
        return true;
    }

    @Override
    public void onLodgingEdited(String value) {
        accommodationName = value;
        view.showLodging(getLodgingDisplay());
    }

    @Override
    public void onTransportEdited(String value) {
        // 이동수단은 S08로 넘어가지 않는 화면 표시 전용 값이라, 수정 후엔 코드(AIR/OTHER) 대신
        // 사용자가 입력한 텍스트를 그대로 들고 있다가 그대로 보여줌
        transportMode = value;
        view.showTransport(transportMode);
    }

    // "다음"을 누른 시점의 필드 값(수정했든 안 했든)을 /api/itinerary/confirm.php로 보내 서버에
    // 확정 반영한다. 필드가 하나라도 실제로 바뀐 경우 서버가 GPT로 반입규정·추천준비물을 다시
    // 산출해서 돌려주므로, 그 응답값으로 다음 화면에 넘길 목록을 갱신한다.
    @Override
    public void onNextClicked() {
        view.setConfirming(true);
        executor.execute(() -> {
            AppResult<AnalysisResult> result = analysisRepository.confirm(
                    analysisId, destinationCountry, destinationCity, startDate, endDate,
                    transportMode, accommodationName);
            mainHandler.post(() -> handleConfirmResult(result));
        });
    }

    private void handleConfirmResult(AppResult<AnalysisResult> result) {
        if (destroyed) return;
        if (result.isSuccess()) {
            AnalysisResult data = result.getData();
            TripSchedule schedule = data.getSchedule();
            view.navigateToReview(analysisId, roomName, memberCount,
                    new ArrayList<>(data.getRestrictedItems()), new ArrayList<>(data.getRecommendedItems()),
                    schedule.getDestinationCountry(), schedule.getDestinationCity(),
                    schedule.getStartDate(), schedule.getEndDate());
        } else {
            view.setConfirming(false);
            view.showConfirmError(result.getError().getMessage());
        }
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        executor.shutdown();
    }

    private String formatDestination(String city, String country) {
        if (isEmpty(city) && isEmpty(country)) {
            return appContext.getString(R.string.value_unknown);
        }
        if (isEmpty(city)) return country;
        if (isEmpty(country)) return city;
        return city + ", " + country;
    }

    // "yyyy-MM-dd" 두 개를 "M.d — M.d · N박M일" 형식으로 바꾼다. 파싱 실패 시 원본 그대로 보여줌
    private String formatDateRange(String startDate, String endDate) {
        if (isEmpty(startDate) || isEmpty(endDate)) {
            return appContext.getString(R.string.value_unknown);
        }
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            iso.setLenient(false);
            Date start = iso.parse(startDate);
            Date end = iso.parse(endDate);

            SimpleDateFormat display = new SimpleDateFormat("M.d", Locale.KOREA);
            long nights = TimeUnit.MILLISECONDS.toDays(end.getTime() - start.getTime());
            return display.format(start) + " — " + display.format(end)
                    + " · " + nights + "박" + (nights + 1) + "일";
        } catch (ParseException e) {
            return startDate + " — " + endDate;
        }
    }

    private String formatTransportMode(String transportMode) {
        if ("AIR".equals(transportMode)) return appContext.getString(R.string.transport_mode_air);
        if ("OTHER".equals(transportMode)) return appContext.getString(R.string.transport_mode_other);
        return appContext.getString(R.string.value_unknown);
    }

    private boolean isValidIsoDate(String value) {
        if (value.isEmpty()) return false;
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            iso.setLenient(false);
            iso.parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
