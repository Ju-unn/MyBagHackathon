package com.example.mybaghackathon.ui.checklist;

import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.TripMember;

import java.util.List;

/** 체크리스트 Fragment가 Activity의 공통 데이터와 서버 작업을 사용하는 통로. */
public interface ChecklistHost {

    List<PackingItem> getChecklistItems();

    List<TripMember> getTripMembers();

    long getCurrentUserId();

    int getTripMemberCount();

    boolean isChecklistLoaded();

    void refreshChecklist();

    void addChecklistItem(String name, int priorityLevel, String scope);

    void updateChecklistItem(PackingItem item, String name, int priorityLevel);

    void toggleChecklistItem(PackingItem item);

    void assignChecklistItem(PackingItem item, Long userId);

    void deleteChecklistItemWithUndo(PackingItem item);
}
