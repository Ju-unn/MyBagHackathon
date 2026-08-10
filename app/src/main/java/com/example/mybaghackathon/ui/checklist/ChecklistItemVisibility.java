package com.example.mybaghackathon.ui.checklist;

import com.example.mybaghackathon.model.PackingItem;

/** 체크리스트 전체 응답에서 현재 사용자의 내 목록에 보여줄 항목을 판별한다. */
final class ChecklistItemVisibility {

    private ChecklistItemVisibility() {
    }

    static boolean isCommon(PackingItem item) {
        return item != null
                && (item.getScope() == null || "COMMON".equalsIgnoreCase(item.getScope()));
    }

    static boolean isMine(PackingItem item, long currentUserId) {
        if (item == null || currentUserId <= 0L) {
            return false;
        }

        return isAssignedCommon(item, currentUserId)
                || isOwnedPersonal(item, currentUserId);
    }

    static boolean isAssignedCommon(PackingItem item, long currentUserId) {
        return isCommon(item)
                && currentUserId > 0L
                && item.getAssigneeUserId() != null
                && item.getAssigneeUserId() == currentUserId;
    }

    static boolean isOwnedPersonal(PackingItem item, long currentUserId) {
        if (item == null || currentUserId <= 0L
                || !"PERSONAL".equalsIgnoreCase(item.getScope())) {
            return false;
        }

        // 최신 서버는 created_by_user_id를 항상 반환한다. 구버전 응답에서는 값이 0일 수
        // 있으므로 그때만 PERSONAL 생성 시 자동 지정되는 assignee_user_id를 안전한 대체값으로 쓴다.
        return item.getCreatedByUserId() == currentUserId
                || (item.getCreatedByUserId() <= 0L
                && item.getAssigneeUserId() != null
                && item.getAssigneeUserId() == currentUserId);
    }
}
