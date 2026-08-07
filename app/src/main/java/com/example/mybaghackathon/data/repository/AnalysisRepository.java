package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.model.AnalysisResult;

import java.util.List;

// AI 분석 관련 데이터 접근 규칙을 정의하는 인터페이스
public interface AnalysisRepository {

    // 업로드된 사진들을 GPT Vision으로 분석
    AppResult<AnalysisResult> analyze(List<Long> uploadIds);

    // S07에서 사용자가 고친 필드를 반영해 분석 결과 확정. 안 고친 필드는 null로 넘기면 기존 값 유지됨
    AppResult<AnalysisResult> confirm(
            long analysisId,
            String destinationCountry,
            String destinationCity,
            String startDate,
            String endDate,
            String transportMode,
            String accommodationName
    );
}
