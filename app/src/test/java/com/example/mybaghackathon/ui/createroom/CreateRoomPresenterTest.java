package com.example.mybaghackathon.ui.createroom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CreateRoomPresenterTest {

    @Test
    public void initialState_showsDefaultMemberCount() {
        RecordingView view = new RecordingView();
        new CreateRoomPresenter(view);

        assertEquals(1, view.memberCount);
    }

    @Test
    public void memberPlusAndMinus_areClampedToBounds() {
        RecordingView view = new RecordingView();
        CreateRoomPresenter presenter = new CreateRoomPresenter(view);

        presenter.onMemberMinusClicked();
        assertEquals(1, view.memberCount);

        for (int i = 0; i < 15; i++) {
            presenter.onMemberPlusClicked();
        }
        assertEquals(10, view.memberCount);

        for (int i = 0; i < 15; i++) {
            presenter.onMemberMinusClicked();
        }
        assertEquals(1, view.memberCount);
    }

    @Test
    public void nameReachingMaxLength_showsLengthNotice() {
        RecordingView view = new RecordingView();
        CreateRoomPresenter presenter = new CreateRoomPresenter(view);

        presenter.onNameChanged("짧은이름");
        assertFalse(view.nameLengthNoticeVisible);

        presenter.onNameChanged("0123456789");
        assertTrue(view.nameLengthNoticeVisible);
    }

    @Test
    public void submitWithBlankName_showsRequiredErrorWithoutNavigating() {
        RecordingView view = new RecordingView();
        CreateRoomPresenter presenter = new CreateRoomPresenter(view);

        presenter.onNameChanged("   ");
        presenter.onSubmitClicked();

        assertTrue(view.nameRequiredErrorShown);
        assertNull(view.navigatedRoomName);
    }

    @Test
    public void submitWithValidName_navigatesWithTrimmedNameAndMemberCount() {
        RecordingView view = new RecordingView();
        CreateRoomPresenter presenter = new CreateRoomPresenter(view);

        presenter.onNameChanged("  제주 여행  ");
        presenter.onMemberPlusClicked();
        presenter.onSubmitClicked();

        assertFalse(view.nameRequiredErrorShown);
        assertEquals("제주 여행", view.navigatedRoomName);
        assertEquals(2, view.navigatedMemberCount);
    }

    private static final class RecordingView implements CreateRoomContract.View {
        private int memberCount;
        private boolean nameLengthNoticeVisible;
        private boolean nameRequiredErrorShown;
        private String navigatedRoomName;
        private int navigatedMemberCount;

        @Override
        public void showMemberCount(int count) {
            memberCount = count;
        }

        @Override
        public void showNameLengthNotice(boolean visible) {
            nameLengthNoticeVisible = visible;
        }

        @Override
        public void showNameRequiredError() {
            nameRequiredErrorShown = true;
        }

        @Override
        public void navigateToScheduleUpload(String roomName, int memberCount) {
            navigatedRoomName = roomName;
            navigatedMemberCount = memberCount;
        }
    }
}
