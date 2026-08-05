package com.example.mybaghackathon.data.mapper;

import com.example.mybaghackathon.data.remote.dto.auth.AuthTokenDto;
import com.example.mybaghackathon.model.User;

// 서버 Dto를 User 모델로 변환
public final class UserMapper {

    private UserMapper() {
    }

    public static User from(AuthTokenDto.UserPayload dto) {
        if (dto == null) {
            return null;
        }
        // 카카오 로그인은 이메일 동의항목을 안 받으므로 email은 항상 null
        return new User(dto.getId(), dto.getNickname(), null, dto.getProfileImageUrl());
    }
}
