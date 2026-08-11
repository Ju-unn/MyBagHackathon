package com.example.mybaghackathon.data.local;

import android.content.Context;
import android.util.Base64;

import com.example.mybaghackathon.util.PrefsManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

// 로그인 JWT의 로컬 저장과 로그인 상태 확인을 담당한다.
// TODO(보안/배포전): PrefsManager가 일반 SharedPreferences라 JWT가 평문 저장됨 +
// AndroidManifest의 allowBackup="true"라 백업 경로로 새어나갈 여지 있음.
// EncryptedSharedPreferences(androidx.security.crypto)로 교체 고려.
public class TokenStorage {

    private static final String KEY_JWT = "jwt";

    private final PrefsManager prefs;

    public TokenStorage(Context context) {
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

    // JWT payload에서 사용자 ID를 읽는다. 토큰 서명 검증은 서버가 담당한다.
    public int getUserId() {
        JSONObject payload = decodePayload(getToken());
        return payload != null ? payload.optInt("sub", -1) : -1;
    }

    private boolean isExpired(String jwt) {
        JSONObject payload = decodePayload(jwt);
        if (payload == null) {
            return true;
        }

        long exp = payload.optLong("exp", 0);
        return exp > 0 && System.currentTimeMillis() / 1000 >= exp;
    }

    private JSONObject decodePayload(String jwt) {
        if (jwt == null) {
            return null;
        }

        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            return null;
        }

        try {
            byte[] decoded = Base64.decode(
                    parts[1],
                    Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING
            );
            return new JSONObject(new String(decoded, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | JSONException e) {
            return null;
        }
    }
}
