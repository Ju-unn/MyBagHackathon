package com.example.mybaghackathon.data.remote.dto.packing;

import com.google.gson.annotations.SerializedName;

// POST /api/checklist/check.php 응답 — { "is_completed": true }
public class ChecklistCheckResponseDto {

    @SerializedName("is_completed")
    private boolean completed;

    public boolean isCompleted() {
        return completed;
    }
}
