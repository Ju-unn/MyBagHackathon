package com.example.mybaghackathon.data.mapper;

import com.example.mybaghackathon.data.remote.dto.packing.PackingItemDto;
import com.example.mybaghackathon.model.PackingItem;

import java.util.ArrayList;
import java.util.List;

// PackingItemDto를 PackingItem 모델로 변환
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
}
