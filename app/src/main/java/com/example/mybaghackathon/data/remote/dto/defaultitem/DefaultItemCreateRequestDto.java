package com.example.mybaghackathon.data.remote.dto.defaultitem;

import com.google.gson.annotations.SerializedName;

// POST /api/profile/default-items/create.php 요청 바디 — { "item_name", "category", "default_priority" }
public class DefaultItemCreateRequestDto {

    @SerializedName("item_name")
    private final String itemName;

    @SerializedName("category")
    private final String category;

    @SerializedName("default_priority")
    private final String defaultPriority;

    public DefaultItemCreateRequestDto(String itemName, String category, String defaultPriority) {
        this.itemName = itemName;
        this.category = category;
        this.defaultPriority = defaultPriority;
    }
}
