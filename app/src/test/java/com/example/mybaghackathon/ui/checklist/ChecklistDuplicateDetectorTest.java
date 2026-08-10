package com.example.mybaghackathon.ui.checklist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.mybaghackathon.model.PackingItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

public class ChecklistDuplicateDetectorTest {

    private static final long CURRENT_USER_ID = 7L;

    @Test
    public void personalAndAssignedCommonWithSameName_areBothMarked() {
        PackingItem personal = item(1L, "PERSONAL", CURRENT_USER_ID, CURRENT_USER_ID,
                "휴대폰 충전기");
        PackingItem common = item(2L, "COMMON", 9L, CURRENT_USER_ID,
                " 휴대폰충전기 ");

        Set<Long> duplicates = ChecklistDuplicateDetector.findDuplicateItemIds(
                Arrays.asList(personal, common), CURRENT_USER_ID);

        assertTrue(duplicates.contains(1L));
        assertTrue(duplicates.contains(2L));
    }

    @Test
    public void twoPersonalItemsWithSameName_areNotCrossSourceDuplicates() {
        PackingItem first = item(1L, "PERSONAL", CURRENT_USER_ID, CURRENT_USER_ID, "우산");
        PackingItem second = item(2L, "PERSONAL", CURRENT_USER_ID, CURRENT_USER_ID, "우산");

        Set<Long> duplicates = ChecklistDuplicateDetector.findDuplicateItemIds(
                Arrays.asList(first, second), CURRENT_USER_ID);

        assertTrue(duplicates.isEmpty());
    }

    @Test
    public void commonItemAssignedToAnotherUser_isNotMyDuplicate() {
        PackingItem personal = item(1L, "PERSONAL", CURRENT_USER_ID, CURRENT_USER_ID, "여권");
        PackingItem common = item(2L, "COMMON", 9L, 10L, "여권");

        Set<Long> duplicates = ChecklistDuplicateDetector.findDuplicateItemIds(
                Arrays.asList(personal, common), CURRENT_USER_ID);

        assertFalse(duplicates.contains(1L));
        assertFalse(duplicates.contains(2L));
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
