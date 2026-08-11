package com.example.mybaghackathon.ui.checklist;

import static org.junit.Assert.assertEquals;

import com.example.mybaghackathon.model.PackingItem;

import org.junit.Test;

import java.util.Arrays;

public class ChecklistProgressCalculatorTest {

    @Test
    public void progressCountsOnlyActiveAiGroups() {
        PackingItem completedAi = item(1L, 1L, "AI", "ACTIVE", true);
        PackingItem incompleteAi = item(2L, 2L, "AI", "ACTIVE", false);
        PackingItem userItem = item(3L, 3L, "USER", "ACTIVE", true);
        PackingItem defaultItem = item(4L, 4L, "DEFAULT", "ACTIVE", true);
        PackingItem excludedAi = item(5L, 5L, "AI", "EXCLUDED", true);

        ChecklistProgressCalculator.Progress progress = ChecklistProgressCalculator.calculate(
                Arrays.asList(completedAi, incompleteAi, userItem, defaultItem, excludedAi));

        assertEquals(1, progress.completed);
        assertEquals(2, progress.total);
        assertEquals(50, progress.percent());
    }

    @Test
    public void duplicatedAssignmentRowsCountAsOneSelectedItem() {
        PackingItem firstAssignee = item(10L, 7L, "AI", "ACTIVE", true);
        PackingItem secondAssignee = item(11L, 7L, "AI", "ACTIVE", false);

        ChecklistProgressCalculator.Progress progress = ChecklistProgressCalculator.calculate(
                Arrays.asList(firstAssignee, secondAssignee));

        assertEquals(0, progress.completed);
        assertEquals(1, progress.total);
        assertEquals(0, progress.percent());
    }

    private PackingItem item(long id, long groupId, String source, String status, boolean completed) {
        PackingItem item = new PackingItem();
        item.setPackingItemId(id);
        item.setItemGroupId(groupId);
        item.setSource(source);
        item.setItemStatus(status);
        item.setCompleted(completed);
        return item;
    }
}
