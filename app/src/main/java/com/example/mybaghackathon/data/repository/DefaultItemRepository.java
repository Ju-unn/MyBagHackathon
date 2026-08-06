package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.model.UserDefaultItem;

import java.util.List;

// 내 기본 물품(user_default_items) 관련 데이터 접근 규칙을 정의하는 인터페이스
public interface DefaultItemRepository {

    // 내 기본 물품 목록 조회 (S15ItemsWrap)
    AppResult<List<UserDefaultItem>> listItems();

    // 항목 추가 — 성공 시 생성된 default_item_id 반환. category는 없으면 null, priority는 기본 OPTIONAL
    AppResult<Long> addItem(String itemName, String category, String priority);

    // 항목 수정 — 안 고친 필드는 null로 넘기면 기존 값 유지됨
    AppResult<Void> updateItem(long defaultItemId, String itemName, String category, String priority);

    // 항목 삭제(소프트 삭제)
    AppResult<Void> deleteItem(long defaultItemId);
}
