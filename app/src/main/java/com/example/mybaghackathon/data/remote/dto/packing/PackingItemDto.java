package com.example.mybaghackathon.data.remote.dto.packing;

import com.google.gson.annotations.SerializedName;

/**
 * analyze.php/confirm.php 응답의 recommended_items 항목 (JSON 예시)
 *
 * { "item_name": "여권", "category": "여행서류", "priority": "REQUIRED" }
 *
 * Phase 5 체크리스트 API(packing_items 실제 CRUD)가 구현되면 scope/assignee 등
 * 나머지 필드가 이 DTO에 추가될 수 있음 — 지금은 GPT 분석 단계에서 오는 3개 필드만 있음.
 */
public class PackingItemDto {

    @SerializedName("item_name")
    private String itemName;

    private String category;

    // REQUIRED, RECOMMENDED, OPTIONAL 중 하나
    private String priority;

    public String getItemName() {
        return itemName;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }
}
