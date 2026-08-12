package com.example.mybaghackathon.ui.checklist;

import com.example.mybaghackathon.model.PackingItem;

import java.util.List;

/**
 * Calculates progress by counting every active common(공용) checklist row individually — AI
 * recommendations the host kept, and items added directly to the common list. Multi-assignee
 * items contribute one row per assignee (matching the 분담 현황 tab, where each assignee sees
 * their own row), so a 2-person assignment counts as 2 toward the total rather than 1.
 */
final class ChecklistProgressCalculator {

    private ChecklistProgressCalculator() {
    }

    static Progress calculate(List<PackingItem> items) {
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

    private static boolean isActiveCommonItem(PackingItem item) {
        return item != null
                && ChecklistItemVisibility.isCommon(item)
                && !"EXCLUDED".equalsIgnoreCase(item.getItemStatus())
                && !"DELETED".equalsIgnoreCase(item.getItemStatus());
    }

    static final class Progress {
        final int completed;
        final int total;

        Progress(int completed, int total) {
            this.completed = completed;
            this.total = total;
        }

        int percent() {
            return total == 0 ? 0 : Math.round(completed * 100f / total);
        }
    }
}
