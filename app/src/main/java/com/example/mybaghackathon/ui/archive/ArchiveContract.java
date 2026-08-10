package com.example.mybaghackathon.ui.archive;

import com.example.mybaghackathon.model.Trip;

import java.util.List;
import java.util.Map;

// S14 아카이브(공용 여행) 화면의 View/Presenter 계약
public interface ArchiveContract {

    interface View {
        // trips는 진행중인(완료되지 않은) 내 여행방 목록. progressByTripId는 그중
        // 오늘 여행 기간에 들어간 방의 체크리스트 완료율(%)만 담는다.
        void showOngoingTrips(List<Trip> trips, Map<Long, Integer> progressByTripId);

        // 지난(완료된) 여행방 목록
        void showPastTrips(List<Trip> trips);

        void showError(String message);

        void onTripDeleted(long tripId);

        void onTripLeft(long tripId);
    }

    interface Presenter {
        void loadOngoingTrips();

        void loadPastTrips();

        void deleteTrip(long tripId);

        void leaveTrip(long tripId);

        // Fragment의 뷰가 파괴될 때 호출 — 이후 View 콜백을 막는다
        void onDestroy();
    }
}
