package com.example.mybaghackathon.data.local;

import android.content.Context;

import com.example.mybaghackathon.model.User;
import com.example.mybaghackathon.util.PrefsManager;

// 로그인한 사용자 정보(닉네임/이메일/프로필 이미지)의 로컬 저장을 담당한다.
public class UserStorage {

    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NICKNAME = "user_nickname";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_PROFILE_IMAGE_URL = "user_profile_image_url";

    private final PrefsManager prefs;

    public UserStorage(Context context) {
        this.prefs = new PrefsManager(context);
    }

    public void saveUser(User user) {
        prefs.putString(KEY_USER_ID, String.valueOf(user.getUserId()));
        prefs.putString(KEY_NICKNAME, user.getNickname());
        prefs.putString(KEY_EMAIL, user.getEmail());
        prefs.putString(KEY_PROFILE_IMAGE_URL, user.getProfileImageUrl());
    }

    public User getUser() {
        String userId = prefs.getString(KEY_USER_ID, null);
        if (userId == null) {
            return null;
        }
        return new User(
                Long.parseLong(userId),
                prefs.getString(KEY_NICKNAME, null),
                prefs.getString(KEY_EMAIL, null),
                prefs.getString(KEY_PROFILE_IMAGE_URL, null)
        );
    }

    public void clearUser() {
        prefs.remove(KEY_USER_ID);
        prefs.remove(KEY_NICKNAME);
        prefs.remove(KEY_EMAIL);
        prefs.remove(KEY_PROFILE_IMAGE_URL);
    }
}
