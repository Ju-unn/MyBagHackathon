package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.model.NotificationSettings;

// 알림 설정(notification_settings) 관련 데이터 접근 규칙을 정의하는 인터페이스
public interface NotificationSettingsRepository {

    // 내 알림 설정 조회 (S15)
    AppResult<NotificationSettings> getSettings();

    // 바꿀 토글만 값을 채워서 넘기면 되고, 나머지는 null로 두면 기존 값 유지됨
    AppResult<Void> updateSettings(
            Boolean d7Enabled,
            Boolean d3Enabled,
            Boolean d1Enabled,
            Boolean assignmentEnabled,
            Boolean checklistChangeEnabled,
            Boolean weatherEnabled
    );
}
