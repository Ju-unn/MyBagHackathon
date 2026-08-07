package com.example.mybaghackathon.data.remote.api;

import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.notification.NotificationSettingsDto;
import com.example.mybaghackathon.data.remote.dto.notification.NotificationSettingsUpdateRequestDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

// 알림 설정(notification_settings) 관련 EC2 서버 API 엔드포인트 정의
public interface NotificationSettingsApi {

    // 내 알림 설정 조회 (S15)
    @GET("api/profile/notification-settings/get.php")
    Call<ApiResponseDto<NotificationSettingsDto>> get();

    // 내 알림 설정 수정 — 바꿀 토글만 채워서 보내면 나머지는 기존 값 유지
    @POST("api/profile/notification-settings/update.php")
    Call<ApiResponseDto<Object>> update(@Body NotificationSettingsUpdateRequestDto body);
}
