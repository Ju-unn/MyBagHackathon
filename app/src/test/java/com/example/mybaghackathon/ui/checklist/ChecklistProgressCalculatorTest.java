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
    public void duplicatedAssignmentRowsCountAsOneCommonItem() {
        PackingItem firstAssignee = commonItem(10L, 7L, "AI", "ACTIVE", true);
        PackingItem secondAssignee = commonItem(11L, 7L, "AI", "ACTIVE", false);

        ChecklistProgressCalculator.Progress progress = ChecklistProgressCalculator.calculate(
                Arrays.asList(firstAssignee, secondAssignee));

        assertEquals(0, progress.completed);
        assertEquals(1, progress.total);
        assertEquals(0, progress.percent());
    }

    @Test
    public void oneCheckedOutOfFifteenShowsZeroOfOne() {
        PackingItem[] recommendations = new PackingItem[15];
        recommendations[0] = item(1L, 1L, "AI", "ACTIVE", false);
        recommendations[0].setScope("COMMON");
        for (int index = 1; index < recommendations.length; index++) {
            recommendations[index] = item(
                    index + 1L, index + 1L, "AI", "ACTIVE", false);
            recommendations[index].setScope("PERSONAL");
        }

        java.util.List<PackingItem> filtered = ChecklistSelectionFilter.apply(
                Arrays.asList(recommendations),
                java.util.Collections.singleton(
                        ChecklistSelectionFilter.normalize(recommendations[0].getItemName())));
        ChecklistProgressCalculator.Progress progress =
                ChecklistProgressCalculator.calculate(filtered);

        assertEquals(1, filtered.size());
        assertEquals(0, progress.completed);
        assertEquals(1, progress.total);
        assertEquals(0, progress.percent());
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
