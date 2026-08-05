package com.example.mybaghackathon.data.remote.dto.auth;

import com.google.gson.annotations.SerializedName;

// POST /api/auth/kakao_login.php 응답 data ({"token","user":{...}})
public class AuthTokenDto {

    private String token;
    private UserPayload user;

    public String getToken() {
        return token;
    }

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

        public long getId() {
            return id;
        }

        public long getKakaoId() {
            return kakaoId;
        }

        public String getNickname() {
            return nickname;
        }

        public String getProfileImageUrl() {
            return profileImageUrl;
        }
    }
}
