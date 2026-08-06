package com.example.mybaghackathon.data.remote.dto.upload;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// POST /api/itinerary/upload.php 응답 — { "upload_ids": [1, 2] }
public class TripUploadDto {

    @SerializedName("upload_ids")
    private List<Long> uploadIds;

    public List<Long> getUploadIds() {
        return uploadIds;
    }
}
