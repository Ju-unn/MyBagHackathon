package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppError;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.local.TokenStorage;
import com.example.mybaghackathon.data.local.UserStorage;
import com.example.mybaghackathon.data.mapper.UserMapper;
import com.example.mybaghackathon.data.remote.api.AuthApi;
import com.example.mybaghackathon.data.remote.dto.auth.AuthTokenDto;
import com.example.mybaghackathon.data.remote.dto.auth.KakaoLoginRequestDto;
import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.notification.FcmTokenDto;
import com.example.mybaghackathon.model.User;

import java.io.IOException;

import retrofit2.Response;

// AuthRepository의 실제 구현체 (AuthApi 호출). 네트워크 호출이라 반드시 메인 스레드 밖에서 호출할 것
public class AuthRepositoryImpl implements AuthRepository {

    private final AuthApi authApi;
    private final TokenStorage tokenStorage;
    private final UserStorage userStorage;

    public AuthRepositoryImpl(AuthApi authApi, TokenStorage tokenStorage, UserStorage userStorage) {
        this.authApi = authApi;
        this.tokenStorage = tokenStorage;
        this.userStorage = userStorage;
    }

    // 카카오 로그인 API 호출 → 성공 시 JWT 저장 후 User 반환, 실패 시 에러 반환
    @Override
    public AppResult<User> loginWithKakao(String kakaoAccessToken) {
        try {
            Response<ApiResponseDto<AuthTokenDto>> response =
                    authApi.kakaoLogin(new KakaoLoginRequestDto(kakaoAccessToken)).execute();

            ApiResponseDto<AuthTokenDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }

            AuthTokenDto data = body.getData();
            tokenStorage.saveToken(data.getToken());
            User user = UserMapper.from(data.getUser());
            userStorage.saveUser(user);
            return AppResult.success(user);
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    // 로그아웃 API 호출 → 성공 시 저장된 토큰 삭제
    @Override
    public AppResult<Void> logout() {
        try {
            Response<ApiResponseDto<Object>> response = authApi.logout().execute();
            ApiResponseDto<Object> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }

            tokenStorage.clearToken();
            userStorage.clearUser();
            return AppResult.success(null);
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    // FCM 토큰 등록 API 호출
    @Override
    public AppResult<Void> registerFcmToken(String token, String deviceId, String platform, String appVersion) {
        try {
            Response<ApiResponseDto<Object>> response =
                    authApi.registerFcmToken(new FcmTokenDto(token, deviceId, platform, appVersion)).execute();

            ApiResponseDto<Object> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }

            return AppResult.success(null);
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    // 실패 응답에서 상태코드와 메시지를 뽑아 AppError로 변환한다
    private AppError toError(Response<?> response, ApiResponseDto<?> body) {
        String message = body != null ? body.getMessage() : "요청에 실패했습니다.";
        return new AppError(response.code(), message);
    }

    // IOException(네트워크 자체 실패) 상황을 위한 공통 에러를 만든다
    private AppError networkError() {
        return new AppError(0, "네트워크 오류가 발생했습니다.");
    }
}
