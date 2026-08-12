package com.example.mybaghackathon.ui.checklist;

import com.example.mybaghackathon.model.PackingItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates progress from active common(공용) checklist items only, counted per 물품 — AI
 * recommendations the host kept and items added directly to the common list. Multi-assignee
 * items share one item_group_id and count as a single 물품: the group is done only when every
 * assignee row is completed, and it contributes 1 to the total. Personal/base items (PERSONAL,
 * DEFAULT) are never counted, so 1인/다인 방 모두 같은 공용 기준을 쓴다.
 */
public final class ChecklistProgressCalculator {

    private ChecklistProgressCalculator() {
    }

    public static Progress calculate(List<PackingItem> items) {
        // 활성 공용 물품을 item_group_id로 묶어 "물품 1개" 단위로 계산.
        // 다중 담당은 그룹의 담당 행이 모두 완료돼야 그 물품을 완료로 센다.
        Map<Long, Boolean> groupAllDone = new LinkedHashMap<>();
        if (items != null) {
            for (PackingItem item : items) {
                if (!isActiveCommonItem(item)) {
                    continue;
                }
                long groupId = item.getItemGroupId(); // group 없으면 packingItemId 반환(모델 처리됨)
                boolean prev = groupAllDone.containsKey(groupId) ? groupAllDone.get(groupId) : true;
                groupAllDone.put(groupId, prev && item.isCompleted());
            }
        }
        int completed = 0;
        for (boolean done : groupAllDone.values()) {
            if (done) {
                completed++;
            }
        }
        return new Progress(completed, groupAllDone.size());
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
