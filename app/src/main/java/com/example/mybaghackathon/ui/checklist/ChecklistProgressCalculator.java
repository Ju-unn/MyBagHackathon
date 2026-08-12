package com.example.mybaghackathon.ui.checklist;

import com.example.mybaghackathon.model.PackingItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 진행률 계산. 방 형태에 따라 두 기준을 쓴다.
 * - calculate(): 다인방용. 활성 공용(COMMON) 물품만 item_group_id 단위로 센다(다중 담당은 물품
 *   1개, 그룹의 담당 행 전원 완료 시 완료 1). 개인·기본 물품은 제외.
 * - calculateForMine(): 1인방용. 공용 리스트·분담 개념이 없으므로 화면에 보이는 "내 목록"
 *   (선택 AI 공용 + 기본/개인 물품, 이름 병합) 전체를 그대로 센다. 기본 물품도 진행률에 포함된다.
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

    // 1인 방은 공용 리스트·분담 현황 탭이 없고 "내 목록"만 보여준다. 그 화면과 숫자를 맞추려면
    // 선택한 AI 공용 물품 + 내 기본/개인 물품을 이름으로 병합한 "내 목록" 그룹을 그대로 센다.
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
