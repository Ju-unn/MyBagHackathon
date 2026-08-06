package com.example.mybaghackathon.ui.home;

import com.example.mybaghackathon.ui.molecules.AvatarStackHelper;

import java.util.List;

/**
 * S03 홈 화면의 여행방 카드 하나를 그리는 데 필요한 값 묶음.
 * 상태(진행중/준비전/완료)별로 필요한 필드만 모아 놓은 화면 전용 모델.
 */
public class TripRoomUiModel {

    public enum State { ACTIVE, UPCOMING, PAST }

    public final long tripId;
    public final State state;
    public final String title;
    public final String ddayText;
    public final List<AvatarStackHelper.Entry> avatars;
    public final int progressPercent;
    public final String hint;

    private TripRoomUiModel(long tripId, State state, String title, String ddayText,
                             List<AvatarStackHelper.Entry> avatars, int progressPercent, String hint) {
        this.tripId = tripId;
        this.state = state;
        this.title = title;
        this.ddayText = ddayText;
        this.avatars = avatars;
        this.progressPercent = progressPercent;
        this.hint = hint;
    }

    public static TripRoomUiModel active(long tripId, String title, String ddayText,
                                          List<AvatarStackHelper.Entry> avatars, int progressPercent) {
        return new TripRoomUiModel(tripId, State.ACTIVE, title, ddayText, avatars, progressPercent, null);
    }

    public static TripRoomUiModel upcoming(long tripId, String title, String ddayText, String hint) {
        return new TripRoomUiModel(tripId, State.UPCOMING, title, ddayText, null, 0, hint);
    }

    public static TripRoomUiModel past(long tripId, String title) {
        return new TripRoomUiModel(tripId, State.PAST, title, null, null, 0, null);
    }
}
