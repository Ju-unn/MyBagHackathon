package com.example.mybaghackathon.data.remote.dto.auth;

import com.google.gson.annotations.SerializedName;

// POST /api/auth/kakao_login.php 요청 바디
public class KakaoLoginRequestDto {

    @SerializedName("access_token")
    private final String accessToken;

    public KakaoLoginRequestDto(String accessToken) {
        this.accessToken = accessToken;
    }
}
