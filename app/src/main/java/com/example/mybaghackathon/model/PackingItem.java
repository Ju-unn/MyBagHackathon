package com.example.mybaghackathon.model;

import java.io.Serializable;

/**
 * 여행 체크리스트의 준비물 한 건을 나타내는 앱 내부 모델입니다.
 */
public class PackingItem implements Serializable {

    private long packingItemId;
    private long tripId;
    private long createdByUserId;
    private String itemName;
    private String category;
    private String priority;
    private String scope;
    private String source;
    private String restrictionType;
    private String restrictionReason;
    // 담당자 user_id는 화면에서 이미 들고 있는 멤버 목록과 대조해 TripMember를 구한다.
    private Long assigneeUserId;
    private boolean completed;
    private String completedAt;
    // ACTIVE, EXCLUDED, DELETED 중 하나 — 델타 조회(since)에서 삭제 여부 판단에 씀
    private String itemStatus;
    private int sortOrder;

    public PackingItem() {
    }

    public PackingItem(
            long packingItemId,
            long tripId,
            long createdByUserId,
            String itemName,
            String category,
            String priority,
            String scope,
            String source,
            String restrictionType,
            String restrictionReason,
            Long assigneeUserId,
            boolean completed,
            String completedAt,
            String itemStatus,
            int sortOrder
    ) {
        this.packingItemId = packingItemId;
        this.tripId = tripId;
        this.createdByUserId = createdByUserId;
        this.itemName = itemName;
        this.category = category;
        this.priority = priority;
        this.scope = scope;
        this.source = source;
        this.restrictionType = restrictionType;
        this.restrictionReason = restrictionReason;
        this.assigneeUserId = assigneeUserId;
        this.completed = completed;
        this.completedAt = completedAt;
        this.itemStatus = itemStatus;
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

    public long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(long createdByUserId) {
        this.createdByUserId = createdByUserId;
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

    public Long getAssigneeUserId() {
        return assigneeUserId;
    }

    public void setAssigneeUserId(Long assigneeUserId) {
        this.assigneeUserId = assigneeUserId;
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

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
