package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppError;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.mapper.PackingItemMapper;
import com.example.mybaghackathon.data.remote.api.PackingApi;
import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistAssignRequestDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistCheckResponseDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistCreateRequestDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistCreateResponseDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistGenerateRequestDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistItemIdRequestDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistListResponseDto;
import com.example.mybaghackathon.data.remote.dto.packing.ChecklistUpdateRequestDto;
import com.example.mybaghackathon.model.PackingItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

// PackingRepository의 실제 구현체 (PackingApi 호출). 네트워크 호출이라 반드시 메인 스레드 밖에서 호출할 것
public class PackingRepositoryImpl implements PackingRepository {

    private final PackingApi packingApi;

    public PackingRepositoryImpl(PackingApi packingApi) {
        this.packingApi = packingApi;
    }

    @Override
    public AppResult<List<PackingItem>> listItems(long tripId, String since) {
        try {
            Response<ApiResponseDto<ChecklistListResponseDto>> response =
                    packingApi.list(tripId, since).execute();
            ApiResponseDto<ChecklistListResponseDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            return AppResult.success(PackingItemMapper.fromChecklistItems(body.getData().getItems()));
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<Long> addItem(long tripId, String itemName, String category, String priority, String scope) {
        try {
            Response<ApiResponseDto<ChecklistCreateResponseDto>> response = packingApi.create(
                    new ChecklistCreateRequestDto(tripId, itemName, category, priority, scope)
            ).execute();
            ApiResponseDto<ChecklistCreateResponseDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            return AppResult.success(body.getData().getPackingItemId());
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<Boolean> toggleCheck(long itemId) {
        try {
            Response<ApiResponseDto<ChecklistCheckResponseDto>> response =
                    packingApi.check(new ChecklistItemIdRequestDto(itemId)).execute();
            ApiResponseDto<ChecklistCheckResponseDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            return AppResult.success(body.getData().isCompleted());
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<Void> assign(long itemId, Long assigneeUserId) {
        try {
            Response<ApiResponseDto<Object>> response =
                    packingApi.assign(new ChecklistAssignRequestDto(itemId, assigneeUserId)).execute();
            ApiResponseDto<Object> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            return AppResult.success(null);
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<Void> assignMultiple(long itemId, List<Long> assigneeUserIds) {
        try {
            Response<ApiResponseDto<Object>> response =
                    packingApi.assign(new ChecklistAssignRequestDto(itemId, assigneeUserIds)).execute();
            ApiResponseDto<Object> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            return AppResult.success(null);
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<Void> updateItem(long itemId, String itemName, String category, String priority, String scope) {
        ChecklistUpdateRequestDto body = new ChecklistUpdateRequestDto(itemId)
                .setItemName(itemName)
                .setCategory(category)
                .setPriority(priority)
                .setScope(scope);

        try {
            Response<ApiResponseDto<Object>> response = packingApi.update(body).execute();
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
    public AppResult<Void> deleteItem(long itemId) {
        try {
            Response<ApiResponseDto<Object>> response =
                    packingApi.delete(new ChecklistItemIdRequestDto(itemId)).execute();
            ApiResponseDto<Object> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            return AppResult.success(null);
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<Void> generate(long tripId, long analysisId, Map<String, String> itemScopeByName) {
        List<ChecklistGenerateRequestDto.Item> items = new ArrayList<>();
        if (itemScopeByName != null) {
            for (Map.Entry<String, String> entry : itemScopeByName.entrySet()) {
                items.add(new ChecklistGenerateRequestDto.Item(entry.getKey(), entry.getValue()));
            }
        }

        try {
            Response<ApiResponseDto<Object>> response =
                    packingApi.generate(new ChecklistGenerateRequestDto(tripId, analysisId, items)).execute();
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
        String message = body != null ? body.getMessage() : "요청에 실패했습니다.";
        return new AppError(response.code(), message);
    }

    private AppError networkError() {
        return new AppError(0, "네트워크 오류가 발생했습니다.");
    }
}
