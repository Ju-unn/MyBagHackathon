package com.example.mybaghackathon.model;

/**
 * 로그인한 사용자의 앱 내부 정보를 나타내는 모델입니다.
 *
 * <p>서버 응답 형식은 DTO에서 처리하고, 화면과 Presenter는 이 모델을 사용합니다.
 * email과 profileImageUrl은 카카오 계정 제공 범위에 따라 null일 수 있습니다.</p>
 */
public class User {

    private long userId;
    private String nickname;
    private String email;
    private String profileImageUrl;

    public User() {
    }

    public User(long userId, String nickname, String email, String profileImageUrl) {
        this.userId = userId;
        this.nickname = nickname;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
