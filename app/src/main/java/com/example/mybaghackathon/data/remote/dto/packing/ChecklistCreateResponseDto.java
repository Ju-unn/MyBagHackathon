package com.example.mybaghackathon.data.remote.dto.packing;

import com.google.gson.annotations.SerializedName;

// POST /api/checklist/create.php 응답 — { "packing_item_id": 30 }
public class ChecklistCreateResponseDto {

    @SerializedName("packing_item_id")
    private long packingItemId;

    public long getPackingItemId() {
        return packingItemId;
    }
}
