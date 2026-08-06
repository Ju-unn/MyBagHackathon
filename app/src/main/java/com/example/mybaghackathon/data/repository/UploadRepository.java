package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppResult;

import java.io.File;
import java.util.List;

// 여러 장의 일정·숙소 이미지 업로드 데이터 접근 규칙
public interface UploadRepository {

    // uploadType: ITINERARY, ACCOMMODATION, EXTRA. photos는 로컬에 저장된(리사이즈 완료된) 이미지 파일들
    AppResult<List<Long>> upload(List<File> photos, String uploadType);
}
