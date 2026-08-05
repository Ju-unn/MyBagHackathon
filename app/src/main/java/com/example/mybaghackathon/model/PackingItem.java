package com.example.mybaghackathon.model;

/**
 * 여행 체크리스트의 준비물 한 건을 나타내는 앱 내부 모델입니다.
 */
public class PackingItem {

    private long packingItemId;
    private long tripId;
    private String itemName;
    private String category;
    private String priority;
    private String scope;
    private String source;
    private String restrictionType;
    private String restrictionReason;
    private TripMember assignee;
    private boolean completed;
    private String completedAt;
    private int sortOrder;

    public PackingItem() {
    }

    public PackingItem(
            long packingItemId,
            long tripId,
            String itemName,
            String category,
            String priority,
            String scope,
            String source,
            String restrictionType,
            String restrictionReason,
            TripMember assignee,
            boolean completed,
            String completedAt,
            int sortOrder
    ) {
        this.packingItemId = packingItemId;
        this.tripId = tripId;
        this.itemName = itemName;
        this.category = category;
        this.priority = priority;
        this.scope = scope;
        this.source = source;
        this.restrictionType = restrictionType;
        this.restrictionReason = restrictionReason;
        this.assignee = assignee;
        this.completed = completed;
        this.completedAt = completedAt;
        this.sortOrder = sortOrder;
    }

    public long getPackingItemId() {
        return packingItemId;
    }

    public void setPackingItemId(long packingItemId) {
        this.packingItemId = packingItemId;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRestrictionType() {
        return restrictionType;
    }

    public void setRestrictionType(String restrictionType) {
        this.restrictionType = restrictionType;
    }

    public String getRestrictionReason() {
        return restrictionReason;
    }

    public void setRestrictionReason(String restrictionReason) {
        this.restrictionReason = restrictionReason;
    }

    public TripMember getAssignee() {
        return assignee;
    }

    public void setAssignee(TripMember assignee) {
        this.assignee = assignee;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
