package com.example.mybaghackathon.model;

// 여행 목적지·날씨 기반 옷차림/음식/숙소 GPT 조언 (WeatherFeedbackDto 1:1 대응)
// 지역·기온·기간·일별예보는 이 API가 아니라 trips/detail.php + weather/forecast.php에서 가져올 것
public class WeatherFeedback {

    private final String clothing;
    private final String food;
    private final String accommodationNotes;

    public WeatherFeedback(String clothing, String food, String accommodationNotes) {
        this.clothing = clothing;
        this.food = food;
        this.accommodationNotes = accommodationNotes;
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
