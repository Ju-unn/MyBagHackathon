package com.example.mybaghackathon.data.remote.dto.trip;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * POST /api/trips/create.php 요청 바디 (S08 '방 생성 완료')
 *
 * {
 *   "trip_name": "오사카 여름 여행", "expected_member_count": 3, "analysis_id": 2,
 *   "items": [{ "item_name": "여권", "scope": "COMMON" }, { "item_name": "우산", "scope": "PERSONAL" }]
 * }
 */
public class TripCreateRequestDto {

    @SerializedName("trip_name")
    private final String tripName;

    @SerializedName("expected_member_count")
    private final int expectedMemberCount;

    @SerializedName("analysis_id")
    private final long analysisId;

    @SerializedName("items")
    private final List<Item> items;

    public TripCreateRequestDto(String tripName, int expectedMemberCount, long analysisId, List<Item> items) {
        this.tripName = tripName;
        this.expectedMemberCount = expectedMemberCount;
        this.analysisId = analysisId;
        this.items = items;
    }

    // S08 Scope 토글 — item_name당 COMMON|PERSONAL
    public static class Item {

        @SerializedName("item_name")
        private final String itemName;

        @SerializedName("scope")
        private final String scope;

        public Item(String itemName, String scope) {
            this.itemName = itemName;
            this.scope = scope;
        }
    }
}
