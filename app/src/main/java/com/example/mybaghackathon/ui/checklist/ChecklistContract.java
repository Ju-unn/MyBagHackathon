package com.example.mybaghackathon.ui.checklist;

import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.TripMember;

import java.util.List;

/** S11~S13 체크리스트 화면에서 View와 Presenter가 주고받는 동작을 정의한다. */
public interface ChecklistContract {

    interface View {
        void showChecklist(
                String tripName,
                List<PackingItem> items,
                List<TripMember> members,
                int memberCount,
                boolean isHost
        );

        void showError(String message);

        void showRetryableError(String message);

        void showLoading(boolean loading);

        void showDeleteUndo(PackingItem item);
    }

    interface Presenter {
        void loadChecklist(long tripId, boolean initialHost);

        void refreshChecklist();

        void addItem(String name, int priorityLevel, String scope);

        void updateItem(PackingItem item, String name, int priorityLevel);

        void toggleItem(PackingItem item);

        void assignItem(PackingItem item, Long userId);

        void assignItems(PackingItem item, List<Long> userIds);

        /**
         * 다중 배정으로 row가 나뉜 공용 물품 그룹(같은 item_group_id)의 담당자를 새 선택
         * 목록으로 재조정한다. 서버 assign.php가 그룹 전체를 하나의 트랜잭션으로 동기화한다.
         */
        void reassignGroup(List<PackingItem> groupItems, List<Long> userIds);

        void requestDelete(PackingItem item);

        void undoDelete(PackingItem item);

        void confirmDelete(PackingItem item);

        /** 실행취소 없이 즉시 삭제 — 방장이 공용 물품을 지울 때 사용(BS.. 공용 탭 더보기 메뉴). */
        void deleteItem(PackingItem item);

        void onDestroy();
    }
}
