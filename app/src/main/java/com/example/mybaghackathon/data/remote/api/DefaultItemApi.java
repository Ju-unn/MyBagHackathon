package com.example.mybaghackathon.data.remote.api;

import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.defaultitem.DefaultItemCreateRequestDto;
import com.example.mybaghackathon.data.remote.dto.defaultitem.DefaultItemCreateResponseDto;
import com.example.mybaghackathon.data.remote.dto.defaultitem.DefaultItemIdRequestDto;
import com.example.mybaghackathon.data.remote.dto.defaultitem.DefaultItemListResponseDto;
import com.example.mybaghackathon.data.remote.dto.defaultitem.DefaultItemUpdateRequestDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

// 내 기본 물품(user_default_items) 관련 EC2 서버 API 엔드포인트 정의
public interface DefaultItemApi {

    // 내 기본 물품 목록 조회 (S15ItemsWrap)
    @GET("api/profile/default-items/list.php")
    Call<ApiResponseDto<DefaultItemListResponseDto>> list();

    // 항목 추가
    @POST("api/profile/default-items/create.php")
    Call<ApiResponseDto<DefaultItemCreateResponseDto>> create(@Body DefaultItemCreateRequestDto body);

    // 항목 수정
    @POST("api/profile/default-items/update.php")
    Call<ApiResponseDto<Object>> update(@Body DefaultItemUpdateRequestDto body);

    // 항목 삭제(소프트 삭제)
    @POST("api/profile/default-items/delete.php")
    Call<ApiResponseDto<Object>> delete(@Body DefaultItemIdRequestDto body);
}
