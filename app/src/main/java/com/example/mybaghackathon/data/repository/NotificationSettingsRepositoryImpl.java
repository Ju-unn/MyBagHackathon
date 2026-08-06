package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppError;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.mapper.NotificationMapper;
import com.example.mybaghackathon.data.remote.api.NotificationSettingsApi;
import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.notification.NotificationSettingsDto;
import com.example.mybaghackathon.data.remote.dto.notification.NotificationSettingsUpdateRequestDto;
import com.example.mybaghackathon.model.NotificationSettings;

import java.io.IOException;

import retrofit2.Response;

// NotificationSettingsRepository의 실제 구현체 (NotificationSettingsApi 호출). 네트워크 호출이라 반드시 메인 스레드 밖에서 호출할 것
public class NotificationSettingsRepositoryImpl implements NotificationSettingsRepository {

    private final NotificationSettingsApi notificationSettingsApi;

    public NotificationSettingsRepositoryImpl(NotificationSettingsApi notificationSettingsApi) {
        this.notificationSettingsApi = notificationSettingsApi;
    }

    @Override
    public AppResult<NotificationSettings> getSettings() {
        try {
            Response<ApiResponseDto<NotificationSettingsDto>> response = notificationSettingsApi.get().execute();
            ApiResponseDto<NotificationSettingsDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            return AppResult.success(NotificationMapper.from(body.getData()));
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<Void> updateSettings(
            Boolean d7Enabled,
            Boolean d3Enabled,
            Boolean d1Enabled,
            Boolean assignmentEnabled,
            Boolean checklistChangeEnabled,
            Boolean weatherEnabled
    ) {
        NotificationSettingsUpdateRequestDto body = new NotificationSettingsUpdateRequestDto()
                .setD7Enabled(d7Enabled)
                .setD3Enabled(d3Enabled)
                .setD1Enabled(d1Enabled)
                .setAssignmentEnabled(assignmentEnabled)
                .setChecklistChangeEnabled(checklistChangeEnabled)
                .setWeatherEnabled(weatherEnabled);

        try {
            Response<ApiResponseDto<Object>> response = notificationSettingsApi.update(body).execute();
            ApiResponseDto<Object> responseBody = response.body();
            if (!response.isSuccessful() || responseBody == null || !responseBody.isSuccess()) {
                return AppResult.failure(toError(response, responseBody));
            }
            return AppResult.success(null);
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    private AppError toError(Response<?> response, ApiResponseDto<?> body) {
        String message = body != null ? body.getMessage() : "요청에 실패했습니다.";
        return new AppError(response.code(), message);
    }

    private AppError networkError() {
        return new AppError(0, "네트워크 오류가 발생했습니다.");
    }
}
