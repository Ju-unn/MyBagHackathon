package com.example.mybaghackathon.data.remote.dto.auth;

import com.google.gson.annotations.SerializedName;

// POST /api/auth/kakao_login.php 응답 data.
// 정상 로그인: {"token","user":{...}}. 탈퇴 후 유예기간 내 계정인데 복구 미확인 상태로 로그인 시도한 경우:
// {"requires_restore_confirmation":true,"withdrawn_at":"..."} — 이때 token/user는 내려오지 않는다
public class AuthTokenDto {

    private String token;
    private UserPayload user;

    @SerializedName("requires_restore_confirmation")
    private boolean requiresRestoreConfirmation;

    @SerializedName("withdrawn_at")
    private String withdrawnAt;

    // 발급된 JWT를 반환한다
    public String getToken() {
        return token;
    }

    // 로그인한 사용자 정보를 반환한다
    public UserPayload getUser() {
        return user;
    }

    // 탈퇴 계정 복구 확인이 필요한 응답인지 반환한다
    public boolean isRequiresRestoreConfirmation() {
        return requiresRestoreConfirmation;
    }

    // 탈퇴 시각을 반환한다 (복구 확인이 필요한 응답일 때만 값이 있음)
    public String getWithdrawnAt() {
        return withdrawnAt;
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
