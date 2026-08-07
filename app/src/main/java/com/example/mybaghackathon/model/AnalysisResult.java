package com.example.mybaghackathon.model;

import java.util.ArrayList;
import java.util.List;

/**
 * GPT Vision 일정 분석 결과 한 건을 나타내는 모델입니다. (analyze.php/confirm.php 응답 1:1 대응)
 *
 * <p>여행 전체를 대표하는 단일 {@link TripSchedule} + 숙소명 문자열 + 반입 제한 목록 +
 * 추천 준비물 목록으로 구성됩니다. 방 생성 전이라 tripId는 없습니다.</p>
 */
public class AnalysisResult {

    private long analysisId;
    private TripSchedule schedule;
    private String accommodationName;
    private List<RestrictedItem> restrictedItems = new ArrayList<>();
    private List<PackingItem> recommendedItems = new ArrayList<>();

    public AnalysisResult() {
    }

    public AnalysisResult(
            long analysisId,
            TripSchedule schedule,
            String accommodationName,
            List<RestrictedItem> restrictedItems,
            List<PackingItem> recommendedItems
    ) {
        this.analysisId = analysisId;
        this.schedule = schedule;
        this.accommodationName = accommodationName;
        setRestrictedItems(restrictedItems);
        setRecommendedItems(recommendedItems);
    }

    public long getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(long analysisId) {
        this.analysisId = analysisId;
    }

    public TripSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(TripSchedule schedule) {
        this.schedule = schedule;
    }

    public String getAccommodationName() {
        return accommodationName;
    }

    public void setAccommodationName(String accommodationName) {
        this.accommodationName = accommodationName;
    }

    public List<RestrictedItem> getRestrictedItems() {
        return restrictedItems;
    }

    public void setRestrictedItems(List<RestrictedItem> restrictedItems) {
        this.restrictedItems = restrictedItems == null
                ? new ArrayList<>()
                : new ArrayList<>(restrictedItems);
    }

    public List<PackingItem> getRecommendedItems() {
        return recommendedItems;
    }

    public void setRecommendedItems(List<PackingItem> recommendedItems) {
        this.recommendedItems = recommendedItems == null
                ? new ArrayList<>()
                : new ArrayList<>(recommendedItems);
    }
}
