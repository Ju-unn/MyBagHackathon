package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppResult;

// 내 프로필(user) 정보 수정 관련 데이터 접근 규칙을 정의하는 인터페이스
public interface UserRepository {

    // 닉네임 수정 — 성공 시 로컬 UserStorage 캐시도 함께 갱신한다
    AppResult<Void> updateNickname(String nickname);
}
