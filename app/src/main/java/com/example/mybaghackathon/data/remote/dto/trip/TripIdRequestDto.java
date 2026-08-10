package com.example.mybaghackathon.data.remote.dto.trip;

import com.google.gson.annotations.SerializedName;

// { "trip_id": 2 } 형태만 필요한 요청 바디 — delete.php/leave.php 공용
public class TripIdRequestDto {

    @SerializedName("trip_id")
    private final long tripId;

    public TripIdRequestDto(long tripId) {
        this.tripId = tripId;
    }
}
