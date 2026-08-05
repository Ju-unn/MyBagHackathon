package com.example.mybaghackathon.model;

/**
 * 사용자가 선택한 여행 준비 알림 옵션을 나타내는 모델입니다.
 */
public class NotificationSettings {

    private long userId;
    private boolean dDay7Enabled;
    private boolean dDay3Enabled;
    private boolean dDay1Enabled;
    private boolean assignmentEnabled;
    private boolean checklistChangeEnabled;
    private boolean weatherEnabled;

    public NotificationSettings() {
    }

    public NotificationSettings(
            long userId,
            boolean dDay7Enabled,
            boolean dDay3Enabled,
            boolean dDay1Enabled,
            boolean assignmentEnabled,
            boolean checklistChangeEnabled,
            boolean weatherEnabled
    ) {
        this.userId = userId;
        this.dDay7Enabled = dDay7Enabled;
        this.dDay3Enabled = dDay3Enabled;
        this.dDay1Enabled = dDay1Enabled;
        this.assignmentEnabled = assignmentEnabled;
        this.checklistChangeEnabled = checklistChangeEnabled;
        this.weatherEnabled = weatherEnabled;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public boolean isDDay7Enabled() {
        return dDay7Enabled;
    }

    public void setDDay7Enabled(boolean dDay7Enabled) {
        this.dDay7Enabled = dDay7Enabled;
    }

    public boolean isDDay3Enabled() {
        return dDay3Enabled;
    }

    public void setDDay3Enabled(boolean dDay3Enabled) {
        this.dDay3Enabled = dDay3Enabled;
    }

    public boolean isDDay1Enabled() {
        return dDay1Enabled;
    }

    public void setDDay1Enabled(boolean dDay1Enabled) {
        this.dDay1Enabled = dDay1Enabled;
    }

    public boolean isAssignmentEnabled() {
        return assignmentEnabled;
    }

    public void setAssignmentEnabled(boolean assignmentEnabled) {
        this.assignmentEnabled = assignmentEnabled;
    }

    public boolean isChecklistChangeEnabled() {
        return checklistChangeEnabled;
    }

    public void setChecklistChangeEnabled(boolean checklistChangeEnabled) {
        this.checklistChangeEnabled = checklistChangeEnabled;
    }

    public boolean isWeatherEnabled() {
        return weatherEnabled;
    }

    public void setWeatherEnabled(boolean weatherEnabled) {
        this.weatherEnabled = weatherEnabled;
    }
}
