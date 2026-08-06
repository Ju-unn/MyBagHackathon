package com.example.mybaghackathon.data.remote.dto.analysis;

import com.example.mybaghackathon.data.remote.dto.packing.PackingItemDto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * analyze.php / confirm.php 응답 (두 엔드포인트가 같은 모양을 돌려줌, JSON 예시)
 *
 * {
 *   "analysis_id": 1,
 *   "destination_country": "일본", "destination_city": "오사카",
 *   "start_date": "2026-08-28", "end_date": "2026-08-31",
 *   "is_overseas": true, "transport_mode": "AIR", "accommodation_name": null,
 *   "restricted_items": [{ "item_name": "보조배터리", "restriction_type": "CARRY_ON_ONLY", "reason": "..." }],
 *   "recommended_items": [{ "item_name": "여권", "category": "여행서류", "priority": "REQUIRED" }]
 * }
 */
public class AnalysisResponseDto {

    @SerializedName("analysis_id")
    private long analysisId;

    @SerializedName("destination_country")
    private String destinationCountry;

    @SerializedName("destination_city")
    private String destinationCity;

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("end_date")
    private String endDate;

    @SerializedName("is_overseas")
    private Boolean isOverseas;

    @SerializedName("transport_mode")
    private String transportMode;

    @SerializedName("accommodation_name")
    private String accommodationName;

    @SerializedName("restricted_items")
    private List<RestrictedItemDto> restrictedItems;

    @SerializedName("recommended_items")
    private List<PackingItemDto> recommendedItems;

    public long getAnalysisId() {
        return analysisId;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public Boolean getIsOverseas() {
        return isOverseas;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public String getAccommodationName() {
        return accommodationName;
    }

    public List<RestrictedItemDto> getRestrictedItems() {
        return restrictedItems;
    }

    public List<PackingItemDto> getRecommendedItems() {
        return recommendedItems;
    }
}
