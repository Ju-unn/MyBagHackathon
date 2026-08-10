package com.example.mybaghackathon.data.mapper;

import static org.junit.Assert.assertEquals;

import com.example.mybaghackathon.data.remote.dto.packing.ChecklistItemDto;
import com.example.mybaghackathon.model.PackingItem;
import com.google.gson.Gson;

import org.junit.Test;

public class PackingItemMapperTest {

    @Test
    public void checklistResponse_mapsCreatorUserId() {
        String json = "{\"packing_item_id\":11,\"trip_id\":3,"
                + "\"created_by_user_id\":7,\"item_name\":\"여권\","
                + "\"item_scope\":\"PERSONAL\",\"assignee_user_id\":7,"
                + "\"is_completed\":false}";

        ChecklistItemDto dto = new Gson().fromJson(json, ChecklistItemDto.class);
        PackingItem item = PackingItemMapper.fromChecklistItem(dto);

        assertEquals(7L, item.getCreatedByUserId());
    }
}
