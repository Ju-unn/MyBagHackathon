package com.example.mybaghackathon.data.remote.dto.notification;

import com.google.gson.annotations.SerializedName;

/**
 * POST /api/profile/notification-settings/update.php 요청 바디.
 *
 * DefaultItemUpdateRequestDto와 같은 방식: 안 바꿀 필드는 setter를 호출하지 않으면
 * null(Boolean 박싱 타입)로 남고, Gson 기본 설정상 JSON에서 아예 빠져 서버가 기존 값을 유지한다.
 */
public class NotificationSettingsUpdateRequestDto {

    @SerializedName("d7_enabled")
    private Boolean d7Enabled;

    @SerializedName("d3_enabled")
    private Boolean d3Enabled;

    @SerializedName("d1_enabled")
    private Boolean d1Enabled;

    @SerializedName("assignment_enabled")
    private Boolean assignmentEnabled;

    @SerializedName("checklist_change_enabled")
    private Boolean checklistChangeEnabled;

    @SerializedName("weather_enabled")
    private Boolean weatherEnabled;

    public NotificationSettingsUpdateRequestDto setD7Enabled(Boolean d7Enabled) {
        this.d7Enabled = d7Enabled;
        return this;
    }

    public NotificationSettingsUpdateRequestDto setD3Enabled(Boolean d3Enabled) {
        this.d3Enabled = d3Enabled;
        return this;
    }

    public NotificationSettingsUpdateRequestDto setD1Enabled(Boolean d1Enabled) {
        this.d1Enabled = d1Enabled;
        return this;
    }

    public NotificationSettingsUpdateRequestDto setAssignmentEnabled(Boolean assignmentEnabled) {
        this.assignmentEnabled = assignmentEnabled;
        return this;
    }

    public NotificationSettingsUpdateRequestDto setChecklistChangeEnabled(Boolean checklistChangeEnabled) {
        this.checklistChangeEnabled = checklistChangeEnabled;
        return this;
    }

    public NotificationSettingsUpdateRequestDto setWeatherEnabled(Boolean weatherEnabled) {
        this.weatherEnabled = weatherEnabled;
        return this;
    }
}
