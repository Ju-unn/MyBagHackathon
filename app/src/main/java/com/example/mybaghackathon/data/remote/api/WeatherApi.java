package com.example.mybaghackathon.data.remote.api;

import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.weather.WeatherForecastDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// 날씨 관련 EC2 서버 API 엔드포인트 정의
public interface WeatherApi {

    // 도시명 + 기간(yyyy-MM-dd)으로 일별 예보 조회
    @GET("api/weather/forecast.php")
    Call<ApiResponseDto<WeatherForecastDto>> getForecast(
            @Query("city") String city,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );
}
