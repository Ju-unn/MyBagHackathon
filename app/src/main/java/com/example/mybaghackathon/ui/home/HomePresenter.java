package com.example.mybaghackathon.ui.home;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.PackingRepository;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.TripMember;
import com.example.mybaghackathon.ui.checklist.ChecklistProgressCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// HomeContract.Presenter 구현체 — 진행중인 여행방 목록을 불러와 가장 임박한 순으로
// 정렬하고, 각 방의 참여자·체크리스트 완료율도 함께 불러온다.
public class HomePresenter implements HomeContract.Presenter {

    private static final String TAG = "HomePresenter";

    private final HomeContract.View view;
    private final TripRepository tripRepository;
    private final PackingRepository packingRepository;
    private final long currentUserId;
    private final Context appContext;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean destroyed;

    public HomePresenter(HomeContract.View view, TripRepository tripRepository,
                          PackingRepository packingRepository, long currentUserId, Context context) {
        this.view = view;
        this.tripRepository = tripRepository;
        this.packingRepository = packingRepository;
        this.currentUserId = currentUserId;
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void loadTrips() {
        executor.execute(() -> {
            AppResult<List<Trip>> result = tripRepository.listMyTrips();
            if (!result.isSuccess() || result.getData() == null) {
                postError(result);
                return;
            }

            List<Trip> trips = new ArrayList<>(result.getData());
            Collections.sort(trips, (a, b) -> compareByStartDate(a.getStartDate(), b.getStartDate()));

            // 목록 맨 앞(가장 임박한/진행중인 방)만 검정 강조 카드로 보여주지만,
            // 아바타·체크리스트 진행률은 모든 방 카드에 자세히 표시하므로 전부 불러온다.
            Map<Long, Integer> progressByTripId = new HashMap<>();
            for (Trip trip : trips) {
                // listMyTrips()의 요약 정보 대신 getTripDetail()로 멤버·예상 인원수를 다시 불러온다 —
                // 아바타 표시와, 진행률 계산 시 1인/다인 방을 가르는 데(체크리스트 화면과 동일 기준) 쓴다.
                List<TripMember> members = Collections.emptyList();
                AppResult<Trip> detailResult = tripRepository.getTripDetail(trip.getTripId());
                if (detailResult.isSuccess() && detailResult.getData() != null) {
                    Trip detail = detailResult.getData();
                    members = detail.getMembers() == null ? Collections.emptyList() : detail.getMembers();
                    trip.setMembers(members);
                    trip.setExpectedMemberCount(detail.getExpectedMemberCount());
                }
                AppResult<List<PackingItem>> packingResult = packingRepository.listItems(trip.getTripId(), null);
                if (packingResult.isSuccess() && packingResult.getData() != null) {
                    boolean solo = isSoloTrip(trip, members);
                    progressByTripId.put(trip.getTripId(),
                            completionPercent(packingResult.getData(), solo));
                }
            }

            postToView(() -> view.showTrips(trips, progressByTripId));
        });
    }

    @Override
    public void deleteTrip(long tripId) {
        executor.execute(() -> {
            AppResult<Void> result = tripRepository.deleteTrip(tripId);
            if (result.isSuccess()) {
                postToView(() -> view.onTripDeleted(tripId));
            } else {
                postError(result);
            }
        });
    }

    @Override
    public void leaveTrip(long tripId) {
        executor.execute(() -> {
            AppResult<Void> result = tripRepository.leaveTrip(tripId);
            if (result.isSuccess()) {
                postToView(() -> view.onTripLeft(tripId));
            } else {
                postError(result);
            }
        });
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        executor.shutdownNow();
    }

    // 체크리스트 화면(ChecklistActivity#updateHeader)과 동일한 계산 — 1인방은 "내 목록"
    // (선택 AI + 기본/개인 물품) 기준, 다인방은 공용 물품 기준. 서버가 선택한 AI 추천만
    // 반환하므로 홈 카드와 방 안 체크리스트의 진행률이 항상 일치한다.
    private int completionPercent(List<PackingItem> items, boolean solo) {
        List<PackingItem> active = activeItems(items);
        return solo
                ? ChecklistProgressCalculator.calculateForMine(active, currentUserId).percent()
                : ChecklistProgressCalculator.calculate(active).percent();
    }

    // ChecklistActivity가 memberCount<=1일 때 soloMode로 전환하는 것과 동일한 기준.
    private boolean isSoloTrip(Trip trip, List<TripMember> members) {
        Integer expected = trip.getExpectedMemberCount();
        int effectiveCount = expected == null || expected <= 0
                ? Math.max(1, members.size())
                : expected;
        return effectiveCount <= 1;
    }

    private List<PackingItem> activeItems(List<PackingItem> items) {
        List<PackingItem> result = new ArrayList<>();
        for (PackingItem item : items) {
            if (item != null
                    && !"DELETED".equalsIgnoreCase(item.getItemStatus())
                    && !"EXCLUDED".equalsIgnoreCase(item.getItemStatus())) {
                result.add(item);
            }
        }
        return result;
    }

    private int compareByStartDate(String a, String b) {
        boolean aEmpty = a == null || a.isEmpty();
        boolean bEmpty = b == null || b.isEmpty();
        if (aEmpty && bEmpty) {
            return 0;
        }
        if (aEmpty) {
            return 1;
        }
        if (bEmpty) {
            return -1;
        }
        return a.compareTo(b);
    }

    private void postError(AppResult<?> result) {
        int httpStatus = result.getError() == null ? 0 : result.getError().getHttpStatus();
        String message = result.getError() == null || result.getError().getMessage() == null
                ? "여행 목록을 불러오지 못했습니다."
                : result.getError().getMessage();
        Log.e(TAG, "API 실패 (status=" + httpStatus + "): " + message);
        postToView(() -> view.showError(message));
    }

    private void postToView(Runnable action) {
        mainHandler.post(() -> {
            if (destroyed) {
                return;
            }
            action.run();
        });
    }
}
