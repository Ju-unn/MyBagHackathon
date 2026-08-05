package com.example.mybaghackathon.data.remote.dto.common;

// 서버 공통 응답 포맷 {"success","message","data"} 래퍼. EC2 PHP ApiResponse::send()와 1:1 대응
public class ApiResponseDto<T> {

    private boolean success;
    private String message;
    private T data;

    // 서버 처리 성공 여부를 반환한다
    public boolean isSuccess() {
        return success;
    }

    // 서버가 보낸 안내 메시지를 반환한다
    public String getMessage() {
        return message;
    }

    // 성공 시 실제 데이터를 반환한다 (엔드포인트마다 타입이 다름)
    public T getData() {
        return data;
    }
}
