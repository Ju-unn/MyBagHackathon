package com.example.mybaghackathon.data.remote.dto.trip;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// GET /api/trips/members.php 응답 — { "members": [...] }
public class TripMembersResponseDto {

    @SerializedName("members")
    private List<TripMemberDto> members;

    public List<TripMemberDto> getMembers() {
        return members;
    }
}
