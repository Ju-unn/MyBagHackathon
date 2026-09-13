package com.example.mybaghackathon.ui.checklist;

import static org.junit.Assert.assertEquals;

import com.example.mybaghackathon.model.PackingItem;

import org.junit.Test;

import java.util.Arrays;

public class ChecklistProgressCalculatorTest {

    @Test
    public void progressCountsAllActiveCommonItemsRegardlessOfSource() {
        PackingItem completedAi = commonItem(1L, 1L, "AI", "ACTIVE", true);
        PackingItem incompleteAi = commonItem(2L, 2L, "AI", "ACTIVE", false);
        PackingItem completedManualCommon = commonItem(3L, 3L, "USER", "ACTIVE", true);
        PackingItem personalDefaultItem = item(4L, 4L, "DEFAULT", "ACTIVE", true);
        personalDefaultItem.setScope("PERSONAL");
        PackingItem excludedCommon = commonItem(5L, 5L, "AI", "EXCLUDED", true);

        ChecklistProgressCalculator.Progress progress = ChecklistProgressCalculator.calculate(
                Arrays.asList(completedAi, incompleteAi, completedManualCommon,
                        personalDefaultItem, excludedCommon));

        assertEquals(2, progress.completed);
        assertEquals(3, progress.total);
        assertEquals(67, progress.percent());
    }

    @Test
    public void multiAssigneeGroupCountsAsOneItemDoneOnlyWhenAllRowsDone() {
        // 같은 item_group_id를 공유하는 다중 배정 row 2개 — 물품 1개로 세고, 담당자 전원이 체크해야 완료.
        PackingItem firstAssignee = commonItem(10L, 7L, "AI", "ACTIVE", true);
        PackingItem secondAssignee = commonItem(11L, 7L, "AI", "ACTIVE", false);

        ChecklistProgressCalculator.Progress progress = ChecklistProgressCalculator.calculate(
                Arrays.asList(firstAssignee, secondAssignee));

        assertEquals(0, progress.completed);
        assertEquals(1, progress.total);
        assertEquals(0, progress.percent());

        secondAssignee.setCompleted(true);
        progress = ChecklistProgressCalculator.calculate(Arrays.asList(firstAssignee, secondAssignee));

        assertEquals(1, progress.completed);
        assertEquals(1, progress.total);
    }

    // 1인 방: 방 생성 시점의 AI 추천(COMMON)뿐 아니라 "내 목록"에서 나중에 직접 추가한 물품
    // (PERSONAL)도 진행률에 반영돼야 한다 — calculate()라면 뒤에 추가한 물품을 놓친다.
    @Test
    public void calculateForMine_countsPersonalItemsAddedAfterRoomCreation() {
        long userId = 7L;
        PackingItem aiCommon = soloItem(1L, "COMMON", "AI", userId, userId, true);
        PackingItem manualPersonal = soloItem(2L, "PERSONAL", "USER", userId, userId, false);

        ChecklistProgressCalculator.Progress progress = ChecklistProgressCalculator.calculateForMine(
                Arrays.asList(aiCommon, manualPersonal), userId);

        assertEquals(1, progress.completed);
        assertEquals(2, progress.total);
        assertEquals(50, progress.percent());
    }

    private PackingItem soloItem(
            long id, String scope, String source, long creatorId, Long assigneeId, boolean completed) {
        PackingItem item = new PackingItem();
        item.setPackingItemId(id);
        item.setItemName("item-" + id);
        item.setScope(scope);
        item.setSource(source);
        item.setCreatedByUserId(creatorId);
        item.setAssigneeUserId(assigneeId);
        item.setItemStatus("ACTIVE");
        item.setCompleted(completed);
        return item;
    }

    private PackingItem commonItem(
            long id, long groupId, String source, String status, boolean completed) {
        PackingItem item = item(id, groupId, source, status, completed);
        item.setScope("COMMON");
        return item;
    }

    private PackingItem item(long id, long groupId, String source, String status, boolean completed) {
        PackingItem item = new PackingItem();
        item.setPackingItemId(id);
        item.setItemGroupId(groupId);
        item.setItemName("item-" + id);
        item.setSource(source);
        item.setItemStatus(status);
        item.setCompleted(completed);
        return item;
    }
}
