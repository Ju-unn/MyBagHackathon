package com.example.mybaghackathon.ui.checklist;

import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.TripMember;

import java.util.List;

/** 체크리스트 Fragment가 Activity의 공통 데이터와 서버 작업을 사용하는 통로. */
public interface ChecklistHost {

    List<PackingItem> getChecklistItems();

    List<TripMember> getTripMembers();

    long getCurrentUserId();

    boolean isCurrentUserHost();

    int getTripMemberCount();

    boolean isChecklistLoaded();

    void refreshChecklist();

    void addChecklistItem(String name, int priorityLevel, String scope);

    void updateChecklistItem(PackingItem item, String name, int priorityLevel);

    void toggleChecklistItem(PackingItem item);

    void assignChecklistItem(PackingItem item, Long userId);

    /** 서버 다중 배정 API 연결 전까지 담당자 지정 오버레이의 선택 상태를 보관한다. */
    List<Long> getDraftAssigneeIds(long itemId);

    boolean hasDraftAssigneeIds(long itemId);

    /** API 요청 없이 현재 체크리스트 화면 안에서만 복수 담당자 선택을 저장한다. */
    void saveDraftAssigneeIds(long itemId, List<Long> userIds);

    void deleteChecklistItemWithUndo(PackingItem item);
}
