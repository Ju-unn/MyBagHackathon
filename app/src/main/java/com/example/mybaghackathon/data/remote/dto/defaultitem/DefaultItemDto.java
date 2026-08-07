package com.example.mybaghackathon.data.remote.dto.defaultitem;

import com.google.gson.annotations.SerializedName;

// user_default_items 테이블 1건 응답 (default-items/list.php의 items 배열 요소)
public class DefaultItemDto {

    @SerializedName("default_item_id")
    private long defaultItemId;

    @SerializedName("item_name")
    private String itemName;

    private String category;

    // REQUIRED, RECOMMENDED, OPTIONAL 중 하나
    @SerializedName("default_priority")
    private String defaultPriority;

    public long getDefaultItemId() {
        return defaultItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getCategory() {
        return category;
    }

    public String getDefaultPriority() {
        return defaultPriority;
    }
}
