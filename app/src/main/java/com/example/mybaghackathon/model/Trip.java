package com.example.mybaghackathon.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 여행방의 기본 정보와 참여자 목록을 나타내는 앱 내부 모델입니다.
 *
 * <p>날짜는 서버 API 규칙에 따라 yyyy-MM-dd 형식의 문자열로 보관합니다.
 * tripType과 status는 서버가 전달한 상태 문자열을 그대로 사용합니다.
 * destinationCountry/destinationCity는 국가 코드 없이 GPT 분석이 뽑은 텍스트 그대로입니다
 * (예: "일본"/"오사카") — trips.destination_country/city와 1:1 대응.</p>
 */
public class Trip {

    private long tripId;
    private long ownerUserId;
    private String tripName;
    private Integer expectedMemberCount;
    private String tripType;
    private String destinationCountry;
    private String destinationCity;
    private String startDate;
    private String endDate;
    private String status;
    private String createdAt;
    private List<TripMember> members = new ArrayList<>();
    // 방 상세 조회(GET detail.php) 응답에만 실려오는 값 — trips 테이블 컬럼이 아니라 trip_invites에서 옴.
    // list.php로 만든 Trip에는 항상 null(방 목록 화면은 초대 기능이 없어 필요 없음).
    private String inviteCode;

    public Trip() {
    }

    public Trip(
            long tripId,
            long ownerUserId,
            String tripName,
            Integer expectedMemberCount,
            String tripType,
            String destinationCountry,
            String destinationCity,
            String startDate,
            String endDate,
            String status,
            String createdAt,
            List<TripMember> members
    ) {
        this.tripId = tripId;
        this.ownerUserId = ownerUserId;
        this.tripName = tripName;
        this.expectedMemberCount = expectedMemberCount;
        this.tripType = tripType;
        this.destinationCountry = destinationCountry;
        this.destinationCity = destinationCity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
        setMembers(members);
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public Integer getExpectedMemberCount() {
        return expectedMemberCount;
    }

    public void setExpectedMemberCount(Integer expectedMemberCount) {
        this.expectedMemberCount = expectedMemberCount;
    }

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public void setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<TripMember> getMembers() {
        return members;
    }

    public void setMembers(List<TripMember> members) {
        this.members = members == null ? new ArrayList<>() : new ArrayList<>(members);
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
}
