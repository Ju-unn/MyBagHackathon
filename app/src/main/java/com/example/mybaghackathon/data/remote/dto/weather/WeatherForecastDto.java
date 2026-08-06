package com.example.mybaghackathon.data.remote.dto.weather;

import java.util.List;

// GET /api/weather/forecast.php 응답 data ({"location","days":[...]})
public class WeatherForecastDto {

    private String location;
    private List<WeatherDto> days;

    public String getLocation() {
        return location;
    }

    public List<WeatherDto> getDays() {
        return days;
    }
}
