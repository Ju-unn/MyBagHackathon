package com.example.mybaghackathon.ui.archive;

import com.example.mybaghackathon.ui.molecules.AvatarStackHelper;

import java.util.List;

/**
 * S14 공용 여행(아카이브) 화면의 여행방 카드 하나를 그리는 데 필요한 값 묶음.
 * "진행 중" 탭 안에서도 홈 화면과 동일하게 오늘이 여행 기간 안이면
 * {@link #ONGOING}(진행중), 아직 시작 전이면 {@link #PLANNED}(준비 전)로
 * 나눠 보여준다. 완료된 여행만 {@link #PAST}로 별도 표시된다.
 */
public class ArchiveTripUiModel {

    public enum State { ONGOING, PLANNED, PAST }

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

    private ArchiveTripUiModel(long tripId, State state, String title, String ddayText,
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

    public static ArchiveTripUiModel ongoing(long tripId, String title, String ddayText,
                                              List<AvatarStackHelper.Entry> avatars, int progressPercent,
                                              boolean isOwner, boolean isOngoing) {
        return new ArchiveTripUiModel(
                tripId, State.ONGOING, title, ddayText, avatars, progressPercent, isOwner, isOngoing);
    }

    public static ArchiveTripUiModel planned(long tripId, String title, String ddayText,
                                              List<AvatarStackHelper.Entry> avatars, int progressPercent,
                                              boolean isOwner, boolean isOngoing) {
        return new ArchiveTripUiModel(
                tripId, State.PLANNED, title, ddayText, avatars, progressPercent, isOwner, isOngoing);
    }

    public static ArchiveTripUiModel past(long tripId, String title, String ddayText, boolean isOwner) {
        return new ArchiveTripUiModel(tripId, State.PAST, title, ddayText, null, 0, isOwner, false);
    }
}
