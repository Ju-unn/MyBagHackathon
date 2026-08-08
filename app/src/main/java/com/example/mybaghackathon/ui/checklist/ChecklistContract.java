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

        void showDeleteUndo(PackingItem item);
    }

    interface Presenter {
        void loadChecklist(long tripId, boolean initialHost);

        void refreshChecklist();

        void addItem(String name, int priorityLevel, String scope);

        void updateItem(PackingItem item, String name, int priorityLevel);

        void toggleItem(PackingItem item);

        void assignItem(PackingItem item, Long userId);

        void requestDelete(PackingItem item);

        void undoDelete(PackingItem item);

        void confirmDelete(PackingItem item);

        void onDestroy();
    }
}
