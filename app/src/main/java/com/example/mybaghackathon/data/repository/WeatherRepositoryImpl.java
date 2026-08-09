package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppError;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.mapper.WeatherMapper;
import com.example.mybaghackathon.data.remote.api.WeatherApi;
import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.weather.WeatherFeedbackDto;
import com.example.mybaghackathon.data.remote.dto.weather.WeatherForecastDto;
import com.example.mybaghackathon.model.Weather;
import com.example.mybaghackathon.model.WeatherFeedback;
import com.example.mybaghackathon.model.WeatherForecast;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

// WeatherRepository의 실제 구현체 (WeatherApi 호출). 네트워크 호출이라 반드시 메인 스레드 밖에서 호출할 것
public class WeatherRepositoryImpl implements WeatherRepository {

    private final WeatherApi weatherApi;

    public WeatherRepositoryImpl(WeatherApi weatherApi) {
        this.weatherApi = weatherApi;
    }

    // 날씨 예보 API 호출 → 성공 시 일별 예보 목록 반환
    @Override
    public AppResult<List<Weather>> getForecast(String city, String startDate, String endDate) {
        try {
            Response<ApiResponseDto<WeatherForecastDto>> response =
                    weatherApi.getForecast(city, startDate, endDate).execute();

            ApiResponseDto<WeatherForecastDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }

            return AppResult.success(WeatherMapper.from(body.getData().getDays()));
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    // trip_id로 저장된 예보 조회 (라이브 호출 없음)
    @Override
    public AppResult<WeatherForecast> getForecastByTrip(long tripId) {
        try {
            Response<ApiResponseDto<WeatherForecastDto>> response =
                    weatherApi.getForecastByTrip(tripId).execute();

            ApiResponseDto<WeatherForecastDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }

            return AppResult.success(WeatherMapper.from(body.getData()));
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    // 날씨 기반 옷차림/음식/숙소 GPT 조언 API 호출
    @Override
    public AppResult<WeatherFeedback> getFeedback(long tripId) {
        try {
            Response<ApiResponseDto<WeatherFeedbackDto>> response =
                    weatherApi.getFeedback(tripId).execute();

            ApiResponseDto<WeatherFeedbackDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }

            return AppResult.success(WeatherMapper.from(body.getData()));
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    // 실패 응답에서 상태코드와 메시지를 뽑아 AppError로 변환한다
    private AppError toError(Response<?> response, ApiResponseDto<?> body) {
        String message = body != null ? body.getMessage() : "요청에 실패했습니다.";
        return new AppError(response.code(), message);
    }

    // IOException(네트워크 자체 실패) 상황을 위한 공통 에러를 만든다
    private AppError networkError() {
        return new AppError(0, "네트워크 오류가 발생했습니다.");
    }
}
