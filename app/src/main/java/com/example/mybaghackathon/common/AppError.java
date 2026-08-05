package com.example.mybaghackathon.common;

// 앱 전역에서 공통으로 사용하는 오류 타입
public final class AppError {

    private final int httpStatus;
    private final String message;

    public AppError(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
