package com.example.mybaghackathon.data.remote.dto.trip;

import com.google.gson.annotations.SerializedName;

// trips 테이블 1건 응답. list.php/detail.php의 trip(들) 필드에 대응
public class TripDto {

    @SerializedName("trip_id")
    private long tripId;

    @SerializedName("owner_user_id")
    private long ownerUserId;

    @SerializedName("trip_name")
    private String tripName;

    @SerializedName("expected_member_count")
    private Integer expectedMemberCount;

    @SerializedName("trip_type")
    private String tripType;

    @SerializedName("destination_country")
    private String destinationCountry;

    @SerializedName("destination_city")
    private String destinationCity;

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("end_date")
    private String endDate;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    public long getTripId() {
        return tripId;
    }

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public String getTripName() {
        return tripName;
    }

    public Integer getExpectedMemberCount() {
        return expectedMemberCount;
    }

    public String getTripType() {
        return tripType;
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

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
