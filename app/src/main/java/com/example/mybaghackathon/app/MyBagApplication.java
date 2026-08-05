package com.example.mybaghackathon.app;

import android.app.Application;

import com.example.mybaghackathon.BuildConfig;
import com.kakao.sdk.common.KakaoSdk;

// 앱 전역 초기화 진입점으로 사용하는 Application 클래스
public class MyBagApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY);
    }
}
