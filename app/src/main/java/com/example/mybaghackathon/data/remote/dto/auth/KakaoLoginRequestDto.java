package com.example.mybaghackathon.data.remote.dto.auth;

import com.google.gson.annotations.SerializedName;

// POST /api/auth/kakao_login.php 요청 바디
public class KakaoLoginRequestDto {

    @SerializedName("access_token")
    private final String accessToken;

    // 로그인 화면 필수 동의 체크박스 값 — 서버가 user_consents에 기록
    @SerializedName("privacy_agreed")
    private final boolean privacyAgreed;

    @SerializedName("terms_agreed")
    private final boolean termsAgreed;

    // 탈퇴 후 유예기간(30일) 내 계정 복구 확인 다이얼로그를 거쳤는지 여부
    @SerializedName("restore_confirmed")
    private final boolean restoreConfirmed;

    public KakaoLoginRequestDto(String accessToken, boolean privacyAgreed, boolean termsAgreed, boolean restoreConfirmed) {
        this.accessToken = accessToken;
        this.privacyAgreed = privacyAgreed;
        this.termsAgreed = termsAgreed;
        this.restoreConfirmed = restoreConfirmed;
    }
}
