package com.example.mybaghackathon.ui.profile;

import com.example.mybaghackathon.model.UserDefaultItem;

import java.util.List;

// S15 내 기본 물품 전체보기 화면의 View/Presenter 계약
public interface ProfileItemsContract {

    interface View {
        void showItems(List<UserDefaultItem> items);
        void showError(String message);
    }

    interface Presenter {
        void loadItems();

        // priorityLevel: 0=필수, 1=중간, 2=선택 (AddItemSheet 기준)
        void addItem(String itemName, int priorityLevel);

        void renameItem(long defaultItemId, String newLabel, int priorityLevel);

        void deleteItem(long defaultItemId);

        // Activity가 파괴될 때 호출 — 이후 View 콜백을 막는다
        void onDestroy();
    }
}
