package com.example.mybaghackathon.ui.review;

import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.RestrictedItem;

import java.util.ArrayList;
import java.util.List;

// S08 일정 검토 화면의 View/Presenter 계약
public interface ReviewContract {

    // 우선순위별 준비물 섹션 렌더링에 필요한 최소 정보만 담은 화면 전용 모델
    class ItemView {
        public final String itemName;
        public final Integer restrictionTagType; // null이면 태그 없음

        public ItemView(String itemName, Integer restrictionTagType) {
            this.itemName = itemName;
            this.restrictionTagType = restrictionTagType;
        }
    }

    interface View {
        void showDestination(String value);
        void showDateRange(String value);
        void showRestrictionWarning(String text);
        void hideRestrictionWarning();
        void addWeatherRow(String date, int weatherType, String status);
        void showWeatherLimitNotice();
        void showRequiredItems(List<ItemView> items);
        void showRecommendedItems(List<ItemView> items);
        void showOptionalItems(List<ItemView> items);
        void setGenerating(boolean generating);
        void showGenerateError(String message);
        void navigateToRoomDetail(
                long tripId,
                String roomName,
                String inviteCode
        );
    }

    interface Presenter {
        void init(String roomName, int memberCount, long analysisId,
                  ArrayList<RestrictedItem> restrictedItems, ArrayList<PackingItem> recommendedItems,
                  String destinationCountry, String destinationCity, String startDate, String endDate);
        List<RestrictedItem> getRestrictedItems();
        void onItemScopeToggled(String itemName, boolean checked);
        void onGenerateClicked();
        void onDestroy();
    }
}
