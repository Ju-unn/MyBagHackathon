package com.example.mybaghackathon.data.remote.api;

import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.weather.WeatherFeedbackDto;
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

    /**
     * 여행방 trip_id로 저장된 예보 조회 (라이브 호출 없이 방 생성/D-7/D-3/D-1 체크포인트에 갱신된 값만 반환)
     *
     * 요청: GET /api/weather/forecast.php?trip_id=2
     *
     * 아직 체크포인트 갱신 전이면 data.ready=false, data.next_refresh_at에 다음 갱신 예정일이 옴
     */
    @GET("api/weather/forecast.php")
    Call<ApiResponseDto<WeatherForecastDto>> getForecastByTrip(@Query("trip_id") long tripId);

    /**
     * 여행방 목적지·기간·날씨 기반 옷차림/음식/숙소 GPT 조언 조회 (S10 WeatherFeedbackActivity)
     *
     * 요청: GET /api/weather/feedback.php?trip_id=2
     *
     * 응답:
     * {
     *   "success": true,
     *   "message": "조회 성공",
     *   "data": {
     *     "clothing": "...",
     *     "food": "...",
     *     "accommodation_notes": "..."
     *   }
     * }
     */
    @GET("api/weather/feedback.php")
    Call<ApiResponseDto<WeatherFeedbackDto>> getFeedback(@Query("trip_id") long tripId);
}
