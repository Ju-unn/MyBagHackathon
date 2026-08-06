package com.example.mybaghackathon.model;

// 여행지의 하루치 날씨 정보를 나타내는 앱 내부 모델 (WeatherDto 1:1 대응)
public class Weather {

    // 날짜 (yyyy-MM-dd)
    private final String date;

    // 최고 기온 (섭씨)
    private final double tempMax;

    // 최저 기온 (섭씨)
    private final double tempMin;

    // 날씨 상태 — "sun" / "rain" / "cloud" / "snow" 중 하나
    private final String condition;

    // 강수 확률 (0~100, %) — 값 없으면 null
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
