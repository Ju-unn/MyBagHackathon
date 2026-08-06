package com.example.mybaghackathon.data.remote.dto.packing;

import com.google.gson.annotations.SerializedName;

// POST /api/checklist/create.php 요청 바디 (BS02 항목 직접 추가) — { "trip_id", "item_name", "category", "priority", "scope" }
public class ChecklistCreateRequestDto {

    @SerializedName("trip_id")
    private final long tripId;

    @SerializedName("item_name")
    private final String itemName;

    @SerializedName("category")
    private final String category;

    @SerializedName("priority")
    private final String priority;

    @SerializedName("scope")
    private final String scope;

    public ChecklistCreateRequestDto(long tripId, String itemName, String category, String priority, String scope) {
        this.tripId = tripId;
        this.itemName = itemName;
        this.category = category;
        this.priority = priority;
        this.scope = scope;
    }
}
