package com.example.mybaghackathon.app;

import android.content.Context;

import com.example.mybaghackathon.data.local.TokenStorage;
import com.example.mybaghackathon.data.remote.api.AnalysisApi;
import com.example.mybaghackathon.data.remote.api.ApiClient;
import com.example.mybaghackathon.data.remote.api.AuthApi;
import com.example.mybaghackathon.data.remote.api.PackingApi;
import com.example.mybaghackathon.data.remote.api.UploadApi;
import com.example.mybaghackathon.data.remote.api.WeatherApi;
import com.example.mybaghackathon.data.repository.AnalysisRepository;
import com.example.mybaghackathon.data.repository.AnalysisRepositoryImpl;
import com.example.mybaghackathon.data.repository.AuthRepository;
import com.example.mybaghackathon.data.repository.AuthRepositoryImpl;
import com.example.mybaghackathon.data.repository.PackingRepository;
import com.example.mybaghackathon.data.repository.PackingRepositoryImpl;
import com.example.mybaghackathon.data.repository.UploadRepository;
import com.example.mybaghackathon.data.repository.UploadRepositoryImpl;
import com.example.mybaghackathon.data.repository.WeatherRepository;
import com.example.mybaghackathon.data.repository.WeatherRepositoryImpl;

// Repository, ApiClient 등 공용 객체를 생성하고 보관하는 DI 컨테이너
public class AppContainer {

    public final TokenStorage tokenStorage;
    public final AuthRepository authRepository;
    public final WeatherRepository weatherRepository;
    public final UploadRepository uploadRepository;
    public final AnalysisRepository analysisRepository;
    public final PackingRepository packingRepository;

    // TokenStorage → ApiClient → 각 Api → Repository 순으로 엮어서 보관한다
    public AppContainer(Context context) {
        tokenStorage = new TokenStorage(context.getApplicationContext());

        ApiClient apiClient = new ApiClient(tokenStorage);
        AuthApi authApi = apiClient.create(AuthApi.class);
        WeatherApi weatherApi = apiClient.create(WeatherApi.class);
        UploadApi uploadApi = apiClient.create(UploadApi.class);
        AnalysisApi analysisApi = apiClient.create(AnalysisApi.class);
        PackingApi packingApi = apiClient.create(PackingApi.class);

        authRepository = new AuthRepositoryImpl(authApi, tokenStorage);
        weatherRepository = new WeatherRepositoryImpl(weatherApi);
        uploadRepository = new UploadRepositoryImpl(uploadApi);
        analysisRepository = new AnalysisRepositoryImpl(analysisApi);
        packingRepository = new PackingRepositoryImpl(packingApi);
    }
}
