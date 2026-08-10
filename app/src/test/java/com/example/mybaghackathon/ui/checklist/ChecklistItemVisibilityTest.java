package com.example.mybaghackathon.ui.checklist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.mybaghackathon.model.PackingItem;

import org.junit.Test;

public class ChecklistItemVisibilityTest {

    private static final long CURRENT_USER_ID = 7L;

    @Test
    public void ownPersonalItem_isVisible() {
        PackingItem item = item("PERSONAL", CURRENT_USER_ID, CURRENT_USER_ID);

        assertTrue(ChecklistItemVisibility.isMine(item, CURRENT_USER_ID));
    }

    @Test
    public void otherUsersPersonalItem_isHidden() {
        PackingItem item = item("PERSONAL", 9L, 9L);

        assertFalse(ChecklistItemVisibility.isMine(item, CURRENT_USER_ID));
    }

    @Test
    public void assignedCommonItem_isVisible() {
        PackingItem item = item("COMMON", 9L, CURRENT_USER_ID);

        assertTrue(ChecklistItemVisibility.isMine(item, CURRENT_USER_ID));
    }

    @Test
    public void unassignedCommonItem_isHidden() {
        PackingItem item = item("COMMON", CURRENT_USER_ID, null);

        assertFalse(ChecklistItemVisibility.isMine(item, CURRENT_USER_ID));
    }

    @Test
    public void legacyPersonalItem_fallsBackToAssignee() {
        PackingItem item = item("PERSONAL", 0L, CURRENT_USER_ID);

        assertTrue(ChecklistItemVisibility.isMine(item, CURRENT_USER_ID));
    }

    @Test
    public void onlyCommonScope_isSharedForAssignment() {
        assertTrue(ChecklistItemVisibility.isCommon(item("COMMON", 9L, null)));
        assertTrue(ChecklistItemVisibility.isCommon(item(null, 9L, null)));
        assertFalse(ChecklistItemVisibility.isCommon(item("PERSONAL", 9L, 9L)));
    }

    private PackingItem item(String scope, long createdByUserId, Long assigneeUserId) {
        PackingItem item = new PackingItem();
        item.setScope(scope);
        item.setCreatedByUserId(createdByUserId);
        item.setAssigneeUserId(assigneeUserId);
        return item;
    }
}
