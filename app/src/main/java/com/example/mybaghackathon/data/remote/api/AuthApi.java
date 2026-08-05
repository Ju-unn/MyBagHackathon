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

    // 카카오 access_token 검증 후 자체 JWT 발급받기
    @POST("api/auth/kakao_login.php")
    Call<ApiResponseDto<AuthTokenDto>> kakaoLogin(@Body KakaoLoginRequestDto body);

    // 로그인 토큰 유효성 확인 후 로그아웃 처리
    @POST("api/auth/logout.php")
    Call<ApiResponseDto<Object>> logout();

    // 이 기기의 FCM 토큰을 서버에 등록/갱신
    @POST("api/auth/fcm-token.php")
    Call<ApiResponseDto<Object>> registerFcmToken(@Body FcmTokenDto body);
}
