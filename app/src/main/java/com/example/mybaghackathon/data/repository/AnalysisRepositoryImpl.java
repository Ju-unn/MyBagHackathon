package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppError;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.mapper.AnalysisMapper;
import com.example.mybaghackathon.data.remote.api.AnalysisApi;
import com.example.mybaghackathon.data.remote.dto.analysis.AnalysisRequestDto;
import com.example.mybaghackathon.data.remote.dto.analysis.AnalysisResponseDto;
import com.example.mybaghackathon.data.remote.dto.analysis.ConfirmRequestDto;
import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.model.AnalysisResult;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

// AnalysisRepository의 실제 구현체 (AnalysisApi 호출). 네트워크 호출이라 반드시 메인 스레드 밖에서 호출할 것
public class AnalysisRepositoryImpl implements AnalysisRepository {

    private final AnalysisApi analysisApi;

    public AnalysisRepositoryImpl(AnalysisApi analysisApi) {
        this.analysisApi = analysisApi;
    }

    @Override
    public AppResult<AnalysisResult> analyze(List<Long> uploadIds) {
        try {
            Response<ApiResponseDto<AnalysisResponseDto>> response =
                    analysisApi.analyze(new AnalysisRequestDto(uploadIds)).execute();
            return toResult(response);
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    @Override
    public AppResult<AnalysisResult> confirm(
            long analysisId,
            String destinationCountry,
            String destinationCity,
            String startDate,
            String endDate,
            String transportMode,
            String accommodationName
    ) {
        ConfirmRequestDto body = new ConfirmRequestDto(analysisId)
                .setDestinationCountry(destinationCountry)
                .setDestinationCity(destinationCity)
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setTransportMode(transportMode)
                .setAccommodationName(accommodationName);

        try {
            Response<ApiResponseDto<AnalysisResponseDto>> response = analysisApi.confirm(body).execute();
            return toResult(response);
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    private AppResult<AnalysisResult> toResult(Response<ApiResponseDto<AnalysisResponseDto>> response) {
        ApiResponseDto<AnalysisResponseDto> body = response.body();
        if (!response.isSuccessful() || body == null || !body.isSuccess()) {
            return AppResult.failure(toError(response, body));
        }
        return AppResult.success(AnalysisMapper.from(body.getData()));
    }

    private AppError toError(Response<?> response, ApiResponseDto<?> body) {
        String message = body != null ? body.getMessage() : "요청에 실패했습니다.";
        return new AppError(response.code(), message);
    }

    private AppError networkError() {
        return new AppError(0, "네트워크 오류가 발생했습니다.");
    }
}
