package com.example.mybaghackathon.data.remote.api;

import com.example.mybaghackathon.data.remote.dto.auth.AuthTokenDto;
import com.example.mybaghackathon.data.remote.dto.auth.KakaoLoginRequestDto;
import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.notification.FcmTokenDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// 인증 관련 EC2 서버 API 엔드포인트 정의
public interface AuthApi {

    @POST("api/auth/kakao_login.php")
    Call<ApiResponseDto<AuthTokenDto>> kakaoLogin(@Body KakaoLoginRequestDto body);

    @POST("api/auth/logout.php")
    Call<ApiResponseDto<Object>> logout();

    @POST("api/auth/fcm-token.php")
    Call<ApiResponseDto<Object>> registerFcmToken(@Body FcmTokenDto body);
}
