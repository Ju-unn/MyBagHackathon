package com.example.mybaghackathon.data.remote.dto.weather;

import com.google.gson.annotations.SerializedName;

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
 *
 * trip_id 조회(getForecastByTrip)는 아직 체크포인트 갱신 전이면 ready=false로 오고 location/days가 없다:
 * { "ready": false, "next_refresh_at": "2026-08-21" }
 */
public class WeatherForecastDto {

    // trip_id 조회 전용. city/date 직접 조회 응답엔 없음(기본값 false, 무시할 것)
    private boolean ready;

    // trip_id 조회 전용. 다음 자동 갱신 예정일(yyyy-MM-dd) 또는 null
    @SerializedName("next_refresh_at")
    private String nextRefreshAt;

    // 지오코딩으로 확정된 지역명 (요청한 city 파라미터와 표기가 다를 수 있음)
    private String location;

    // 요청한 start_date~end_date 구간의 일별 예보 목록
    private List<WeatherDto> days;

    public boolean isReady() {
        return ready;
    }

    public String getNextRefreshAt() {
        return nextRefreshAt;
    }

    public String getLocation() {
        return location;
    }

    public List<WeatherDto> getDays() {
        return days;
    }
}
