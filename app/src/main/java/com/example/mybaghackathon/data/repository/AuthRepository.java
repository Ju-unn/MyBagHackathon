package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.model.User;

// 인증(로그인) 관련 데이터 접근 규칙을 정의하는 인터페이스
public interface AuthRepository {

    AppResult<User> loginWithKakao(String kakaoAccessToken);

    AppResult<Void> logout();

    AppResult<Void> registerFcmToken(String token, String deviceId, String platform, String appVersion);
}
