package com.example.mybaghackathon.data.remote.dto.user;

import com.google.gson.annotations.SerializedName;

// POST /api/profile/me/update.php 요청 바디 — { "nickname" }
public class UserNicknameUpdateRequestDto {

    @SerializedName("nickname")
    private final String nickname;

    public UserNicknameUpdateRequestDto(String nickname) {
        this.nickname = nickname;
    }
}
