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

    public static <T> AppResult<T> success(T data) {
        return new AppResult<>(true, data, null);
    }

    public static <T> AppResult<T> failure(AppError error) {
        return new AppResult<>(false, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public AppError getError() {
        return error;
    }
}
