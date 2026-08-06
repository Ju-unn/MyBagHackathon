package com.example.mybaghackathon.common;

// 앱 전역에서 공통으로 사용하는 오류 타입
public final class AppError {

    private final int httpStatus;
    private final String message;

    // HTTP 상태코드와 에러 메시지를 함께 담는다 (네트워크 자체 실패는 httpStatus=0)
    public AppError(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    // 실패한 요청의 HTTP 상태코드를 반환한다
    public int getHttpStatus() {
        return httpStatus;
    }

    // 사용자에게 보여줄 에러 메시지를 반환한다
    public String getMessage() {
        return message;
    }
}
