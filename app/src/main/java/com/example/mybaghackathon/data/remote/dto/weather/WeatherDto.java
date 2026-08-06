package com.example.mybaghackathon.data.remote.dto.weather;

import com.google.gson.annotations.SerializedName;

/**
 * WeatherForecastDto.days의 일별 예보 항목 (JSON 예시)
 *
 * { "date": "2026-08-10", "temp_max": 31.87, "temp_min": 25.25, "condition": "cloud", "precipitation_probability": 0 }
 */
public class WeatherDto {

    // 날짜 (yyyy-MM-dd)
    private String date;

    // 최고 기온 (섭씨, units=metric 고정)
    @SerializedName("temp_max")
    private double tempMax;

    // 최저 기온 (섭씨, units=metric 고정)
    @SerializedName("temp_min")
    private double tempMin;

    // 날씨 상태 — "sun" / "rain" / "cloud" / "snow" 중 하나 (서버가 OpenWeatherMap 코드를 4종으로 단순화해서 내려줌)
    private String condition;

    // 강수 확률 (0~100, %) — 서버가 값을 못 받으면 null
    @SerializedName("precipitation_probability")
    private Integer precipitationProbability;

    public String getDate() {
        return date;
    }

    public double getTempMax() {
        return tempMax;
    }

    public double getTempMin() {
        return tempMin;
    }

    public String getCondition() {
        return condition;
    }

    public Integer getPrecipitationProbability() {
        return precipitationProbability;
    }
}
