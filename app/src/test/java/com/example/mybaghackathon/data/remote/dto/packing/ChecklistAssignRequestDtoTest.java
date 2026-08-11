package com.example.mybaghackathon.data.remote.dto.packing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class ChecklistAssignRequestDtoTest {

    private final Gson gson = new Gson();

    @Test
    public void singleAssignment_serializesItemIdContract() {
        JsonObject json = toJson(new ChecklistAssignRequestDto(30L, 7L));

        assertEquals(30L, json.get("item_id").getAsLong());
        assertEquals(7L, json.get("assignee_user_id").getAsLong());
        assertFalse(json.has("item_group_id"));
        assertFalse(json.has("assignee_user_ids"));
    }

    @Test
    public void singleUnassignment_omitsNullableAssignee() {
        JsonObject json = toJson(new ChecklistAssignRequestDto(30L, (Long) null));

        assertEquals(30L, json.get("item_id").getAsLong());
        assertFalse(json.has("assignee_user_id"));
        assertFalse(json.has("item_group_id"));
    }

    @Test
    public void groupAssignment_serializesFinalAssigneeListContract() {
        JsonObject json = toJson(new ChecklistAssignRequestDto(
                500L, Arrays.asList(7L, 8L)));

        assertEquals(500L, json.get("item_group_id").getAsLong());
        assertEquals(2, json.getAsJsonArray("assignee_user_ids").size());
        assertFalse(json.has("item_id"));
        assertFalse(json.has("assignee_user_id"));
    }

    @Test
    public void groupUnassignment_keepsEmptyArray() {
        JsonObject json = toJson(new ChecklistAssignRequestDto(
                500L, Collections.emptyList()));

        assertTrue(json.has("assignee_user_ids"));
        assertEquals(0, json.getAsJsonArray("assignee_user_ids").size());
    }

    private JsonObject toJson(ChecklistAssignRequestDto request) {
        return gson.toJsonTree(request).getAsJsonObject();
    }
}
