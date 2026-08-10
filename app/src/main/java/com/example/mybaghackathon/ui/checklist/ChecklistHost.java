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

    /** 공용 물품 1개를 여러 멤버에게 동시 배정 — 방장 전용(서버 검증). */
    void assignChecklistItems(PackingItem item, List<Long> userIds);

    /** 이미 여러 명에게 배정된 공용 물품 그룹의 배정을 새 선택 목록으로 재조정 — 방장 전용. */
    void reassignGroupedItem(List<PackingItem> groupItems, List<Long> userIds);

    void deleteChecklistItemWithUndo(PackingItem item);

    /** 실행취소 안내 없이 즉시 삭제 — 방장이 공용 물품을 지울 때 사용. */
    void deleteChecklistItem(PackingItem item);
}
