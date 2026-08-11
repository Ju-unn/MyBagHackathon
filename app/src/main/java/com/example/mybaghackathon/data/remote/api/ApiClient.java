package com.example.mybaghackathon.data.remote.api;

import com.example.mybaghackathon.common.Constants;
import com.example.mybaghackathon.data.local.TokenStorage;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Retrofit 등 서버 통신 클라이언트를 생성/설정
public class ApiClient {

    private final Retrofit retrofit;

    // 인증 헤더 인터셉터 + 로깅 인터셉터를 붙인 Retrofit 인스턴스를 만든다
    public ApiClient(TokenStorage tokenStorage) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // TODO(보안/배포전): BuildConfig.DEBUG일 때만 BODY, 릴리즈는 NONE으로 바꿀 것.
        // 지금은 릴리즈 빌드에서도 Authorization 헤더(JWT)와 응답 바디(개인정보)가
        // 그대로 Logcat에 찍힘. logging.setLevel(BuildConfig.DEBUG ? Level.BODY : Level.NONE);
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 저장된 JWT가 있으면 모든 요청에 Authorization 헤더를 자동으로 붙인다
        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            String token = tokenStorage.getToken();
            if (token == null) {
                return chain.proceed(original);
            }
            Request authorized = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(authorized);
        };

        // analyze.php/confirm.php는 서버에서 GPT를 호출할 수 있어(서버 타임아웃 60초, OPENAI_VISION_TIMEOUT)
        // readTimeout을 그보다 여유 있게 잡음. writeTimeout도 사진 여러 장 업로드 대비 넉넉히 잡음.
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(70, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // 주어진 API 인터페이스의 Retrofit 구현체를 만들어 반환한다
    public <T> T create(Class<T> apiClass) {
        return retrofit.create(apiClass);
    }
}
