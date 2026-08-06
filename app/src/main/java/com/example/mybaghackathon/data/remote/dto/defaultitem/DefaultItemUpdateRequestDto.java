package com.example.mybaghackathon.data.remote.dto.defaultitem;

import com.google.gson.annotations.SerializedName;

/**
 * POST /api/profile/default-items/update.php 요청 바디 — { "default_item_id": 12, "item_name": "..." }
 *
 * ChecklistUpdateRequestDto와 같은 방식: 안 고친 필드는 setter를 호출하지 않으면 null로 남고,
 * Gson 기본 설정상 JSON에서 아예 빠져 서버가 기존 값을 그대로 유지한다.
 */
public class DefaultItemUpdateRequestDto {

    @SerializedName("default_item_id")
    private final long defaultItemId;

    @SerializedName("item_name")
    private String itemName;

    @SerializedName("category")
    private String category;

    @SerializedName("default_priority")
    private String defaultPriority;

    public DefaultItemUpdateRequestDto(long defaultItemId) {
        this.defaultItemId = defaultItemId;
    }

    public DefaultItemUpdateRequestDto setItemName(String itemName) {
        this.itemName = itemName;
        return this;
    }

    public DefaultItemUpdateRequestDto setCategory(String category) {
        this.category = category;
        return this;
    }

    public DefaultItemUpdateRequestDto setDefaultPriority(String defaultPriority) {
        this.defaultPriority = defaultPriority;
        return this;
    }
}
