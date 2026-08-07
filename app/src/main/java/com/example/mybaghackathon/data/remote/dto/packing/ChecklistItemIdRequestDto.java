package com.example.mybaghackathon.data.remote.dto.packing;

import com.google.gson.annotations.SerializedName;

// { "item_id": 30 } 형태만 필요한 요청 바디 — check.php/delete.php 공용
public class ChecklistItemIdRequestDto {

    @SerializedName("item_id")
    private final long itemId;

    public ChecklistItemIdRequestDto(long itemId) {
        this.itemId = itemId;
    }
}
