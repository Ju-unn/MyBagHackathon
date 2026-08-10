package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.model.PackingItem;

import java.util.List;
import java.util.Map;

// 체크리스트(준비물) 관련 데이터 접근 규칙을 정의하는 인터페이스
public interface PackingRepository {

    // 델타 조회(S11/S12) — since가 null이면 활성 항목 전체
    AppResult<List<PackingItem>> listItems(long tripId, String since);

    // BS02 항목 직접 추가 — 성공 시 생성된 packing_item_id 반환. category/scope는 없으면 null/기본값(COMMON)
    AppResult<Long> addItem(long tripId, String itemName, String category, String priority, String scope);

    // 체크/해제 토글 — 성공 시 변경된 완료 상태 반환
    AppResult<Boolean> toggleCheck(long itemId);

    // 담당자 지정(본인 userId)/해제(null) — 본인만 가능(서버 검증)
    AppResult<Void> assign(long itemId, Long assigneeUserId);

    // 공용 물품 1개를 여러 멤버에게 동시 배정 — 방장 전용, COMMON 물품만(서버 검증)
    AppResult<Void> assignMultiple(long itemId, List<Long> assigneeUserIds);

    // 항목 수정 — 안 고친 필드는 null로 넘기면 기존 값 유지됨
    AppResult<Void> updateItem(long itemId, String itemName, String category, String priority, String scope);

    // 항목 삭제(소프트 삭제)
    AppResult<Void> deleteItem(long itemId);

    // 재분석 후 재생성 — 방장 전용(F-LGTOBW). itemScopeByName: item_name -> COMMON|PERSONAL
    AppResult<Void> generate(long tripId, long analysisId, Map<String, String> itemScopeByName);
}
