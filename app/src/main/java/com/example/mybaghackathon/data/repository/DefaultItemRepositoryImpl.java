package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppError;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.mapper.DefaultItemMapper;
import com.example.mybaghackathon.data.remote.api.DefaultItemApi;
import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.defaultitem.DefaultItemCreateRequestDto;
import com.example.mybaghackathon.data.remote.dto.defaultitem.DefaultItemCreateResponseDto;
import com.example.mybaghackathon.data.remote.dto.defaultitem.DefaultItemIdRequestDto;
import com.example.mybaghackathon.data.remote.dto.defaultitem.DefaultItemListResponseDto;
import com.example.mybaghackathon.data.remote.dto.defaultitem.DefaultItemUpdateRequestDto;
import com.example.mybaghackathon.model.UserDefaultItem;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

// DefaultItemRepository의 실제 구현체 (DefaultItemApi 호출). 네트워크 호출이라 반드시 메인 스레드 밖에서 호출할 것
public class DefaultItemRepositoryImpl implements DefaultItemRepository {

    private final DefaultItemApi defaultItemApi;

    public DefaultItemRepositoryImpl(DefaultItemApi defaultItemApi) {
        this.defaultItemApi = defaultItemApi;
    }

    @Override
    public AppResult<List<UserDefaultItem>> listItems() {
        try {
            Response<ApiResponseDto<DefaultItemListResponseDto>> response = defaultItemApi.list().execute();
            ApiResponseDto<DefaultItemListResponseDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            return AppResult.success(DefaultItemMapper.from(body.getData().getItems()));
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<Long> addItem(String itemName, String category, String priority) {
        try {
            Response<ApiResponseDto<DefaultItemCreateResponseDto>> response = defaultItemApi.create(
                    new DefaultItemCreateRequestDto(itemName, category, priority)
            ).execute();
            ApiResponseDto<DefaultItemCreateResponseDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            return AppResult.success(body.getData().getDefaultItemId());
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<Void> updateItem(long defaultItemId, String itemName, String category, String priority) {
        DefaultItemUpdateRequestDto body = new DefaultItemUpdateRequestDto(defaultItemId)
                .setItemName(itemName)
                .setCategory(category)
                .setDefaultPriority(priority);

        try {
            Response<ApiResponseDto<Object>> response = defaultItemApi.update(body).execute();
            ApiResponseDto<Object> responseBody = response.body();
            if (!response.isSuccessful() || responseBody == null || !responseBody.isSuccess()) {
                return AppResult.failure(toError(response, responseBody));
            }
            return AppResult.success(null);
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<Void> deleteItem(long defaultItemId) {
        try {
            Response<ApiResponseDto<Object>> response =
                    defaultItemApi.delete(new DefaultItemIdRequestDto(defaultItemId)).execute();
            ApiResponseDto<Object> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            return AppResult.success(null);
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    private AppError toError(Response<?> response, ApiResponseDto<?> body) {
        String message = body != null ? body.getMessage() : parseErrorMessage(response);
        return new AppError(response.code(), message);
    }

    // body가 null인 건 실패 응답(4xx/5xx)이라 Retrofit이 body()를 채워주지 않기 때문 —
    // 실제 서버 메시지는 errorBody()에 같은 {success,message,data} 포맷으로 들어있다
    private String parseErrorMessage(Response<?> response) {
        if (response.errorBody() != null) {
            try {
                ApiResponseDto<?> errorBody =
                        new Gson().fromJson(response.errorBody().string(), ApiResponseDto.class);
                if (errorBody != null && errorBody.getMessage() != null) {
                    return errorBody.getMessage();
                }
            } catch (IOException ignored) {
                // 아래 기본 메시지로 폴백
            }
        }
        return "요청에 실패했습니다.";
    }

    private AppError networkError() {
        return new AppError(0, "네트워크 오류가 발생했습니다.");
    }
}
