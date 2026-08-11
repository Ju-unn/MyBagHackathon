package com.example.mybaghackathon.ui.createroom;

/** S04 · 방 만들기 화면의 View/Presenter 계약. */
public interface CreateRoomContract {

    interface View {
        void showMemberCount(int count);

        void showNameLengthNotice(boolean visible);

        void showNameRequiredError();

        void navigateToScheduleUpload(String roomName, int memberCount);
    }

    interface Presenter {
        void onNameChanged(String name);

        void onMemberMinusClicked();

        void onMemberPlusClicked();

        void onSubmitClicked();
    }
}
