package com.example.mybaghackathon.data.mapper;

import com.example.mybaghackathon.data.remote.dto.defaultitem.DefaultItemDto;
import com.example.mybaghackathon.model.UserDefaultItem;

import java.util.ArrayList;
import java.util.List;

// DefaultItemDto를 UserDefaultItem 모델로 변환
public final class DefaultItemMapper {

    private DefaultItemMapper() {
    }

    public static UserDefaultItem from(DefaultItemDto dto) {
        return new UserDefaultItem(
                dto.getDefaultItemId(),
                dto.getItemName(),
                dto.getCategory(),
                dto.getDefaultPriority()
        );
    }

    public static List<UserDefaultItem> from(List<DefaultItemDto> dtos) {
        List<UserDefaultItem> result = new ArrayList<>();
        if (dtos == null) {
            return result;
        }
        for (DefaultItemDto dto : dtos) {
            result.add(from(dto));
        }
        return result;
    }
}
