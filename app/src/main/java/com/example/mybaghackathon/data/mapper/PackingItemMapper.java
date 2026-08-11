package com.example.mybaghackathon.data.mapper;

import com.example.mybaghackathon.data.remote.dto.packing.ChecklistItemDto;
import com.example.mybaghackathon.data.remote.dto.packing.PackingItemDto;
import com.example.mybaghackathon.model.PackingItem;

import java.util.ArrayList;
import java.util.List;

// PackingItemDto/ChecklistItemDto를 PackingItem 모델로 변환
public final class PackingItemMapper {

    private PackingItemMapper() {
    }

    // GPT 분석 단계의 추천 준비물 목록 변환 — packingItemId 등 DB 전용 필드는 방 생성 전이라 아직 없음(기본값 유지)
    public static List<PackingItem> from(List<PackingItemDto> dtos) {
        List<PackingItem> result = new ArrayList<>();
        if (dtos == null) {
            return result;
        }
        for (PackingItemDto dto : dtos) {
            PackingItem item = new PackingItem();
            item.setItemName(dto.getItemName());
            item.setCategory(dto.getCategory());
            item.setPriority(dto.getPriority());
            result.add(item);
        }
        return result;
    }

    // 체크리스트 API(list/create 등)가 돌려주는 packing_items 전체 필드 변환
    public static PackingItem fromChecklistItem(ChecklistItemDto dto) {
        PackingItem item = new PackingItem(
                dto.getPackingItemId(),
                dto.getTripId(),
                dto.getCreatedByUserId(),
                dto.getItemName(),
                dto.getCategory(),
                dto.getPriority(),
                dto.getItemScope(),
                dto.getSource(),
                dto.getRestrictionType(),
                dto.getRestrictionReason(),
                dto.getAssigneeUserId(),
                dto.isCompleted(),
                dto.getCompletedAt(),
                dto.getItemStatus(),
                dto.getSortOrder()
        );
        item.setItemGroupId(dto.getItemGroupId());
        return item;
    }

    public static List<PackingItem> fromChecklistItems(List<ChecklistItemDto> dtos) {
        List<PackingItem> result = new ArrayList<>();
        if (dtos == null) {
            return result;
        }
        for (ChecklistItemDto dto : dtos) {
            result.add(fromChecklistItem(dto));
        }
        return result;
    }
}
