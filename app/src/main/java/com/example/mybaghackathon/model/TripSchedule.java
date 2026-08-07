package com.example.mybaghackathon.model;

/**
 * 여행 일정을 나타내는 모델입니다. (README 8번 "최종 일정 Model" 통일 방침)
 *
 * <p>GPT Vision 분석(analyze.php/confirm.php)이 여행 전체를 대표하는 목적지 1개 +
 * 기간 1쌍만 반환하므로, 날짜별 세부 항목이 아니라 여행 전체 기준 플랫한 정보로
 * 구성합니다. 방 생성(trips/create.php) 이후에는 {@code trips} 테이블의
 * destination_country/city, start_date/end_date와 동일한 값입니다.</p>
 */
public class TripSchedule {

    private String destinationCountry;
    private String destinationCity;
    private String startDate;
    private String endDate;
    private String transportMode;
    private Boolean isOverseas;

    public TripSchedule() {
    }

    public TripSchedule(
            String destinationCountry,
            String destinationCity,
            String startDate,
            String endDate,
            String transportMode,
            Boolean isOverseas
    ) {
        this.destinationCountry = destinationCountry;
        this.destinationCity = destinationCity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.transportMode = transportMode;
        this.isOverseas = isOverseas;
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

    // AIR, OTHER 중 하나 (또는 null — 사진에서 못 읽었을 때)
    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }

    public Boolean getIsOverseas() {
        return isOverseas;
    }

    public void setIsOverseas(Boolean isOverseas) {
        this.isOverseas = isOverseas;
    }
}
