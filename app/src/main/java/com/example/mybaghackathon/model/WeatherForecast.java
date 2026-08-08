package com.example.mybaghackathon.model;

import java.util.List;

// trip_id 기반 예보 조회 결과 (WeatherForecastDto 1:1 대응). ready=false면 체크포인트 갱신 전이라 days가 비어있다
public class WeatherForecast {

    private final boolean ready;
    private final String nextRefreshAt;
    private final List<Weather> days;

    public WeatherForecast(boolean ready, String nextRefreshAt, List<Weather> days) {
        this.ready = ready;
        this.nextRefreshAt = nextRefreshAt;
        this.days = days;
    }

    // false면 아직 체크포인트 갱신 전 — days는 비어있으니 쓰지 말 것
    public boolean isReady() {
        return ready;
    }

    // 다음 자동 갱신 예정일(yyyy-MM-dd) 또는 null (ready=true면 항상 null)
    public String getNextRefreshAt() {
        return nextRefreshAt;
    }

    public List<Weather> getDays() {
        return days;
    }
}
