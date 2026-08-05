package com.example.mybaghackathon.data.remote.dto.auth;

import com.google.gson.annotations.SerializedName;

// POST /api/auth/kakao_login.php 요청 바디
public class KakaoLoginRequestDto {

    @SerializedName("access_token")
    private final String accessToken;

    // 카카오에서 받은 access_token을 요청 바디로 감싼다
    public KakaoLoginRequestDto(String accessToken) {
        this.accessToken = accessToken;
    }
}
