package com.example.mybaghackathon.data.remote.dto.analysis;

import com.google.gson.annotations.SerializedName;

/**
 * analyze.php/confirm.php 응답의 restricted_items 항목 (JSON 예시)
 *
 * { "item_name": "보조배터리", "restriction_type": "CARRY_ON_ONLY", "reason": "..." }
 */
public class RestrictedItemDto {

    @SerializedName("item_name")
    private String itemName;

    @SerializedName("restriction_type")
    private String restrictionType;

    private String reason;

    public String getItemName() {
        return itemName;
    }

    public String getRestrictionType() {
        return restrictionType;
    }

    public String getReason() {
        return reason;
    }
}
