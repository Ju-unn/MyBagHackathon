package com.example.mybaghackathon.model;

/**
 * 방 생성(trips/create.php) 응답으로 받는 초대 정보를 나타내는 모델입니다.
 *
 * <p>서버는 만료 시각·사용 횟수 제한 없이 코드 하나만 발급하므로, 지금은
 * tripId와 inviteCode만 존재합니다. 만료/횟수 제한이 생기면 그때 필드를 추가합니다.</p>
 */
public class TripInvite {

    private long tripId;
    private String inviteCode;

    public TripInvite() {
    }

    public TripInvite(long tripId, String inviteCode) {
        this.tripId = tripId;
        this.inviteCode = inviteCode;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
}
