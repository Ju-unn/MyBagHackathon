package com.example.mybaghackathon.data.remote.dto.defaultitem;

import com.google.gson.annotations.SerializedName;

// POST /api/profile/default-items/create.php 응답 — { "default_item_id": 12 }
public class DefaultItemCreateResponseDto {

    @SerializedName("default_item_id")
    private long defaultItemId;

    public long getDefaultItemId() {
        return defaultItemId;
    }
}
