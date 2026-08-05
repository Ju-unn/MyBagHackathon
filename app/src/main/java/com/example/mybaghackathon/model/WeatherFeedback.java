package com.example.mybaghackathon.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 확정된 여행 일정의 날씨 요약과 의식주 준비 안내를 나타내는 모델입니다.
 */
public class WeatherFeedback {

    private long tripId;
    private String region;
    private String startDate;
    private String endDate;
    private Double minimumTemperature;
    private Double maximumTemperature;
    private List<Weather> dailyForecasts = new ArrayList<>();
    private String clothingAdvice;
    private String foodAdvice;
    private String accommodationAdvice;

    public WeatherFeedback() {
    }

    public WeatherFeedback(
            long tripId,
            String region,
            String startDate,
            String endDate,
            Double minimumTemperature,
            Double maximumTemperature,
            List<Weather> dailyForecasts,
            String clothingAdvice,
            String foodAdvice,
            String accommodationAdvice
    ) {
        this.tripId = tripId;
        this.region = region;
        this.startDate = startDate;
        this.endDate = endDate;
        this.minimumTemperature = minimumTemperature;
        this.maximumTemperature = maximumTemperature;
        setDailyForecasts(dailyForecasts);
        this.clothingAdvice = clothingAdvice;
        this.foodAdvice = foodAdvice;
        this.accommodationAdvice = accommodationAdvice;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Double getMinimumTemperature() {
        return minimumTemperature;
    }

    public void setMinimumTemperature(Double minimumTemperature) {
        this.minimumTemperature = minimumTemperature;
    }

    public Double getMaximumTemperature() {
        return maximumTemperature;
    }

    public void setMaximumTemperature(Double maximumTemperature) {
        this.maximumTemperature = maximumTemperature;
    }

    public List<Weather> getDailyForecasts() {
        return dailyForecasts;
    }

    public void setDailyForecasts(List<Weather> dailyForecasts) {
        this.dailyForecasts = dailyForecasts == null
                ? new ArrayList<>()
                : new ArrayList<>(dailyForecasts);
    }

    public String getClothingAdvice() {
        return clothingAdvice;
    }

    public void setClothingAdvice(String clothingAdvice) {
        this.clothingAdvice = clothingAdvice;
    }

    public String getFoodAdvice() {
        return foodAdvice;
    }

    public void setFoodAdvice(String foodAdvice) {
        this.foodAdvice = foodAdvice;
    }

    public String getAccommodationAdvice() {
        return accommodationAdvice;
    }

    public void setAccommodationAdvice(String accommodationAdvice) {
        this.accommodationAdvice = accommodationAdvice;
    }
}
