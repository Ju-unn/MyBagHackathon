package com.example.mybaghackathon.data.remote.dto.defaultitem;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// GET /api/profile/default-items/list.php 응답 — { "items": [...] }
public class DefaultItemListResponseDto {

    @SerializedName("items")
    private List<DefaultItemDto> items;

    public List<DefaultItemDto> getItems() {
        return items;
    }
}
