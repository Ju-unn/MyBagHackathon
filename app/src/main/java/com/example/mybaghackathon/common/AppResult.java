package com.example.mybaghackathon.common;

// 성공/실패를 함께 표현하는 공통 결과 래퍼
public final class AppResult<T> {

    private final boolean success;
    private final T data;
    private final AppError error;

    private AppResult(boolean success, T data, AppError error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    // 성공 결과를 만든다
    public static <T> AppResult<T> success(T data) {
        return new AppResult<>(true, data, null);
    }

    // 실패 결과를 만든다
    public static <T> AppResult<T> failure(AppError error) {
        return new AppResult<>(false, null, error);
    }

    // 성공 여부를 반환한다
    public boolean isSuccess() {
        return success;
    }

    // 성공 시 데이터를 반환한다 (실패면 null)
    public T getData() {
        return data;
    }

    // 실패 시 에러를 반환한다 (성공이면 null)
    public AppError getError() {
        return error;
    }
}
