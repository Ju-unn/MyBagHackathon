package com.example.mybaghackathon.data.remote.dto.packing;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * POST /api/checklist/assign.php 요청 바디.
 * - 단일: { "item_id": 30, "assignee_user_id": 1 } — assigneeUserId가 null이면 Gson이 키를
 *   아예 빼고, 서버는 이를 "해제"로 처리한다. 본인만 지정/해제 가능(서버 검증).
 * - 다중(방장 전용): { "item_id": 30, "assignee_user_ids": [1, 2] } — 공용 물품 1개를 여러
 *   멤버에게 한 번에 배정. 서버가 원본 row는 첫 번째 유저에게 배정하고 나머지는 row를
 *   복제해서 배정하므로, 응답 후엔 반드시 체크리스트를 다시 조회해야 한다.
 */
public class ChecklistAssignRequestDto {

    @SerializedName("item_id")
    private final long itemId;

    @SerializedName("assignee_user_id")
    private final Long assigneeUserId;

    @SerializedName("assignee_user_ids")
    private final List<Long> assigneeUserIds;

    public ChecklistAssignRequestDto(long itemId, Long assigneeUserId) {
        this.itemId = itemId;
        this.assigneeUserId = assigneeUserId;
        this.assigneeUserIds = null;
    }

    public ChecklistAssignRequestDto(long itemId, List<Long> assigneeUserIds) {
        this.itemId = itemId;
        this.assigneeUserId = null;
        this.assigneeUserIds = assigneeUserIds;
    }
}
