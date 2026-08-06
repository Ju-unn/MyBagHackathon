package com.example.mybaghackathon.data.remote.dto.weather;

import com.google.gson.annotations.SerializedName;

// WeatherForecastDto.days의 일별 예보 항목
public class WeatherDto {

    private String date;

    @SerializedName("temp_max")
    private double tempMax;

    @SerializedName("temp_min")
    private double tempMin;

    private String condition;

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
