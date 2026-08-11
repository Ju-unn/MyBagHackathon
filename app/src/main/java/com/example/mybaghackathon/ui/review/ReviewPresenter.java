package com.example.mybaghackathon.ui.review;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.mapper.WeatherMapper;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.data.repository.WeatherRepository;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.RestrictedItem;
import com.example.mybaghackathon.model.TripInvite;
import com.example.mybaghackathon.model.Weather;
import com.example.mybaghackathon.ui.atoms.RestrictionTagView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// ReviewContract.Presenter 구현체 — 날씨/방생성 API 호출, 준비물 그룹핑,
// 반입규정 태그 매칭, 필드 포맷팅을 담당
public class ReviewPresenter implements ReviewContract.Presenter {

    private static final String SCOPE_COMMON = "COMMON";

    private final ReviewContract.View view;
    private final Context appContext;
    private final WeatherRepository weatherRepository;
    private final TripRepository tripRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Map<String, String> itemScopeByName = new LinkedHashMap<>();

    private volatile boolean destroyed = false;

    private String roomName;
    private int memberCount;
    private long analysisId;
    private ArrayList<RestrictedItem> restrictedItems;

    public ReviewPresenter(ReviewContract.View view, Context appContext,
                            WeatherRepository weatherRepository, TripRepository tripRepository) {
        this.view = view;
        this.appContext = appContext;
        this.weatherRepository = weatherRepository;
        this.tripRepository = tripRepository;
    }

    @Override
    public void init(String roomName, int memberCount, long analysisId,
                      ArrayList<RestrictedItem> restrictedItems, ArrayList<PackingItem> recommendedItems,
                      String destinationCountry, String destinationCity, String startDate, String endDate) {
        this.roomName = roomName;
        this.memberCount = memberCount;
        this.analysisId = analysisId;
        this.restrictedItems = restrictedItems;

        view.showDestination(formatDestination(destinationCity, destinationCountry));
        view.showDateRange(formatDateRange(startDate, endDate));

        setupRestrictionWarning();
        setupRecommendedItems(recommendedItems);
        loadWeather(destinationCity, startDate, endDate);
    }

    @Override
    public List<RestrictedItem> getRestrictedItems() {
        return restrictedItems;
    }

    @Override
    public void onItemScopeToggled(String itemName, boolean checked) {
        if (checked) {
            itemScopeByName.put(itemName, SCOPE_COMMON);
        } else {
            itemScopeByName.remove(itemName);
        }
    }

    @Override
    public void onGenerateClicked() {
        view.setGenerating(true);
        executor.execute(() -> {
            AppResult<TripInvite> result = tripRepository.createTrip(roomName, memberCount, analysisId, itemScopeByName);
            mainHandler.post(() -> {
                if (destroyed) return;
                if (result.isSuccess()) {
                    TripInvite invite = result.getData();
                    view.navigateToRoomDetail(
                            invite.getTripId(),
                            roomName,
                            invite.getInviteCode(),
                            new ArrayList<>(itemScopeByName.keySet()));
                } else {
                    view.setGenerating(false);
                    view.showGenerateError(result.getError().getMessage());
                }
            });
        });
    }

    private void setupRestrictionWarning() {
        if (restrictedItems == null || restrictedItems.isEmpty()) {
            view.hideRestrictionWarning();
            return;
        }
        String firstItemName = restrictedItems.get(0).getItemName();
        String bannerText = restrictedItems.size() == 1
                ? appContext.getString(R.string.review_restriction_warning_single, firstItemName)
                : appContext.getString(R.string.review_restriction_warning_multiple, firstItemName, restrictedItems.size());
        view.showRestrictionWarning(bannerText);
    }

    private void setupRecommendedItems(ArrayList<PackingItem> recommendedItems) {
        if (recommendedItems == null) return;

        List<ReviewContract.ItemView> required = new ArrayList<>();
        List<ReviewContract.ItemView> recommended = new ArrayList<>();
        List<ReviewContract.ItemView> optional = new ArrayList<>();
        for (PackingItem item : recommendedItems) {
            // 체크 안 한 항목은 itemScopeByName에 아예 안 들어가서 방 생성 시 제외됨 (onItemScopeToggled 참고)
            ReviewContract.ItemView itemView = new ReviewContract.ItemView(
                    item.getItemName(), restrictionTagType(item.getItemName()));

            String priority = item.getPriority();
            if ("REQUIRED".equals(priority)) required.add(itemView);
            else if ("OPTIONAL".equals(priority)) optional.add(itemView);
            else recommended.add(itemView); // RECOMMENDED 또는 알 수 없는 값은 중간 취급
        }

        view.showRequiredItems(required);
        view.showRecommendedItems(recommended);
        view.showOptionalItems(optional);
    }

