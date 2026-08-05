package com.example.mybaghackathon.data.remote.dto.auth;

import com.google.gson.annotations.SerializedName;

// POST /api/auth/kakao_login.php 응답 data ({"token","user":{...}})
public class AuthTokenDto {

    private String token;
    private UserPayload user;

    // 발급된 JWT를 반환한다
    public String getToken() {
        return token;
    }

    // 로그인한 사용자 정보를 반환한다
    public UserPayload getUser() {
        return user;
    }

    public static class UserPayload {

        private long id;

        @SerializedName("kakao_id")
        private long kakaoId;

        private String nickname;

        @SerializedName("profile_image_url")
        private String profileImageUrl;

        // 서버 user_id를 반환한다
        public long getId() {
            return id;
        }

        // 카카오 사용자 id를 반환한다
        public long getKakaoId() {
            return kakaoId;
        }

        // 닉네임을 반환한다
        public String getNickname() {
            return nickname;
        }

        // 프로필 이미지 URL을 반환한다 (없으면 null)
        public String getProfileImageUrl() {
            return profileImageUrl;
        }
    }
}
