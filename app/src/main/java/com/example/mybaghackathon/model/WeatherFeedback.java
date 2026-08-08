package com.example.mybaghackathon.model;

// 여행 목적지·날씨 기반 옷차림/음식/숙소 GPT 조언 (WeatherFeedbackDto 1:1 대응)
// 지역·기온·기간·일별예보는 이 API가 아니라 trips/detail.php + weather/forecast.php에서 가져올 것
public class WeatherFeedback {

    private final boolean ready;
    private final String nextRefreshAt;
    private final String clothing;
    private final String food;
    private final String accommodationNotes;

    public WeatherFeedback(boolean ready, String nextRefreshAt, String clothing, String food, String accommodationNotes) {
        this.ready = ready;
        this.nextRefreshAt = nextRefreshAt;
        this.clothing = clothing;
        this.food = food;
        this.accommodationNotes = accommodationNotes;
    }

    // false면 아직 체크포인트 갱신 전 — clothing/food/accommodationNotes는 비어있으니 쓰지 말 것
    public boolean isReady() {
        return ready;
    }

    // 다음 자동 갱신 예정일(yyyy-MM-dd) 또는 null (ready=true면 항상 null)
    public String getNextRefreshAt() {
        return nextRefreshAt;
    }

    public String getClothing() {
        return clothing;
    }

    public String getFood() {
        return food;
    }

    public String getAccommodationNotes() {
        return accommodationNotes;
    }
}
