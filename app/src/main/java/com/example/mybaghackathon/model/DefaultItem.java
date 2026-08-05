package com.example.mybaghackathon.model;

/**
 * 사용자가 프로필에서 관리하고 새 여행에 재사용하는 기본 준비물 모델입니다.
 */
public class DefaultItem {

    private long defaultItemId;
    private long userId;
    private String itemName;
    private String category;
    private boolean active;

    public DefaultItem() {
    }

    public DefaultItem(
            long defaultItemId,
            long userId,
            String itemName,
            String category,
            boolean active
    ) {
        this.defaultItemId = defaultItemId;
        this.userId = userId;
        this.itemName = itemName;
        this.category = category;
        this.active = active;
    }

    public long getDefaultItemId() {
        return defaultItemId;
    }

    public void setDefaultItemId(long defaultItemId) {
        this.defaultItemId = defaultItemId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
