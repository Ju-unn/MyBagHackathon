package com.example.mybaghackathon.data.remote.dto.trip;

import com.google.gson.annotations.SerializedName;

// trip_members 1건 + users 조인 nickname/profile_image_url. detail.php/members.php 대응
public class TripMemberDto {

    @SerializedName("trip_member_id")
    private long tripMemberId;

    @SerializedName("user_id")
    private long userId;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("profile_image_url")
    private String profileImageUrl;

    @SerializedName("role")
    private String role;

    @SerializedName("member_status")
    private String memberStatus;

    @SerializedName("joined_at")
    private String joinedAt;

    public long getTripMemberId() {
        return tripMemberId;
    }

    public long getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getRole() {
        return role;
    }

    public String getMemberStatus() {
        return memberStatus;
    }

    public String getJoinedAt() {
        return joinedAt;
    }
}
