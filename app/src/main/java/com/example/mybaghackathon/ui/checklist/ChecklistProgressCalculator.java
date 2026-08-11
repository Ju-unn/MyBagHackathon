package com.example.mybaghackathon.ui.checklist;

import com.example.mybaghackathon.model.PackingItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Calculates progress only from AI items that were generated into the trip checklist. */
final class ChecklistProgressCalculator {

    private ChecklistProgressCalculator() {
    }

    static Progress calculate(List<PackingItem> items) {
        Map<Long, GroupProgress> groups = new LinkedHashMap<>();
        if (items != null) {
            for (PackingItem item : items) {
                if (!isSelectedAiItem(item)) {
                    continue;
                }
                long groupId = item.getItemGroupId();
                GroupProgress group = groups.get(groupId);
                if (group == null) {
                    group = new GroupProgress();
                    groups.put(groupId, group);
                }
                group.hasItem = true;
                group.allCompleted &= item.isCompleted();
            }
        }

        int completed = 0;
        for (GroupProgress group : groups.values()) {
            if (group.hasItem && group.allCompleted) {
                completed++;
            }
        }
        return new Progress(completed, groups.size());
    }

    private static boolean isSelectedAiItem(PackingItem item) {
        return item != null
                && "AI".equalsIgnoreCase(item.getSource())
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

    private static final class GroupProgress {
        private boolean hasItem;
        private boolean allCompleted = true;
    }
}
