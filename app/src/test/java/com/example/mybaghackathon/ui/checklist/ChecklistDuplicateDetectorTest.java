package com.example.mybaghackathon.ui.checklist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.example.mybaghackathon.model.PackingItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ChecklistDuplicateDetectorTest {

    private static final long CURRENT_USER_ID = 7L;

    @Test
    public void sameNormalizedName_personalAndAssignedCommonBecomeOneGroup() {
        PackingItem personal = item(1L, "PERSONAL", CURRENT_USER_ID, null, "휴대폰 충전기");
        PackingItem common = item(2L, "COMMON", 9L, CURRENT_USER_ID, " 휴대폰충전기 ");
        common.setCompleted(true);

        List<ChecklistDuplicateDetector.ItemGroup> groups =
                ChecklistDuplicateDetector.groupForMine(
                        Arrays.asList(personal, common), CURRENT_USER_ID);

        assertEquals(1, groups.size());
        assertTrue(groups.get(0).isMerged());
        assertSame(personal, groups.get(0).getPersonalItem());
        assertSame(common, groups.get(0).getCommonItem());
        assertSame(common, groups.get(0).getCompletionItem());
    }

    @Test
    public void sameTypeItemsAreNotDropped() {
        PackingItem first = item(1L, "PERSONAL", CURRENT_USER_ID, null, "여권");
        PackingItem second = item(2L, "PERSONAL", CURRENT_USER_ID, null, "여 권");

        List<ChecklistDuplicateDetector.ItemGroup> groups =
                ChecklistDuplicateDetector.groupForMine(
                        Arrays.asList(first, second), CURRENT_USER_ID);

        assertEquals(2, groups.size());
    }

    @Test
    public void commonAssignedToAnotherUserIsExcluded() {
        PackingItem personal = item(1L, "PERSONAL", CURRENT_USER_ID, null, "여권");
        PackingItem common = item(2L, "COMMON", 9L, 10L, "여권");

        List<ChecklistDuplicateDetector.ItemGroup> groups =
                ChecklistDuplicateDetector.groupForMine(
                        Arrays.asList(personal, common), CURRENT_USER_ID);

        assertEquals(1, groups.size());
        assertSame(personal, groups.get(0).getPersonalItem());
    }

    @Test
    public void mergedGroupKeepsPersonalDefaultPriorityForMineFilter() {
        PackingItem personal = item(1L, "PERSONAL", CURRENT_USER_ID, null, "여권");
        personal.setSource("DEFAULT");
        personal.setPriority("OPTIONAL");
        PackingItem common = item(2L, "COMMON", 9L, CURRENT_USER_ID, "여 권");
        common.setPriority("REQUIRED");

        ChecklistDuplicateDetector.ItemGroup group =
                ChecklistDuplicateDetector.groupForMine(
                        Arrays.asList(personal, common), CURRENT_USER_ID).get(0);

        assertSame(personal, group.getPriorityItem());
        assertEquals("OPTIONAL", group.getPriorityItem().getPriority());
    }

    private PackingItem item(long id, String scope, long creatorId, Long assigneeId, String name) {
        PackingItem item = new PackingItem();
        item.setPackingItemId(id);
        item.setScope(scope);
        item.setCreatedByUserId(creatorId);
        item.setAssigneeUserId(assigneeId);
        item.setItemName(name);
        return item;
    }
}
