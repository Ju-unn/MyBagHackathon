package com.example.mybaghackathon.data.remote.api;

import com.example.mybaghackathon.data.remote.dto.analysis.AnalysisRequestDto;
import com.example.mybaghackathon.data.remote.dto.analysis.AnalysisResponseDto;
import com.example.mybaghackathon.data.remote.dto.analysis.ConfirmRequestDto;
import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// AI 분석 관련 EC2 서버 API 엔드포인트 정의
public interface AnalysisApi {

    // 업로드된 사진들을 GPT Vision으로 분석
    @POST("api/itinerary/analyze.php")
    Call<ApiResponseDto<AnalysisResponseDto>> analyze(@Body AnalysisRequestDto body);

    // S07에서 사용자가 고친 필드를 반영해 분석 결과를 확정
    @POST("api/itinerary/confirm.php")
    Call<ApiResponseDto<AnalysisResponseDto>> confirm(@Body ConfirmRequestDto body);
}
