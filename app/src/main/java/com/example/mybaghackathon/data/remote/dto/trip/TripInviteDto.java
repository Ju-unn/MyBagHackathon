package com.example.mybaghackathon.data.remote.dto.trip;

import com.google.gson.annotations.SerializedName;

// POST /api/trips/create.php 응답 — { "trip_id": 1, "invite_code": "EB34EE98" }
public class TripInviteDto {

    @SerializedName("trip_id")
    private long tripId;

    @SerializedName("invite_code")
    private String inviteCode;

    public long getTripId() {
        return tripId;
    }

    public String getInviteCode() {
        return inviteCode;
    }
}
