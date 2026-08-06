package com.example.mybaghackathon.data.remote.api;

import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.weather.WeatherForecastDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// 날씨 관련 EC2 서버 API 엔드포인트 정의
public interface WeatherApi {

    /**
     * 도시명 + 기간(yyyy-MM-dd)으로 일별 예보 조회
     *
     * 요청: GET /api/weather/forecast.php?city=도쿄&start_date=2026-08-10&end_date=2026-08-13
     *
     * 응답:
     * {
     *   "success": true,
     *   "message": "날씨 조회 성공",
     *   "data": {
     *     "location": "도쿄",
     *     "days": [
     *       { "date": "2026-08-10", "temp_max": 31.87, "temp_min": 25.25, "condition": "cloud", "precipitation_probability": 0 },
     *       { "date": "2026-08-11", "temp_max": 32.95, "temp_min": 24.41, "condition": "sun", "precipitation_probability": 0 }
     *     ]
     *   }
     * }
     */
    @GET("api/weather/forecast.php")
    Call<ApiResponseDto<WeatherForecastDto>> getForecast(
            @Query("city") String city,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );
}
