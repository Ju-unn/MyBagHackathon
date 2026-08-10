package com.example.mybaghackathon.ui.invite;

/** 초대 링크로 여행방에 참여하는 화면의 MVP 계약. */
public interface InviteJoinContract {

    interface View {
        void showLoading();

        void showJoinError(String message);

        void openTrip(long tripId);
    }

    interface Presenter {
        void join(String inviteCode);

        void retry();

        void onDestroy();
    }
}
