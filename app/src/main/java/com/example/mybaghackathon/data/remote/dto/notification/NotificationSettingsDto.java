package com.example.mybaghackathon.data.remote.dto.notification;

import com.google.gson.annotations.SerializedName;

// GET /api/profile/notification-settings/get.php 응답
public class NotificationSettingsDto {

    @SerializedName("user_id")
    private long userId;

    @SerializedName("d7_enabled")
    private boolean d7Enabled;

    @SerializedName("d3_enabled")
    private boolean d3Enabled;

    @SerializedName("d1_enabled")
    private boolean d1Enabled;

    @SerializedName("assignment_enabled")
    private boolean assignmentEnabled;

    @SerializedName("checklist_change_enabled")
    private boolean checklistChangeEnabled;

    @SerializedName("weather_enabled")
    private boolean weatherEnabled;

    public long getUserId() {
        return userId;
    }

    public boolean isD7Enabled() {
        return d7Enabled;
    }

    public boolean isD3Enabled() {
        return d3Enabled;
    }

    public boolean isD1Enabled() {
        return d1Enabled;
    }

    public boolean isAssignmentEnabled() {
        return assignmentEnabled;
    }

    public boolean isChecklistChangeEnabled() {
        return checklistChangeEnabled;
    }

    public boolean isWeatherEnabled() {
        return weatherEnabled;
    }
}
