package com.example.mybaghackathon.app;

import android.app.Application;

import com.example.mybaghackathon.BuildConfig;
import com.kakao.sdk.common.KakaoSdk;

// 앱 실행 시 초기화(공용 객체 생성 등)를 담당하는 Application 클래스
public class PackMateApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY);
    }
}
