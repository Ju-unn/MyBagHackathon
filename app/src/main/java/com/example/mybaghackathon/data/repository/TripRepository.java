package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.TripInvite;
import com.example.mybaghackathon.model.TripMember;

import java.util.List;
import java.util.Map;

// 여행방 관련 데이터 접근 규칙을 정의하는 인터페이스
public interface TripRepository {

    // S08 '방 생성 완료' — itemScopeByName: item_name -> COMMON|PERSONAL (S08 Scope 토글, 미포함 항목은 서버 기본값 COMMON)
    AppResult<TripInvite> createTrip(
            String tripName,
            int expectedMemberCount,
            long analysisId,
            Map<String, String> itemScopeByName
    );

    // 홈(S03) — 보관되지 않은 내 여행방 목록
    AppResult<List<Trip>> listMyTrips();

    // 보관함(S14)
    AppResult<List<Trip>> listArchivedTrips();

    // 방 상세(S09)
    AppResult<Trip> getTripDetail(long tripId);

    // 참여자 목록
    AppResult<List<TripMember>> listMembers(long tripId);

    // 초대코드로 참여 — 성공 시 참여한 tripId를 반환
    AppResult<Long> joinByCode(String inviteCode);
}
