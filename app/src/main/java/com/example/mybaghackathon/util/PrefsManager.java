package com.example.mybaghackathon.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.security.GeneralSecurityException;
import java.io.IOException;

// SharedPreferences 공용 래퍼 — 로컬에 저장하는 여러 값(토큰, 유저정보 등)이 이 클래스 하나를 공유.
// JWT처럼 민감한 값을 담기 때문에 Android Keystore 기반 EncryptedSharedPreferences를 씀 —
// 사용법(putString/getString 등)은 일반 SharedPreferences와 동일하고, 파일 저장 시에만
// 자동으로 암호화/복호화됨.
public class PrefsManager {

    private static final String PREFS_NAME = "mybag_prefs_encrypted";
    // 암호화 적용 전에 쓰던 평문 파일 이름 — 마이그레이션 후 삭제 대상
    private static final String LEGACY_PLAINTEXT_PREFS_NAME = "mybag_prefs";

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        Context appContext = context.getApplicationContext();
        // 예전 평문 SharedPreferences 파일이 기기에 남아있으면 삭제(마이그레이션).
        // deleteSharedPreferences는 파일이 없어도 안전하게 아무 일도 안 함.
        appContext.deleteSharedPreferences(LEGACY_PLAINTEXT_PREFS_NAME);
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            this.prefs = EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKeyAlias,
                    appContext,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("암호화된 로컬 저장소를 초기화하지 못했습니다.", e);
        }
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
