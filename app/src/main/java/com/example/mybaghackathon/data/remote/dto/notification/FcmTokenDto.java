package com.example.mybaghackathon.data.remote.dto.notification;

import com.google.gson.annotations.SerializedName;

// POST /api/auth/fcm-token.php 요청 바디
public class FcmTokenDto {

    private final String token;

    @SerializedName("device_id")
    private final String deviceId;

    private final String platform;

    @SerializedName("app_version")
    private final String appVersion;

    public FcmTokenDto(String token, String deviceId, String platform, String appVersion) {
        this.token = token;
        this.deviceId = deviceId;
        this.platform = platform;
        this.appVersion = appVersion;
    }
}
