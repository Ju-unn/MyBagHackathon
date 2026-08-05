package com.example.mybaghackathon.model;

/**
 * 여행방에 동행자를 초대할 때 사용하는 공유 정보를 나타내는 모델입니다.
 *
 * <p>inviteCode는 서버가 참여 요청을 검증할 때 사용하고, inviteUrl은 Android
 * 공유 화면에서 전달할 전체 링크입니다. expiresAt은 ISO 8601 형식의 문자열을
 * 사용합니다.</p>
 */
public class TripInvite {

    private long inviteId;
    private String inviteCode;
    private String inviteUrl;
    private String expiresAt;
    private boolean active;

    public TripInvite() {
    }

    public TripInvite(
            long inviteId,
            String inviteCode,
            String inviteUrl,
            String expiresAt,
            boolean active
    ) {
        this.inviteId = inviteId;
        this.inviteCode = inviteCode;
        this.inviteUrl = inviteUrl;
        this.expiresAt = expiresAt;
        this.active = active;
    }

    public long getInviteId() {
        return inviteId;
    }

    public void setInviteId(long inviteId) {
        this.inviteId = inviteId;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getInviteUrl() {
        return inviteUrl;
    }

    public void setInviteUrl(String inviteUrl) {
        this.inviteUrl = inviteUrl;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
