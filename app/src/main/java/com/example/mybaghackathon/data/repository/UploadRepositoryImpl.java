package com.example.mybaghackathon.data.repository;

import com.example.mybaghackathon.common.AppError;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.remote.api.UploadApi;
import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.upload.TripUploadDto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

// UploadRepository의 EC2 API 기반 구현체. 네트워크 호출이라 반드시 메인 스레드 밖에서 호출할 것
public class UploadRepositoryImpl implements UploadRepository {

    private final UploadApi uploadApi;

    public UploadRepositoryImpl(UploadApi uploadApi) {
        this.uploadApi = uploadApi;
    }

    // photos[] 멀티파트 파트명은 PHP $_FILES['photos']가 배열로 받도록 서버(FileUploader)와 맞춰진 이름 — 바꾸면 안 됨
    @Override
    public AppResult<List<Long>> upload(List<File> photos, String uploadType) {
        List<MultipartBody.Part> parts = new ArrayList<>();
        for (File photo : photos) {
            RequestBody fileBody = RequestBody.create(photo, MediaType.parse(mimeTypeOf(photo)));
            parts.add(MultipartBody.Part.createFormData("photos[]", photo.getName(), fileBody));
        }
        RequestBody typeBody = RequestBody.create(uploadType, MediaType.parse("text/plain"));

        try {
            Response<ApiResponseDto<TripUploadDto>> response =
                    uploadApi.upload(parts, typeBody).execute();

            ApiResponseDto<TripUploadDto> body = response.body();
            if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                return AppResult.failure(toError(response, body));
            }

            return AppResult.success(body.getData().getUploadIds());
        } catch (IOException e) {
            return AppResult.failure(networkError());
        }
    }

    // 확장자 기준 MIME 추정 — 서버(FileUploader)가 jpeg/png만 허용하므로 그 외는 400으로 걸러짐
    private String mimeTypeOf(File file) {
        return file.getName().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
    }

    private AppError toError(Response<?> response, ApiResponseDto<?> body) {
        String message = body != null ? body.getMessage() : "요청에 실패했습니다.";
        return new AppError(response.code(), message);
    }

    private AppError networkError() {
        return new AppError(0, "네트워크 오류가 발생했습니다.");
    }
}
