package com.example.mybaghackathon.data.remote.dto.defaultitem;

import com.google.gson.annotations.SerializedName;

// { "default_item_id": 12 } 형태만 필요한 요청 바디 — delete.php 전용
public class DefaultItemIdRequestDto {

    @SerializedName("default_item_id")
    private final long defaultItemId;

    public DefaultItemIdRequestDto(long defaultItemId) {
        this.defaultItemId = defaultItemId;
    }
}
