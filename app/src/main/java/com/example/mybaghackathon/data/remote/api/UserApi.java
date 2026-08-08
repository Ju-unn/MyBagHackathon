package com.example.mybaghackathon.data.remote.api;

import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.user.UserNicknameUpdateRequestDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// 내 프로필(user) 관련 EC2 서버 API 엔드포인트 정의
public interface UserApi {

    // 닉네임 수정
    @POST("api/profile/me/update.php")
    Call<ApiResponseDto<Object>> updateNickname(@Body UserNicknameUpdateRequestDto body);
}
