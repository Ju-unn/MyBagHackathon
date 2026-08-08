package com.example.mybaghackathon.ui.home;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.PackingRepository;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.TripMember;
import com.example.mybaghackathon.util.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// HomeContract.Presenter 구현체 — 진행중인 여행방 목록을 불러와 가장 임박한 순으로
// 정렬하고, 그중 오늘 여행 중인 방이 있으면 참여자·체크리스트 완료율을 추가로 불러온다.
public class HomePresenter implements HomeContract.Presenter {

    private final HomeContract.View view;
    private final TripRepository tripRepository;
    private final PackingRepository packingRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean destroyed;

    public HomePresenter(HomeContract.View view, TripRepository tripRepository,
                          PackingRepository packingRepository) {
        this.view = view;
        this.tripRepository = tripRepository;
        this.packingRepository = packingRepository;
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

            Map<Long, Integer> progressByTripId = new HashMap<>();
            for (Trip trip : trips) {
                if (!DateUtils.isTravelingNow(trip.getStartDate(), trip.getEndDate())) {
                    continue;
                }
                AppResult<List<TripMember>> membersResult = tripRepository.listMembers(trip.getTripId());
                if (membersResult.isSuccess() && membersResult.getData() != null) {
                    trip.setMembers(membersResult.getData());
                }
                AppResult<List<PackingItem>> packingResult = packingRepository.listItems(trip.getTripId(), null);
                if (packingResult.isSuccess() && packingResult.getData() != null) {
                    progressByTripId.put(trip.getTripId(), completionPercent(packingResult.getData()));
                }
            }

            postToView(() -> view.showTrips(trips, progressByTripId));
        });
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        executor.shutdownNow();
    }

    private int completionPercent(List<PackingItem> items) {
        if (items.isEmpty()) {
            return 0;
        }
        int completed = 0;
        for (PackingItem item : items) {
            if (item.isCompleted()) {
                completed++;
            }
        }
        return Math.round(100f * completed / items.size());
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
        String message = result.getError() == null || result.getError().getMessage() == null
                ? "여행 목록을 불러오지 못했습니다."
                : result.getError().getMessage();
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
