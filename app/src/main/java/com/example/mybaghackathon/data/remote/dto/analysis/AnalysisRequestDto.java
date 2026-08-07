package com.example.mybaghackathon.data.remote.dto.analysis;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// POST /api/itinerary/analyze.php 요청 바디 — { "upload_ids": [1, 2] }
public class AnalysisRequestDto {

    @SerializedName("upload_ids")
    private final List<Long> uploadIds;

    public AnalysisRequestDto(List<Long> uploadIds) {
        this.uploadIds = uploadIds;
    }
}
