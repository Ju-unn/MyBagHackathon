package com.example.mybaghackathon.ui.home;

import com.example.mybaghackathon.model.Trip;

import java.util.List;
import java.util.Map;

// S03 홈 화면의 View/Presenter 계약
public interface HomeContract {

    interface View {
        // trips는 서버가 반환한 진행중인 내 여행방 목록(가장 임박한 일정 순 정렬).
        // progressByTripId는 그중 오늘 여행 기간에 들어간(진행중) 방의 체크리스트 완료율(%)만 담는다.
        void showTrips(List<Trip> trips, Map<Long, Integer> progressByTripId);

        void showError(String message);

        void onTripDeleted(long tripId);
    }

    interface Presenter {
        void loadTrips();

        void deleteTrip(long tripId);

        // Fragment의 뷰가 파괴될 때 호출 — 이후 View 콜백을 막는다
        void onDestroy();
    }
}
