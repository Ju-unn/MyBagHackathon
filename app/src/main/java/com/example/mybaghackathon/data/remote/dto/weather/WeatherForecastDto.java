package com.example.mybaghackathon.data.remote.dto.weather;

import java.util.List;

/**
 * GET /api/weather/forecast.php 응답의 data 부분 (JSON 예시)
 *
 * {
 *   "location": "도쿄",
 *   "days": [
 *     { "date": "2026-08-10", "temp_max": 31.87, "temp_min": 25.25, "condition": "cloud", "precipitation_probability": 0 }
 *   ]
 * }
 */
public class WeatherForecastDto {

    // 지오코딩으로 확정된 지역명 (요청한 city 파라미터와 표기가 다를 수 있음)
    private String location;

    // 요청한 start_date~end_date 구간의 일별 예보 목록
    private List<WeatherDto> days;

    public String getLocation() {
        return location;
    }

    public List<WeatherDto> getDays() {
        return days;
    }
}
