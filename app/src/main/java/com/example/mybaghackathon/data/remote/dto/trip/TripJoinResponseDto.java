package com.example.mybaghackathon.data.remote.dto.trip;

import com.google.gson.annotations.SerializedName;

// POST /api/trips/join.php 응답 — { "trip_id": 1 }
public class TripJoinResponseDto {

    @SerializedName("trip_id")
    private long tripId;

    public long getTripId() {
        return tripId;
    }
}