    // RestrictionTagView는 3종류만 표현 가능 — LIMITED/CAUTION은 태그 없이 넘어감
    private Integer restrictionTagType(String itemName) {
        RestrictedItem restriction = findRestriction(itemName);
        if (restriction == null) return null;
        String type = restriction.getRestrictionType();
        if ("PROHIBITED".equals(type)) return RestrictionTagView.PROHIBITED;
        if ("CARRY_ON_ONLY".equals(type)) return RestrictionTagView.CABIN_ONLY;
        if ("CHECKED_ONLY".equals(type)) return RestrictionTagView.CHECKED_ONLY;
        return null;
    }

    private RestrictedItem findRestriction(String itemName) {
        if (restrictedItems == null) return null;
        for (RestrictedItem restriction : restrictedItems) {
            if (restriction.getItemName().equals(itemName)) return restriction;
        }
        return null;
    }

    private String formatDestination(String city, String country) {
        if (city == null || city.isEmpty()) return country == null ? appContext.getString(R.string.value_unknown) : country;
        if (country == null || country.isEmpty()) return city;
        return city + ", " + country;
    }

    // "yyyy-MM-dd" 두 개를 "M.d — M.d · N박M일" 형식으로 바꾼다. 파싱 실패 시 원본 그대로 보여줌
    private String formatDateRange(String startDate, String endDate) {
        if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            return appContext.getString(R.string.value_unknown);
        }
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            iso.setLenient(false);
            Date start = iso.parse(startDate);
            Date end = iso.parse(endDate);
            SimpleDateFormat display = new SimpleDateFormat("M.d", Locale.KOREA);
            long nights = TimeUnit.MILLISECONDS.toDays(end.getTime() - start.getTime());
            return display.format(start) + " — " + display.format(end) + " · " + nights + "박" + (nights + 1) + "일";
        } catch (ParseException e) {
            return startDate + " — " + endDate;
        }
    }

    private void loadWeather(String city, String startDate, String endDate) {
        if (city == null || city.isEmpty() || startDate == null || endDate == null) return;

        executor.execute(() -> {
            AppResult<List<Weather>> result = weatherRepository.getForecast(city, startDate, endDate);
            mainHandler.post(() -> {
                if (destroyed || !result.isSuccess()) return; // 실패해도 날씨 섹션만 비워둠, 나머지 화면은 그대로
                List<Weather> days = result.getData();
                for (Weather day : days) {
                    view.addWeatherRow(formatWeatherDate(day.getDate()),
                            WeatherMapper.toIconType(day.getCondition()),
                            formatWeatherStatus(day.getCondition(), day.getTempMax()));
                }
                // 날씨 API가 한 번에 최대 10일치만 주기 때문에, 여행 기간이 그보다 길면
                // 뒤쪽 날짜는 안내 없이 그냥 안 뜨는 것처럼 보일 수 있어 문구로 알려줌
                if (days.size() < tripDurationDays(startDate, endDate)) {
                    view.showWeatherLimitNotice();
                }
            });
        });
    }

    private int tripDurationDays(String startDate, String endDate) {
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            iso.setLenient(false);
            Date start = iso.parse(startDate);
            Date end = iso.parse(endDate);
            long nights = TimeUnit.MILLISECONDS.toDays(end.getTime() - start.getTime());
            return (int) nights + 1;
        } catch (ParseException e) {
            return 0;
        }
    }

    private String formatWeatherDate(String isoDate) {
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            iso.setLenient(false);
            Date date = iso.parse(isoDate);
            return new SimpleDateFormat("M.d(E)", Locale.KOREA).format(date);
        } catch (ParseException e) {
            return isoDate;
        }
    }

    private String formatWeatherStatus(String condition, double tempMax) {
        String label;
        if ("rain".equals(condition)) label = appContext.getString(R.string.weather_condition_rain);
        else if ("cloud".equals(condition)) label = appContext.getString(R.string.weather_condition_cloud);
        else if ("snow".equals(condition)) label = appContext.getString(R.string.weather_condition_snow);
        else label = appContext.getString(R.string.weather_condition_sun);
        return label + " " + Math.round(tempMax) + "°";
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        executor.shutdown();
    }
}
