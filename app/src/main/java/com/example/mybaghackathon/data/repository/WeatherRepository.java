package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.model.Weather;

import java.util.List;

// 날씨 관련 데이터 접근 규칙을 정의하는 인터페이스
public interface WeatherRepository {

    // 도시명 + 기간(yyyy-MM-dd)으로 일별 예보를 조회한다
    AppResult<List<Weather>> getForecast(String city, String startDate, String endDate);
}
