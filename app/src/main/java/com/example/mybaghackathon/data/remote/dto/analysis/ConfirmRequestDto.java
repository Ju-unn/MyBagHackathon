package com.example.mybaghackathon.data.remote.dto.analysis;

import com.google.gson.annotations.SerializedName;

/**
 * POST /api/itinerary/confirm.php 요청 바디 — { "analysis_id": 1, "destination_city": "도쿄" }
 *
 * S07에서 사용자가 고친 필드만 채우면 됨. null인 필드는 Gson 기본 설정상 JSON에서
 * 아예 빠지므로, 서버는 그 필드를 기존 분석 결과 그대로 유지한다 (수정 안 한 것으로 처리).
 */
public class ConfirmRequestDto {

    @SerializedName("analysis_id")
    private final long analysisId;

    @SerializedName("destination_country")
    private String destinationCountry;

    @SerializedName("destination_city")
    private String destinationCity;

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("end_date")
    private String endDate;

    @SerializedName("transport_mode")
    private String transportMode;

    @SerializedName("accommodation_name")
    private String accommodationName;

    public ConfirmRequestDto(long analysisId) {
        this.analysisId = analysisId;
    }

    public ConfirmRequestDto setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
        return this;
    }

    public ConfirmRequestDto setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
        return this;
    }

    public ConfirmRequestDto setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public ConfirmRequestDto setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }

    public ConfirmRequestDto setTransportMode(String transportMode) {
        this.transportMode = transportMode;
        return this;
    }

    public ConfirmRequestDto setAccommodationName(String accommodationName) {
        this.accommodationName = accommodationName;
        return this;
    }
}
