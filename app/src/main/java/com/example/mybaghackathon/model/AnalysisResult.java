package com.example.mybaghackathon.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 여러 장의 여행 자료를 분석한 한 번의 AI 실행 결과를 나타내는 모델입니다.
 *
 * <p>분석 상태와 구조화된 일정, 숙소, 추천 준비물을 함께 보관합니다. 분석 실패
 * 시에는 errorCode와 errorMessage를 사용하고, 결과 목록이 null이면 빈 목록으로
 * 처리합니다.</p>
 */
public class AnalysisResult {

    private long analysisId;
    private long tripId;
    private String status;
    private String modelName;
    private String promptVersion;
    private List<TripSchedule> schedules = new ArrayList<>();
    private List<Accommodation> accommodations = new ArrayList<>();
    private List<PackingItem> suggestedItems = new ArrayList<>();
    private String errorCode;
    private String errorMessage;
    private String completedAt;

    public AnalysisResult() {
    }

    public AnalysisResult(
            long analysisId,
            long tripId,
            String status,
            String modelName,
            String promptVersion,
            List<TripSchedule> schedules,
            List<Accommodation> accommodations,
            List<PackingItem> suggestedItems,
            String errorCode,
            String errorMessage,
            String completedAt
    ) {
        this.analysisId = analysisId;
        this.tripId = tripId;
        this.status = status;
        this.modelName = modelName;
        this.promptVersion = promptVersion;
        setSchedules(schedules);
        setAccommodations(accommodations);
        setSuggestedItems(suggestedItems);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.completedAt = completedAt;
    }

    public long getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(long analysisId) {
        this.analysisId = analysisId;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    public List<TripSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<TripSchedule> schedules) {
        this.schedules = schedules == null
                ? new ArrayList<>()
                : new ArrayList<>(schedules);
    }

    public List<Accommodation> getAccommodations() {
        return accommodations;
    }

    public void setAccommodations(List<Accommodation> accommodations) {
        this.accommodations = accommodations == null
                ? new ArrayList<>()
                : new ArrayList<>(accommodations);
    }

    public List<PackingItem> getSuggestedItems() {
        return suggestedItems;
    }

    public void setSuggestedItems(List<PackingItem> suggestedItems) {
        this.suggestedItems = suggestedItems == null
                ? new ArrayList<>()
                : new ArrayList<>(suggestedItems);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }
}
