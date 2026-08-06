package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppError;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.mapper.TripMapper;
import com.example.mybaghackathon.data.remote.api.TripApi;
import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripCreateRequestDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripDetailResponseDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripInviteDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripJoinRequestDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripJoinResponseDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripListResponseDto;
import com.example.mybaghackathon.data.remote.dto.trip.TripMembersResponseDto;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.TripInvite;
import com.example.mybaghackathon.model.TripMember;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

// TripRepository의 실제 구현체 (TripApi 호출). 네트워크 호출이라 반드시 메인 스레드 밖에서 호출할 것
public class TripRepositoryImpl implements TripRepository {

    private final TripApi tripApi;

    public TripRepositoryImpl(TripApi tripApi) {
        this.tripApi = tripApi;
    }

    @Override
    public AppResult<TripInvite> createTrip(
            String tripName,
            int expectedMemberCount,
            long analysisId,
            Map<String, String> itemScopeByName
    ) {
        List<TripCreateRequestDto.Item> items = new ArrayList<>();
        if (itemScopeByName != null) {
            for (Map.Entry<String, String> entry : itemScopeByName.entrySet()) {
                items.add(new TripCreateRequestDto.Item(entry.getKey(), entry.getValue()));
            }
        }
        TripCreateRequestDto body = new TripCreateRequestDto(tripName, expectedMemberCount, analysisId, items);

        try {
            Response<ApiResponseDto<TripInviteDto>> response = tripApi.create(body).execute();
            ApiResponseDto<TripInviteDto> apiBody = response.body();
            if (!response.isSuccessful() || apiBody == null || !apiBody.isSuccess()) {
                return AppResult.failure(toError(response, apiBody));
            }
            TripInviteDto dto = apiBody.getData();
            return AppResult.success(new TripInvite(dto.getTripId(), dto.getInviteCode()));
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<List<Trip>> listMyTrips() {
        try {
            Response<ApiResponseDto<TripListResponseDto>> response = tripApi.list().execute();
            ApiResponseDto<TripListResponseDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            return AppResult.success(TripMapper.fromList(body.getData().getTrips()));
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<List<Trip>> listArchivedTrips() {
        try {
            Response<ApiResponseDto<TripListResponseDto>> response = tripApi.archive("past").execute();
            ApiResponseDto<TripListResponseDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            return AppResult.success(TripMapper.fromList(body.getData().getTrips()));
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<Trip> getTripDetail(long tripId) {
        try {
            Response<ApiResponseDto<TripDetailResponseDto>> response = tripApi.detail(tripId).execute();
            ApiResponseDto<TripDetailResponseDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            TripDetailResponseDto data = body.getData();
            return AppResult.success(TripMapper.from(data.getTrip(), data.getMembers()));
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<List<TripMember>> listMembers(long tripId) {
        try {
            Response<ApiResponseDto<TripMembersResponseDto>> response = tripApi.members(tripId).execute();
            ApiResponseDto<TripMembersResponseDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            return AppResult.success(TripMapper.fromMembers(body.getData().getMembers()));
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<Long> joinByCode(String inviteCode) {
        try {
            Response<ApiResponseDto<TripJoinResponseDto>> response =
                    tripApi.join(new TripJoinRequestDto(inviteCode)).execute();
            ApiResponseDto<TripJoinResponseDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }
            return AppResult.success(body.getData().getTripId());
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
