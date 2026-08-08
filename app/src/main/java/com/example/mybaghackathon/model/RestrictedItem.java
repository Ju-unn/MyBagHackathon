package com.example.mybaghackathon.model;

import java.io.Serializable;

/**
 * 목적지 반입 규정에 걸리는 품목 한 건을 나타내는 모델입니다. (analyze.php/confirm.php 응답의
 * restricted_items 대응)
 */
public class RestrictedItem implements Serializable {

    private String itemName;
    private String restrictionType;
    private String reason;

    public RestrictedItem() {
    }

    public RestrictedItem(String itemName, String restrictionType, String reason) {
        this.itemName = itemName;
        this.restrictionType = restrictionType;
        this.reason = reason;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    // PROHIBITED, CARRY_ON_ONLY, CHECKED_ONLY, LIMITED, CAUTION 중 하나
    public String getRestrictionType() {
        return restrictionType;
    }

    public void setRestrictionType(String restrictionType) {
        this.restrictionType = restrictionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
