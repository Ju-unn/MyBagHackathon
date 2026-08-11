package com.example.mybaghackathon.data.remote.dto.packing;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * POST /api/checklist/assign.php 요청 바디.
 * - 단일: { "item_id": 30, "assignee_user_id": 1 } — assigneeUserId가 null이면 Gson이 키를
 *   아예 빼고, 서버는 이를 "해제"로 처리한다. 본인만 지정/해제 가능(서버 검증).
 * - 다중 동기화(방장 전용): { "item_group_id": 30, "assignee_user_ids": [1, 2] } — 서버가
 *   그룹(원본+복제 row) 전체를 하나의 트랜잭션으로 이 최종 목록에 맞춰 동기화한다(멱등,
 *   빈 배열이면 전원 해제). 응답 후엔 체크리스트를 다시 조회해야 한다.
 */
public class ChecklistAssignRequestDto {

    @SerializedName("item_id")
    private final Long itemId;

    @SerializedName("item_group_id")
    private final Long itemGroupId;

    @SerializedName("assignee_user_id")
    private final Long assigneeUserId;

    @SerializedName("assignee_user_ids")
    private final List<Long> assigneeUserIds;

    public ChecklistAssignRequestDto(long itemId, Long assigneeUserId) {
        this.itemId = itemId;
        this.itemGroupId = null;
        this.assigneeUserId = assigneeUserId;
        this.assigneeUserIds = null;
    }

    public ChecklistAssignRequestDto(long itemGroupId, List<Long> assigneeUserIds) {
        this.itemId = null;
        this.itemGroupId = itemGroupId;
        this.assigneeUserId = null;
        this.assigneeUserIds = assigneeUserIds;
    }
}
