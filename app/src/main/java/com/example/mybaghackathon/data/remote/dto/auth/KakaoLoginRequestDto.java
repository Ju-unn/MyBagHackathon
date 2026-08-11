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

    public KakaoLoginRequestDto(String accessToken, boolean privacyAgreed, boolean termsAgreed) {
        this.accessToken = accessToken;
        this.privacyAgreed = privacyAgreed;
        this.termsAgreed = termsAgreed;
    }
}
