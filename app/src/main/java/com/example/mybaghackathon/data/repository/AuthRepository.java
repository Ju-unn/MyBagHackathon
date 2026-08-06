package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.model.User;

// 인증(로그인) 관련 데이터 접근 규칙을 정의하는 인터페이스
public interface AuthRepository {

    // 카카오 access_token으로 로그인하고 성공 시 JWT를 저장한다
    AppResult<User> loginWithKakao(String kakaoAccessToken);

    // 로그아웃 처리 후 저장된 토큰을 지운다
    AppResult<Void> logout();

    // 이 기기의 FCM 토큰을 서버에 등록한다
    AppResult<Void> registerFcmToken(String token, String deviceId, String platform, String appVersion);
}
