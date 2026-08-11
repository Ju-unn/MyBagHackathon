package com.example.mybaghackathon.ui.checklist;

import com.example.mybaghackathon.model.PackingItem;

/** Shared rule for recommendations that the host checked on the review screen. */
final class ChecklistItemSelection {

    private ChecklistItemSelection() {
    }

    static boolean isSelectedRecommendation(PackingItem item) {
        return item != null
                && "AI".equalsIgnoreCase(item.getSource())
                && ChecklistItemVisibility.isCommon(item)
                && isActive(item);
    }

    static boolean isUnselectedRecommendation(PackingItem item) {
        return item != null
                && "AI".equalsIgnoreCase(item.getSource())
                && !ChecklistItemVisibility.isCommon(item);
    }

    private static boolean isActive(PackingItem item) {
        return !"EXCLUDED".equalsIgnoreCase(item.getItemStatus())
                && !"DELETED".equalsIgnoreCase(item.getItemStatus());
    }
}
