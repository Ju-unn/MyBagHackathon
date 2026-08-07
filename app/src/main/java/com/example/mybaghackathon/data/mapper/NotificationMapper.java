package com.example.mybaghackathon.data.mapper;

import com.example.mybaghackathon.data.remote.dto.notification.NotificationSettingsDto;
import com.example.mybaghackathon.model.NotificationSettings;

// 알림 설정 DTO를 Android Model로 변환하는 Mapper
public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationSettings from(NotificationSettingsDto dto) {
        return new NotificationSettings(
                dto.getUserId(),
                dto.isD7Enabled(),
                dto.isD3Enabled(),
                dto.isD1Enabled(),
                dto.isAssignmentEnabled(),
                dto.isChecklistChangeEnabled(),
                dto.isWeatherEnabled()
        );
    }
}
