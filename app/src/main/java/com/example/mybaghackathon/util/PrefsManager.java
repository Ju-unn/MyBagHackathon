package com.example.mybaghackathon.util;

import android.content.Context;
import android.content.SharedPreferences;

// SharedPreferences 공용 래퍼 — 로컬에 저장하는 여러 값(토큰, 설정 등)이 이 클래스 하나를 공유
public class PrefsManager {

    private static final String PREFS_NAME = "mybag_prefs";

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void putString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public void remove(String key) {
        prefs.edit().remove(key).apply();
    }
}
