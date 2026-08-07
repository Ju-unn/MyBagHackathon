package com.example.mybaghackathon.data.remote.dto.packing;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// GET /api/checklist/list.php 응답 — { "items": [...], "server_time": "..." }
public class ChecklistListResponseDto {

    @SerializedName("items")
    private List<ChecklistItemDto> items;

    @SerializedName("server_time")
    private String serverTime;

    public List<ChecklistItemDto> getItems() {
        return items;
    }

    // 다음 델타 조회(since)에 넘길 서버 기준 시각 — 기기 시각 사용 금지
    public String getServerTime() {
        return serverTime;
    }
}
