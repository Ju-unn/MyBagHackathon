package com.example.mybaghackathon.data.remote.dto.trip;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// GET /api/trips/list.php, /api/trips/archive.php 응답 — { "trips": [...] }
public class TripListResponseDto {

    @SerializedName("trips")
    private List<TripDto> trips;

    public List<TripDto> getTrips() {
        return trips;
    }
}
