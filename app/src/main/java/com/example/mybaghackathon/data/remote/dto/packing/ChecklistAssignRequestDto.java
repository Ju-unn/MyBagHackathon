package com.example.mybaghackathon.data.remote.dto.packing;

import com.google.gson.annotations.SerializedName;

/**
 * POST /api/checklist/assign.php 요청 바디 — { "item_id": 30, "assignee_user_id": 1 }
 *
 * assigneeUserId가 null이면 Gson 기본 설정상 JSON에서 키가 아예 빠지고, 서버는 이를
 * "해제"로 처리한다. 본인만 지정/해제할 수 있다는 규칙은 서버에서 검증한다.
 */
public class ChecklistAssignRequestDto {

    @SerializedName("item_id")
    private final long itemId;

    @SerializedName("assignee_user_id")
    private final Long assigneeUserId;

    public ChecklistAssignRequestDto(long itemId, Long assigneeUserId) {
        this.itemId = itemId;
        this.assigneeUserId = assigneeUserId;
    }
}
