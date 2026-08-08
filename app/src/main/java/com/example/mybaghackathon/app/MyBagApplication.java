package com.example.mybaghackathon.app;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.mybaghackathon.BuildConfig;
import com.kakao.sdk.common.KakaoSdk;

// 앱 전역 초기화 진입점으로 사용하는 Application 클래스
public class MyBagApplication extends Application {

    private AppContainer appContainer;

    // 카카오 SDK 초기화 후 AppContainer(공용 객체 모음)를 생성한다
    @Override
    public void onCreate() {
        super.onCreate();
        // 디자인이 라이트 모드 전용이라(README §1), 시스템 다크모드를 타지 않도록 고정.
        // BottomSheetDialog 등 M3 컴포넌트가 colorSurfaceContainer* 다크 팔레트를
        // 새어 쓰는 걸 근본적으로 막음 (values-night/ 오버라이드 누락에 의존하지 않음)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY);
        appContainer = new AppContainer(this);
    }

    // Activity/Fragment 등에서 Repository 같은 공용 객체를 꺼내 쓰기 위한 접근자
    public AppContainer getAppContainer() {
        return appContainer;
    }
}
