package com.example.mybaghackathon.ui.archive;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.PackingRepository;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.TripMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// ArchiveContract.Presenter 구현체 — "진행 중" 세그먼트는 진행중인 내 여행방 목록을,
// "지난 여행" 세그먼트는 완료된 여행방 목록을 불러온다. 진행중 목록 중 오늘 여행
// 기간에 들어간 방이 있으면 참여자·체크리스트 완료율을 추가로 불러온다.
public class ArchivePresenter implements ArchiveContract.Presenter {

    private final ArchiveContract.View view;
    private final TripRepository tripRepository;
    private final PackingRepository packingRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean destroyed;

    public ArchivePresenter(ArchiveContract.View view, TripRepository tripRepository,
                             PackingRepository packingRepository) {
        this.view = view;
        this.tripRepository = tripRepository;
        this.packingRepository = packingRepository;
    }

    @Override
    public void loadOngoingTrips() {
        executor.execute(() -> {
            AppResult<List<Trip>> result = tripRepository.listMyTrips();
            if (!result.isSuccess() || result.getData() == null) {
                postError(result);
                return;
            }

            List<Trip> trips = new ArrayList<>(result.getData());
            Collections.sort(trips, (a, b) -> compareByStartDate(a.getStartDate(), b.getStartDate()));

            // 목록 맨 앞(가장 임박한/진행중인 방) 하나만 검정 상세 카드로 보여주므로
            // 멤버·체크리스트 진행률도 그 방만 추가로 불러온다.
            Map<Long, Integer> progressByTripId = new HashMap<>();
            if (!trips.isEmpty()) {
                Trip highlighted = trips.get(0);
                AppResult<List<TripMember>> membersResult = tripRepository.listMembers(highlighted.getTripId());
                if (membersResult.isSuccess() && membersResult.getData() != null) {
                    highlighted.setMembers(membersResult.getData());
                }
                AppResult<List<PackingItem>> packingResult = packingRepository.listItems(highlighted.getTripId(), null);
                if (packingResult.isSuccess() && packingResult.getData() != null) {
                    progressByTripId.put(highlighted.getTripId(), completionPercent(packingResult.getData()));
                }
            }

            postToView(() -> view.showOngoingTrips(trips, progressByTripId));
        });
    }

    @Override
    public void loadPastTrips() {
        executor.execute(() -> {
            AppResult<List<Trip>> result = tripRepository.listArchivedTrips();
            if (!result.isSuccess() || result.getData() == null) {
                postError(result);
                return;
            }
            postToView(() -> view.showPastTrips(result.getData()));
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
