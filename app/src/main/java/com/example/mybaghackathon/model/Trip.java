package com.example.mybaghackathon.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 여행방의 기본 정보와 참여자 목록을 나타내는 앱 내부 모델입니다.
 *
 * <p>날짜는 서버 API 규칙에 따라 yyyy-MM-dd 형식의 문자열로 보관합니다.
 * tripType과 status는 서버가 전달한 상태 문자열을 그대로 사용합니다.</p>
 */
public class Trip {

    private long tripId;
    private long ownerUserId;
    private String tripName;
    private Integer expectedMemberCount;
    private String tripType;
    private String countryCode;
    private String countryName;
    private String representativeCity;
    private String startDate;
    private String endDate;
    private String status;
    private String analysisConfirmedAt;
    private List<TripMember> members = new ArrayList<>();

    public Trip() {
    }

    public Trip(
            long tripId,
            long ownerUserId,
            String tripName,
            Integer expectedMemberCount,
            String tripType,
            String countryCode,
            String countryName,
            String representativeCity,
            String startDate,
            String endDate,
            String status,
            String analysisConfirmedAt,
            List<TripMember> members
    ) {
        this.tripId = tripId;
        this.ownerUserId = ownerUserId;
        this.tripName = tripName;
        this.expectedMemberCount = expectedMemberCount;
        this.tripType = tripType;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.representativeCity = representativeCity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.analysisConfirmedAt = analysisConfirmedAt;
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

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getRepresentativeCity() {
        return representativeCity;
    }

    public void setRepresentativeCity(String representativeCity) {
        this.representativeCity = representativeCity;
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

    public String getAnalysisConfirmedAt() {
        return analysisConfirmedAt;
    }

    public void setAnalysisConfirmedAt(String analysisConfirmedAt) {
        this.analysisConfirmedAt = analysisConfirmedAt;
    }

    public List<TripMember> getMembers() {
        return members;
    }

    public void setMembers(List<TripMember> members) {
        this.members = members == null ? new ArrayList<>() : new ArrayList<>(members);
    }
}
