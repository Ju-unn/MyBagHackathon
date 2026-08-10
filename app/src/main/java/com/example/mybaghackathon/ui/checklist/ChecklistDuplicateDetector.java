package com.example.mybaghackathon.ui.checklist;

import com.example.mybaghackathon.model.PackingItem;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** 개인 물품과 현재 사용자에게 배정된 공용 물품의 이름 중복을 찾는다. */
final class ChecklistDuplicateDetector {

    private ChecklistDuplicateDetector() {
    }

    static Set<Long> findDuplicateItemIds(List<PackingItem> items, long currentUserId) {
        Set<Long> duplicateIds = new HashSet<>();
        if (items == null || currentUserId <= 0L) {
            return duplicateIds;
        }

        Map<String, Set<Long>> personalIdsByName = new HashMap<>();
        Map<String, Set<Long>> assignedCommonIdsByName = new HashMap<>();

        for (PackingItem item : items) {
            if (ChecklistItemVisibility.isOwnedPersonal(item, currentUserId)) {
                add(personalIdsByName, normalizedName(item.getItemName()), item.getPackingItemId());
            } else if (ChecklistItemVisibility.isAssignedCommon(item, currentUserId)) {
                add(assignedCommonIdsByName, normalizedName(item.getItemName()), item.getPackingItemId());
            }
        }

        for (Map.Entry<String, Set<Long>> entry : personalIdsByName.entrySet()) {
            Set<Long> commonIds = assignedCommonIdsByName.get(entry.getKey());
            if (!entry.getKey().isEmpty() && commonIds != null && !commonIds.isEmpty()) {
                duplicateIds.addAll(entry.getValue());
                duplicateIds.addAll(commonIds);
            }
        }
        return duplicateIds;
    }

    static String normalizedName(String name) {
        if (name == null) {
            return "";
        }
        return Normalizer.normalize(name, Normalizer.Form.NFKC)
                .replaceAll("\\s+", "")
                .toLowerCase(Locale.ROOT);
    }

    private static void add(Map<String, Set<Long>> idsByName, String name, long itemId) {
        if (!name.isEmpty()) {
            idsByName.computeIfAbsent(name, ignored -> new HashSet<>()).add(itemId);
        }
    }
}
