package com.example.mybaghackathon.ui.profile;

import com.example.mybaghackathon.model.User;
import com.example.mybaghackathon.model.UserDefaultItem;

import java.util.List;

// S15 프로필(미리보기) 화면의 View/Presenter 계약
public interface ProfileContract {

    interface View {
        // user는 로컬에 저장된 로그인 사용자 정보가 없으면(비로그인 등) null일 수 있다
        void showUser(User user);
        void showItemPreview(List<UserDefaultItem> previewItems, int totalCount);
        void showError(String message);
        void navigateToLogin();
    }

    interface Presenter {
        // 로컬에 저장된 사용자 정보와 기본 물품 목록(앞 4개 미리보기 + 전체 개수)을 View에 전달한다
        void loadItems();

        // 서버 로그아웃 API 호출 후 성공하면 로컬 토큰/유저 정보를 지우고 로그인 화면으로 보낸다
        void logout();

        // 회원 탈퇴 API 호출 후 성공하면 로컬 토큰/유저 정보를 지우고 로그인 화면으로 보낸다.
        // 실패 시(네트워크 오류 등) 계정은 그대로이므로 에러만 보여주고 화면은 유지한다
        void withdraw();

        // Fragment의 뷰가 파괴될 때 호출 — 이후 View 콜백을 막는다
        void onDestroy();
    }
}
