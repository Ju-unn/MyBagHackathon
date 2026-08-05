package com.example.mybaghackathon.data.remote.dto.common;

// 서버 공통 응답 포맷 {"success","message","data"} 래퍼. EC2 PHP ApiResponse::send()와 1:1 대응
public class ApiResponseDto<T> {

    private boolean success;
    private String message;
    private T data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
