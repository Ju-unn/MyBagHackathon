package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.model.Weather;
import com.example.mybaghackathon.model.WeatherFeedback;

import java.util.List;

// 날씨 관련 데이터 접근 규칙을 정의하는 인터페이스
public interface WeatherRepository {

    // 도시명 + 기간(yyyy-MM-dd)으로 일별 예보를 조회한다
    AppResult<List<Weather>> getForecast(String city, String startDate, String endDate);

    // 여행방 목적지·기간·날씨 기반 옷차림/음식/숙소 GPT 조언을 조회한다
    AppResult<WeatherFeedback> getFeedback(long tripId);
}
