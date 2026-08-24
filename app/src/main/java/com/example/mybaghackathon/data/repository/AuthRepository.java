package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.model.LoginOutcome;
import com.example.mybaghackathon.model.User;

// 인증(로그인) 관련 데이터 접근 규칙을 정의하는 인터페이스
public interface AuthRepository {

    // 카카오 access_token으로 로그인하고 성공 시 JWT를 저장한다. 필수 동의 값도 서버에 전달.
    // restoreConfirmed=true는 탈퇴 계정 복구 확인 다이얼로그를 거친 뒤 재시도할 때만 true로 보낸다
    AppResult<LoginOutcome> loginWithKakao(String kakaoAccessToken, boolean privacyAgreed, boolean termsAgreed, boolean restoreConfirmed);

    // 로그아웃 처리 후 저장된 토큰을 지운다
    AppResult<Void> logout();

    // 이 기기의 FCM 토큰을 서버에 등록한다
    AppResult<Void> registerFcmToken(String token, String deviceId, String platform, String appVersion);

    // 회원 탈퇴. 서버 처리가 성공한 경우에만 로컬 토큰을 지운다(실패 시 계정은 그대로 살아있으므로)
    AppResult<Void> withdraw();
}
