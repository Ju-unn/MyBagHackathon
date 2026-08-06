package com.example.mybaghackathon.data.remote.dto.packing;

import com.google.gson.annotations.SerializedName;

/**
 * packing_items 테이블 1건 응답 (checklist/list.php의 items 배열 요소).
 * GPT 분석 단계에서만 쓰는 PackingItemDto(3필드)와 달리, 체크리스트 CRUD API가 돌려주는
 * 전체 필드(scope/assignee/completed 등)를 담는다.
 */
public class ChecklistItemDto {

    @SerializedName("packing_item_id")
    private long packingItemId;

    @SerializedName("trip_id")
    private long tripId;

    @SerializedName("item_name")
    private String itemName;

    private String category;

    // REQUIRED, RECOMMENDED, OPTIONAL 중 하나
    private String priority;

    @SerializedName("item_scope")
    private String itemScope;

    // AI, USER 중 하나
    private String source;

    @SerializedName("restriction_type")
    private String restrictionType;

    @SerializedName("restriction_reason")
    private String restrictionReason;

    // 미배정이면 null
    @SerializedName("assignee_user_id")
    private Long assigneeUserId;

    @SerializedName("is_completed")
    private boolean completed;

    @SerializedName("completed_at")
    private String completedAt;

    // ACTIVE, EXCLUDED, DELETED 중 하나
    @SerializedName("item_status")
    private String itemStatus;

    @SerializedName("sort_order")
    private int sortOrder;

    public long getPackingItemId() {
        return packingItemId;
    }

    public long getTripId() {
        return tripId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    public String getItemScope() {
        return itemScope;
    }

    public String getSource() {
        return source;
    }

    public String getRestrictionType() {
        return restrictionType;
    }

    public String getRestrictionReason() {
        return restrictionReason;
    }

    public Long getAssigneeUserId() {
        return assigneeUserId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
