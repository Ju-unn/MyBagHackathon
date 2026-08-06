package com.example.mybaghackathon.data.remote.dto.trip;

import com.google.gson.annotations.SerializedName;

// POST /api/trips/join.php 요청 바디 — { "invite_code": "EB34EE98" }
public class TripJoinRequestDto {

    @SerializedName("invite_code")
    private final String inviteCode;

    public TripJoinRequestDto(String inviteCode) {
        this.inviteCode = inviteCode;
    }
}
