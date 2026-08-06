package com.example.mybaghackathon.model;

/**
 * 프로필 "내 기본 물품" 한 건을 나타내는 앱 내부 모델입니다.
 */
public class UserDefaultItem {

    private long defaultItemId;
    private String itemName;
    private String category;
    private String priority;

    public UserDefaultItem() {
    }

    public UserDefaultItem(long defaultItemId, String itemName, String category, String priority) {
        this.defaultItemId = defaultItemId;
        this.itemName = itemName;
        this.category = category;
        this.priority = priority;
    }

    public long getDefaultItemId() {
        return defaultItemId;
    }

    public void setDefaultItemId(long defaultItemId) {
        this.defaultItemId = defaultItemId;
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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
