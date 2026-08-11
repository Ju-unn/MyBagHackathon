package com.example.mybaghackathon.ui.checklist;

import com.example.mybaghackathon.model.PackingItem;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 내 목록에 표시할 원본 행을 만든다.
 * 같은 이름의 내 개인 물품과 나에게 배정된 공용 물품만 한 그룹으로 묶으며,
 * 서버 원본 PackingItem은 변경하지 않는다.
 */
final class ChecklistDuplicateDetector {

    private ChecklistDuplicateDetector() {
    }

    static List<ItemGroup> groupForMine(List<PackingItem> items, long currentUserId) {
        List<MutableGroup> groups = new ArrayList<>();
        if (items == null || currentUserId <= 0L) {
            return new ArrayList<>();
        }

        for (PackingItem item : items) {
            boolean personal = ChecklistItemVisibility.isOwnedPersonal(item, currentUserId);
            boolean common = ChecklistItemVisibility.isAssignedCommon(item, currentUserId);
            if (!personal && !common) {
                continue;
            }

            String key = normalizedName(item.getItemName());
            MutableGroup target = findAvailableGroup(groups, key, personal);
            if (target == null) {
                target = new MutableGroup(key);
                groups.add(target);
            }
            if (personal) {
                target.personalItem = item;
            } else {
                target.commonItem = item;
            }
        }

        List<ItemGroup> result = new ArrayList<>();
        for (MutableGroup group : groups) {
            result.add(new ItemGroup(group.personalItem, group.commonItem));
        }
        return result;
    }

    static String normalizedName(String name) {
        if (name == null) {
            return "";
        }
        return Normalizer.normalize(name, Normalizer.Form.NFKC)
                .replaceAll("\\s+", "")
                .toLowerCase(Locale.ROOT);
    }

    private static MutableGroup findAvailableGroup(
            List<MutableGroup> groups,
            String key,
            boolean personal
    ) {
        if (key.isEmpty()) {
            return null;
        }
        for (MutableGroup group : groups) {
            if (!key.equals(group.key)) {
                continue;
            }
            if (personal && group.personalItem == null) {
                return group;
            }
            if (!personal && group.commonItem == null) {
                return group;
            }
        }
        return null;
    }

    static final class ItemGroup {
        private final PackingItem personalItem;
        private final PackingItem commonItem;

        ItemGroup(PackingItem personalItem, PackingItem commonItem) {
            this.personalItem = personalItem;
            this.commonItem = commonItem;
        }

        PackingItem getPersonalItem() {
            return personalItem;
        }

        PackingItem getCommonItem() {
            return commonItem;
        }

        PackingItem getDisplayItem() {
            return commonItem != null ? commonItem : personalItem;
        }

        PackingItem getCompletionItem() {
            return commonItem != null ? commonItem : personalItem;
        }

        /** 내 기본 물품의 중요도 분류를 유지하고, 개인 물품이 없을 때만 공용 값을 쓴다. */
        PackingItem getPriorityItem() {
            return personalItem != null ? personalItem : commonItem;
        }

        boolean isMerged() {
            return personalItem != null && commonItem != null;
        }
    }

    private static final class MutableGroup {
        private final String key;
        private PackingItem personalItem;
        private PackingItem commonItem;

        private MutableGroup(String key) {
            this.key = key;
        }
    }
}
