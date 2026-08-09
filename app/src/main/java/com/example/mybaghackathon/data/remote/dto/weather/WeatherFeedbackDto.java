package com.example.mybaghackathon.data.remote.dto.weather;

import com.google.gson.annotations.SerializedName;

/**
 * GET /api/weather/feedback.php 응답의 data 부분 (JSON 예시)
 *
 * {
 *   "clothing": "오사카는 기간 내내 최고기온이 29~30도 안팎이고...",
 *   "food": "비 오는 날에는 도톤보리나 우메다 같은 번화가의...",
 *   "accommodation_notes": "비가 오는 날에는 역에서 숙소까지 이동 동선이..."
 * }
 */
public class WeatherFeedbackDto {

    // 아직 체크포인트 갱신 전이면 false — 이때 clothing/food/accommodation_notes는 안 옴
    private boolean ready;

    // 다음 자동 갱신 예정일(yyyy-MM-dd) 또는 null
    @SerializedName("next_refresh_at")
    private String nextRefreshAt;

    // 옷차림 조언
    private String clothing;

    // 음식·식당 관련 참고할 점
    private String food;

    // 숙소 이용 시 유의사항
    @SerializedName("accommodation_notes")
    private String accommodationNotes;

    public boolean isReady() {
        return ready;
    }

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
