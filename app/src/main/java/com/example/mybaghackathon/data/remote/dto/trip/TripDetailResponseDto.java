package com.example.mybaghackathon.data.remote.dto.trip;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// GET /api/trips/detail.php 응답 — { "trip": {...}, "members": [...], "invite_code": "..." }
public class TripDetailResponseDto {

    @SerializedName("trip")
    private TripDto trip;

    @SerializedName("members")
    private List<TripMemberDto> members;

    @SerializedName("invite_code")
    private String inviteCode;

    public TripDto getTrip() {
        return trip;
    }

    public List<TripMemberDto> getMembers() {
        return members;
    }

    public String getInviteCode() {
        return inviteCode;
    }
}
