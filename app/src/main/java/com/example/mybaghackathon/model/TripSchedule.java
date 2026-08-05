package com.example.mybaghackathon.model;

/**
 * 사용자가 검토하고 확정한 여행 일정 한 건을 나타내는 모델입니다.
 */
public class TripSchedule {

    private long scheduleId;
    private long tripId;
    private String scheduleDate;
    private String startTime;
    private String endTime;
    private String countryCode;
    private String countryName;
    private String cityName;
    private String placeName;
    private String activityText;
    private Double latitude;
    private Double longitude;
    private int sortOrder;
    private String source;
    private boolean confirmed;

    public TripSchedule() {
    }

    public TripSchedule(
            long scheduleId,
            long tripId,
            String scheduleDate,
            String startTime,
            String endTime,
            String countryCode,
            String countryName,
            String cityName,
            String placeName,
            String activityText,
            Double latitude,
            Double longitude,
            int sortOrder,
            String source,
            boolean confirmed
    ) {
        this.scheduleId = scheduleId;
        this.tripId = tripId;
        this.scheduleDate = scheduleDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.cityName = cityName;
        this.placeName = placeName;
        this.activityText = activityText;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sortOrder = sortOrder;
        this.source = source;
        this.confirmed = confirmed;
    }

    public long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public String getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(String scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getActivityText() {
        return activityText;
    }

    public void setActivityText(String activityText) {
        this.activityText = activityText;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
