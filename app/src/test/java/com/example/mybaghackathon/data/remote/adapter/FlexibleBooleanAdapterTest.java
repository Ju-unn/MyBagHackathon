package com.example.mybaghackathon.data.remote.adapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.mybaghackathon.data.remote.dto.packing.ChecklistCheckResponseDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistItemDto;
import com.google.gson.Gson;

import org.junit.Test;

public class FlexibleBooleanAdapterTest {

    private final Gson gson = new Gson();

    @Test
    public void checklistItem_acceptsMysqlNumberBoolean() {
        ChecklistItemDto completed = gson.fromJson("{\"is_completed\":1}", ChecklistItemDto.class);
        ChecklistItemDto incomplete = gson.fromJson("{\"is_completed\":0}", ChecklistItemDto.class);

        assertTrue(completed.isCompleted());
        assertFalse(incomplete.isCompleted());
    }

    @Test
    public void checkResponse_acceptsJsonBoolean() {
        ChecklistCheckResponseDto completed = gson.fromJson(
                "{\"is_completed\":true}", ChecklistCheckResponseDto.class);
        ChecklistCheckResponseDto incomplete = gson.fromJson(
                "{\"is_completed\":false}", ChecklistCheckResponseDto.class);

        assertTrue(completed.isCompleted());
        assertFalse(incomplete.isCompleted());
    }
}
