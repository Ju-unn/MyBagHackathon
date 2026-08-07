package com.example.mybaghackathon.data.remote.dto.packing;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * POST /api/checklist/generate.php 요청 바디 (재분석 후 재생성, 방장 전용)
 *
 * { "trip_id": 1, "analysis_id": 5, "items": [{ "item_name": "여권", "scope": "COMMON" }] }
 * 이전 AI 추천 항목만 정리되고, 사용자가 직접 추가한 항목(source=USER)은 보존된다.
 */
public class ChecklistGenerateRequestDto {

    @SerializedName("trip_id")
    private final long tripId;

    @SerializedName("analysis_id")
    private final long analysisId;

    @SerializedName("items")
    private final List<Item> items;

    public ChecklistGenerateRequestDto(long tripId, long analysisId, List<Item> items) {
        this.tripId = tripId;
        this.analysisId = analysisId;
        this.items = items;
    }

    // S08 Scope 토글과 같은 모양 — item_name당 COMMON|PERSONAL
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
