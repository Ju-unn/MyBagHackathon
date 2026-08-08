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
    }

    interface Presenter {
        // 로컬에 저장된 사용자 정보와 기본 물품 목록(앞 4개 미리보기 + 전체 개수)을 View에 전달한다
        void loadItems();

        void renameItem(long defaultItemId, String newLabel);

        void deleteItem(long defaultItemId);

        // Fragment의 뷰가 파괴될 때 호출 — 이후 View 콜백을 막는다
        void onDestroy();
    }
}
