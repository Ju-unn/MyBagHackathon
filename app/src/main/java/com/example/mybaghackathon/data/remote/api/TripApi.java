package com.example.mybaghackathon.data.remote.api;

import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripCreateRequestDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripDetailResponseDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripInviteDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripJoinRequestDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripJoinResponseDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripListResponseDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripMembersResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

// 여행방 관련 EC2 서버 API 엔드포인트 정의
public interface TripApi {

    // S08 '방 생성 완료' — analysis_id 기반으로 방을 확정 생성
    @POST("api/trips/create.php")
    Call<ApiResponseDto<TripInviteDto>> create(@Body TripCreateRequestDto body);

    // 홈(S03) — 진행중인 내 여행방 목록 (tab 생략 시 서버 기본값 ongoing)
    @GET("api/trips/list.php")
    Call<ApiResponseDto<TripListResponseDto>> list();

    // 보관함(S13/S14) — tab=past로 지난 여행만 조회 (같은 list.php를 재사용, archive.php는 폐지됨)
    @GET("api/trips/list.php")
    Call<ApiResponseDto<TripListResponseDto>> archive(@Query("tab") String tab);

    // 방 상세(S09)
    @GET("api/trips/detail.php")
    Call<ApiResponseDto<TripDetailResponseDto>> detail(@Query("trip_id") long tripId);

    // 참여자 목록
    @GET("api/trips/members.php")
    Call<ApiResponseDto<TripMembersResponseDto>> members(@Query("trip_id") long tripId);

    // 초대코드로 참여
    @POST("api/trips/join.php")
    Call<ApiResponseDto<TripJoinResponseDto>> join(@Body TripJoinRequestDto body);
}
