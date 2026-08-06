package com.example.mybaghackathon.data.remote.api;

import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistAssignRequestDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistCheckResponseDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistCreateRequestDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistCreateResponseDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistGenerateRequestDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistItemIdRequestDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistListResponseDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistUpdateRequestDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

// 체크리스트(packing_items) 관련 EC2 서버 API 엔드포인트 정의
public interface PackingApi {

    // 델타 조회(S11/S12) — since가 null이면 활성 항목 전체
    @GET("api/checklist/list.php")
    Call<ApiResponseDto<ChecklistListResponseDto>> list(@Query("trip_id") long tripId, @Query("since") String since);

    // BS02 항목 직접 추가
    @POST("api/checklist/create.php")
    Call<ApiResponseDto<ChecklistCreateResponseDto>> create(@Body ChecklistCreateRequestDto body);

    // 체크/해제 토글
    @POST("api/checklist/check.php")
    Call<ApiResponseDto<ChecklistCheckResponseDto>> check(@Body ChecklistItemIdRequestDto body);

    // 담당자 지정/해제 — 본인만 가능(서버 검증)
    @POST("api/checklist/assign.php")
    Call<ApiResponseDto<Object>> assign(@Body ChecklistAssignRequestDto body);

    // 항목 수정
    @POST("api/checklist/update.php")
    Call<ApiResponseDto<Object>> update(@Body ChecklistUpdateRequestDto body);

    // 항목 삭제(소프트 삭제)
    @POST("api/checklist/delete.php")
    Call<ApiResponseDto<Object>> delete(@Body ChecklistItemIdRequestDto body);

    // 재분석 후 재생성 — 방장 전용(F-LGTOBW)
    @POST("api/checklist/generate.php")
    Call<ApiResponseDto<Object>> generate(@Body ChecklistGenerateRequestDto body);
}
