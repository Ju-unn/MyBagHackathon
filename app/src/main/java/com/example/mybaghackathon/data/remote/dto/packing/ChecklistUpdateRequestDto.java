package com.example.mybaghackathon.data.remote.dto.packing;

import com.google.gson.annotations.SerializedName;

/**
 * POST /api/checklist/update.php 요청 바디 — { "item_id": 30, "item_name": "..." }
 *
 * ConfirmRequestDto와 같은 방식: 안 고친 필드는 setter를 호출하지 않으면 null로 남고,
 * Gson 기본 설정상 JSON에서 아예 빠져 서버가 기존 값을 그대로 유지한다.
 */
public class ChecklistUpdateRequestDto {

    @SerializedName("item_id")
    private final long itemId;

    @SerializedName("item_name")
    private String itemName;

    @SerializedName("category")
    private String category;

    @SerializedName("priority")
    private String priority;

    @SerializedName("scope")
    private String scope;

    public ChecklistUpdateRequestDto(long itemId) {
        this.itemId = itemId;
    }

    public ChecklistUpdateRequestDto setItemName(String itemName) {
        this.itemName = itemName;
        return this;
    }

    public ChecklistUpdateRequestDto setCategory(String category) {
        this.category = category;
        return this;
    }

    public ChecklistUpdateRequestDto setPriority(String priority) {
        this.priority = priority;
        return this;
    }

    public ChecklistUpdateRequestDto setScope(String scope) {
        this.scope = scope;
        return this;
    }
}
