package com.example.mybaghackathon.ui.createroom;

/** 방 이름 입력값 검증과 인원 수 조절을 담당한다. */
public class CreateRoomPresenter implements CreateRoomContract.Presenter {

    static final int MEMBER_COUNT_MIN = 1;
    static final int MEMBER_COUNT_MAX = 10;
    static final int MEMBER_COUNT_DEFAULT = 1;
    static final int ROOM_NAME_MAX_LENGTH = 10;

    private final CreateRoomContract.View view;

    private int memberCount = MEMBER_COUNT_DEFAULT;
    private String roomName = "";

    public CreateRoomPresenter(CreateRoomContract.View view) {
        this.view = view;
        view.showMemberCount(memberCount);
    }

    @Override
    public void onNameChanged(String name) {
        roomName = name == null ? "" : name;
        view.showNameLengthNotice(roomName.length() >= ROOM_NAME_MAX_LENGTH);
    }

    @Override
    public void onMemberMinusClicked() {
        if (memberCount <= MEMBER_COUNT_MIN) {
            return;
        }
        memberCount--;
        view.showMemberCount(memberCount);
    }

    @Override
    public void onMemberPlusClicked() {
        if (memberCount >= MEMBER_COUNT_MAX) {
            return;
        }
        memberCount++;
        view.showMemberCount(memberCount);
    }

    @Override
    public void onSubmitClicked() {
        String trimmed = roomName.trim();
        if (trimmed.isEmpty()) {
            view.showNameRequiredError();
            return;
        }
        view.navigateToScheduleUpload(trimmed, memberCount);
    }
}
