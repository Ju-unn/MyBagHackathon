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
    /** 현재 로그인한 사용자가 이 방의 방장인지 여부 — 스와이프 액션이 삭제/나가기 중 무엇인지 결정한다. */
    public final boolean isOwner;
    /** 오늘이 이 방의 여행 기간 안인지 — 검정 강조 카드가 아니어도 상태 라벨을 "진행중"으로 보여줄지 결정한다. */
    public final boolean isOngoing;

    private TripRoomUiModel(long tripId, State state, String title, String ddayText,
                             List<AvatarStackHelper.Entry> avatars, int progressPercent, boolean isOwner,
                             boolean isOngoing) {
        this.tripId = tripId;
        this.state = state;
        this.title = title;
        this.ddayText = ddayText;
        this.avatars = avatars;
        this.progressPercent = progressPercent;
        this.isOwner = isOwner;
        this.isOngoing = isOngoing;
    }

    public static TripRoomUiModel active(long tripId, String title, String ddayText,
                                          List<AvatarStackHelper.Entry> avatars, int progressPercent,
                                          boolean isOwner, boolean isOngoing) {
        return new TripRoomUiModel(
                tripId, State.ACTIVE, title, ddayText, avatars, progressPercent, isOwner, isOngoing);
    }

    public static TripRoomUiModel upcoming(long tripId, String title, String ddayText, boolean isOwner,
                                            boolean isOngoing) {
        return new TripRoomUiModel(tripId, State.UPCOMING, title, ddayText, null, 0, isOwner, isOngoing);
    }

    public static TripRoomUiModel past(long tripId, String title, String ddayText, boolean isOwner) {
        return new TripRoomUiModel(tripId, State.PAST, title, ddayText, null, 0, isOwner, false);
    }
}
