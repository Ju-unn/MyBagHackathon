package com.example.mybaghackathon.ui.checklist;

import com.example.mybaghackathon.model.PackingItem;

import java.util.List;

/**
 * Calculates progress by counting every active common(공용) checklist row individually — AI
 * recommendations the host kept, and items added directly to the common list. Multi-assignee
 * items contribute one row per assignee (matching the 분담 현황 tab, where each assignee sees
 * their own row), so a 2-person assignment counts as 2 toward the total rather than 1.
 */
public final class ChecklistProgressCalculator {

    private ChecklistProgressCalculator() {
    }

    public static Progress calculate(List<PackingItem> items) {
        int total = 0;
        int completed = 0;
        if (items != null) {
            for (PackingItem item : items) {
                if (!isActiveCommonItem(item)) {
                    continue;
                }
                total++;
                if (item.isCompleted()) {
                    completed++;
                }
            }
        }
        return new Progress(completed, total);
    }

    // 1인 방은 공용 리스트·분담 현황 탭 자체가 없고 "내 목록"만 보여준다. 그런데 방 생성 시점의
    // AI 추천만 COMMON으로 저장되고, 그 뒤에 "내 목록"에서 직접 추가한 물품은 전부 PERSONAL이라
    // calculate()(COMMON 기준)로는 나중에 추가한 물품이 진행률에 안 잡힌다. 그래서 1인 방은
    // 화면에 실제로 보이는 "내 목록" 병합 결과(ChecklistDuplicateDetector)를 그대로 센다.
    public static Progress calculateForMine(List<PackingItem> items, long currentUserId) {
        List<ChecklistDuplicateDetector.ItemGroup> groups =
                ChecklistDuplicateDetector.groupForMine(items, currentUserId, true);
        int completed = 0;
        for (ChecklistDuplicateDetector.ItemGroup group : groups) {
            if (group.getCompletionItem().isCompleted()) {
                completed++;
            }
        }
        return new Progress(completed, groups.size());
    }

    private static boolean isActiveCommonItem(PackingItem item) {
        return item != null
                && ChecklistItemVisibility.isCommon(item)
                && !"EXCLUDED".equalsIgnoreCase(item.getItemStatus())
                && !"DELETED".equalsIgnoreCase(item.getItemStatus());
    }

    public static final class Progress {
        final int completed;
        final int total;

        Progress(int completed, int total) {
            this.completed = completed;
            this.total = total;
        }

        public int percent() {
            return total == 0 ? 0 : Math.round(completed * 100f / total);
        }
    }
}
