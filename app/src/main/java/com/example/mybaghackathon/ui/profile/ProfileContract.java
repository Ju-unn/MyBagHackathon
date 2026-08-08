package com.example.mybaghackathon.ui.profile;

import com.example.mybaghackathon.model.UserDefaultItem;

import java.util.List;

// S15 프로필(미리보기) 화면의 View/Presenter 계약
public interface ProfileContract {

    interface View {
        void showItemPreview(List<UserDefaultItem> previewItems, int totalCount);
        void showError(String message);
    }

    interface Presenter {
        // 기본 물품 목록을 불러와 앞 4개 미리보기와 전체 개수를 View에 전달한다
        void loadItems();

        void renameItem(long defaultItemId, String newLabel);

        void deleteItem(long defaultItemId);

        // Fragment의 뷰가 파괴될 때 호출 — 이후 View 콜백을 막는다
        void onDestroy();
    }
}
