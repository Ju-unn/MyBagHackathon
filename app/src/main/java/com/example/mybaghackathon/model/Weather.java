package com.example.mybaghackathon.model;

// 여행지의 하루치 날씨 정보를 나타내는 앱 내부 모델
public class Weather {

    private final String date;
    private final double tempMax;
    private final double tempMin;
    private final String condition;
    private final Integer precipitationProbability;

    public Weather(String date, double tempMax, double tempMin, String condition, Integer precipitationProbability) {
        this.date = date;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.condition = condition;
        this.precipitationProbability = precipitationProbability;
    }

    public String getDate() {
        return date;
    }

    public double getTempMax() {
        return tempMax;
    }

    public double getTempMin() {
        return tempMin;
    }

    // "sun" / "rain" / "cloud" / "snow" — WeatherIconView 타입은 WeatherMapper.toIconType()로 변환
    public String getCondition() {
        return condition;
    }

    public Integer getPrecipitationProbability() {
        return precipitationProbability;
    }
}
