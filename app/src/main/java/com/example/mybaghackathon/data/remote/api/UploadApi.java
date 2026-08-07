package com.example.mybaghackathon.data.remote.api;

import com.example.mybaghackathon.data.remote.dto.common.ApiResponseDto;
import com.example.mybaghackathon.data.remote.dto.upload.TripUploadDto;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

// 여러 장의 일정·숙소 이미지 업로드 API 엔드포인트 정의
public interface UploadApi {

    // multipart/form-data: photos[]=파일들, upload_type=ITINERARY|ACCOMMODATION|EXTRA
    @Multipart
    @POST("api/itinerary/upload.php")
    Call<ApiResponseDto<TripUploadDto>> upload(
            @Part List<MultipartBody.Part> photos,
            @Part("upload_type") RequestBody uploadType
    );
}
