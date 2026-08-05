package com.example.mybaghackathon.app;

import android.content.Context;

import com.example.mybaghackathon.data.local.TokenStorage;
import com.example.mybaghackathon.data.remote.api.ApiClient;
import com.example.mybaghackathon.data.remote.api.AuthApi;
import com.example.mybaghackathon.data.repository.AuthRepository;
import com.example.mybaghackathon.data.repository.AuthRepositoryImpl;

// Repository, ApiClient 등 공용 객체를 생성하고 보관하는 DI 컨테이너
public class AppContainer {

    public final TokenStorage tokenStorage;
    public final AuthRepository authRepository;

    public AppContainer(Context context) {
        tokenStorage = new TokenStorage(context.getApplicationContext());

        ApiClient apiClient = new ApiClient(tokenStorage);
        AuthApi authApi = apiClient.create(AuthApi.class);

        authRepository = new AuthRepositoryImpl(authApi, tokenStorage);
    }
}
