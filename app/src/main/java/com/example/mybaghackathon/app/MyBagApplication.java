package com.example.mybaghackathon.app;

import android.app.Application;

import com.example.mybaghackathon.BuildConfig;
import com.kakao.sdk.common.KakaoSdk;

// 앱 전역 초기화 진입점으로 사용하는 Application 클래스
public class MyBagApplication extends Application {

    private AppContainer appContainer;

    // 카카오 SDK 초기화 후 AppContainer(공용 객체 모음)를 생성한다
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY);
        appContainer = new AppContainer(this);
    }

    // Activity/Fragment 등에서 Repository 같은 공용 객체를 꺼내 쓰기 위한 접근자
    public AppContainer getAppContainer() {
        return appContainer;
    }
}
