package com.example.mybaghackathon.data.remote.dto.trip;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// GET /api/trips/detail.php 응답 — { "trip": {...}, "members": [...] }
public class TripDetailResponseDto {

    @SerializedName("trip")
    private TripDto trip;

    @SerializedName("members")
    private List<TripMemberDto> members;

    public TripDto getTrip() {
        return trip;
    }

    public List<TripMemberDto> getMembers() {
        return members;
    }
}
