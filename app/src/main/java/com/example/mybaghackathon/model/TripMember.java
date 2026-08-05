package com.example.mybaghackathon.model;

/**
 * 여행방에 참여한 사용자의 표시 정보와 방 안에서의 상태를 나타내는 모델입니다.
 *
 * <p>role은 OWNER 또는 MEMBER, memberStatus는 ACTIVE, LEFT 또는 REMOVED와 같은
 * 서버 상태 문자열을 사용합니다.</p>
 */
public class TripMember {

    private long userId;
    private String nickname;
    private String profileImageUrl;
    private String role;
    private String memberStatus;
    private String joinedAt;

    public TripMember() {
    }

    public TripMember(
            long userId,
            String nickname,
            String profileImageUrl,
            String role,
            String memberStatus,
            String joinedAt
    ) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.memberStatus = memberStatus;
        this.joinedAt = joinedAt;
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(String memberStatus) {
        this.memberStatus = memberStatus;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }
}
