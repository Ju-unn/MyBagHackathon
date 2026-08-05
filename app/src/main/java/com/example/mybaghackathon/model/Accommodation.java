package com.example.mybaghackathon.model;

/**
 * 여행 일정에서 사용자가 검토하고 확정한 숙소 한 건을 나타내는 모델입니다.
 */
public class Accommodation {

    private long accommodationId;
    private long tripId;
    private String name;
    private String countryCode;
    private String cityName;
    private String address;
    private String checkInAt;
    private String checkOutAt;
    private Double latitude;
    private Double longitude;
    private String source;
    private boolean confirmed;

    public Accommodation() {
    }

    public Accommodation(
            long accommodationId,
            long tripId,
            String name,
            String countryCode,
            String cityName,
            String address,
            String checkInAt,
            String checkOutAt,
            Double latitude,
            Double longitude,
            String source,
            boolean confirmed
    ) {
        this.accommodationId = accommodationId;
        this.tripId = tripId;
        this.name = name;
        this.countryCode = countryCode;
        this.cityName = cityName;
        this.address = address;
        this.checkInAt = checkInAt;
        this.checkOutAt = checkOutAt;
        this.latitude = latitude;
        this.longitude = longitude;
        this.source = source;
        this.confirmed = confirmed;
    }

    public long getAccommodationId() {
        return accommodationId;
    }

    public void setAccommodationId(long accommodationId) {
        this.accommodationId = accommodationId;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCheckInAt() {
        return checkInAt;
    }

    public void setCheckInAt(String checkInAt) {
        this.checkInAt = checkInAt;
    }

    public String getCheckOutAt() {
        return checkOutAt;
    }

    public void setCheckOutAt(String checkOutAt) {
        this.checkOutAt = checkOutAt;
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
