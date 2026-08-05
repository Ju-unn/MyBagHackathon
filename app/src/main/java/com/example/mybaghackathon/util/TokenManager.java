package com.example.mybaghackathon.util;

import android.content.Context;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

// 로그인 토큰(JWT) 저장/조회를 담당하는 유틸
public class TokenManager {

    private static final String KEY_JWT = "jwt";

    private final PrefsManager prefs;

    public TokenManager(Context context) {
        this.prefs = new PrefsManager(context);
    }

    public void saveToken(String jwt) {
        prefs.putString(KEY_JWT, jwt);
    }

    public String getToken() {
        return prefs.getString(KEY_JWT, null);
    }

    public void clearToken() {
        prefs.remove(KEY_JWT);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !isExpired(token);
    }

    // 서버(kakao_login.php)가 발급한 JWT payload의 sub(user id)를 꺼냄.
    // 서명 검증은 서버 책임 — 여기서는 로컬 파싱만 한다.
    public int getUserId() {
        JSONObject payload = decodePayload(getToken());
        return payload != null ? payload.optInt("sub", -1) : -1;
    }

    private boolean isExpired(String jwt) {
        JSONObject payload = decodePayload(jwt);
        if (payload == null) return true;
        long exp = payload.optLong("exp", 0);
        return exp > 0 && System.currentTimeMillis() / 1000 >= exp;
    }

    private JSONObject decodePayload(String jwt) {
        if (jwt == null) return null;
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) return null;
        try {
            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            return new JSONObject(new String(decoded, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | JSONException e) {
            return null;
        }
    }
}
