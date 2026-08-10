package com.example.mybaghackathon.ui.invite;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.TripRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 초대 코드 검증과 여행방 참여 요청을 담당한다. */
public class InviteJoinPresenter implements InviteJoinContract.Presenter {

    private final InviteJoinContract.View view;
    private final TripRepository tripRepository;
    private final ExecutorService executor;
    private final UiDispatcher uiDispatcher;

    private volatile boolean destroyed;
    private boolean joining;
    private String inviteCode;

    public InviteJoinPresenter(InviteJoinContract.View view, TripRepository tripRepository) {
        this(view, tripRepository, Executors.newSingleThreadExecutor(), new AndroidUiDispatcher());
    }

    InviteJoinPresenter(
            InviteJoinContract.View view,
            TripRepository tripRepository,
            ExecutorService executor,
            UiDispatcher uiDispatcher
    ) {
        this.view = view;
        this.tripRepository = tripRepository;
        this.executor = executor;
        this.uiDispatcher = uiDispatcher;
    }

    @Override
    public void join(String inviteCode) {
        this.inviteCode = inviteCode == null ? null : inviteCode.trim();
        retry();
    }

    @Override
    public void retry() {
        if (destroyed || joining) {
            return;
        }
        if (inviteCode == null || inviteCode.isEmpty()) {
            view.showJoinError("유효하지 않은 초대 링크입니다.");
            return;
        }

        joining = true;
        view.showLoading();
        executor.execute(this::joinOnWorker);
    }

    private void joinOnWorker() {
        try {
            AppResult<Long> result = tripRepository.joinByCode(inviteCode);
            if (!result.isSuccess() || result.getData() == null || result.getData() <= 0L) {
                postError(messageOf(result));
                return;
            }
            long tripId = result.getData();
            post(() -> {
                joining = false;
                view.openTrip(tripId);
            });
        } catch (RuntimeException error) {
            postError("초대 정보를 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    private String messageOf(AppResult<?> result) {
        if (result.getError() != null
                && result.getError().getMessage() != null
                && !result.getError().getMessage().trim().isEmpty()) {
            return result.getError().getMessage();
        }
        return "여행방에 참여하지 못했습니다. 초대 링크를 확인해 주세요.";
    }

    private void postError(String message) {
        post(() -> {
            joining = false;
            view.showJoinError(message);
        });
    }

    private void post(Runnable action) {
        uiDispatcher.post(() -> {
            if (!destroyed) {
                action.run();
            }
        });
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        uiDispatcher.clear();
        executor.shutdownNow();
    }

    interface UiDispatcher {
        void post(Runnable action);

        void clear();
    }

    private static final class AndroidUiDispatcher implements UiDispatcher {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void post(Runnable action) {
            handler.post(action);
        }

        @Override
        public void clear() {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
