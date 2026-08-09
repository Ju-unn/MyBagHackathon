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

    private ArchiveTripUiModel(long tripId, State state, String title, String ddayText,
                                List<AvatarStackHelper.Entry> avatars, int progressPercent) {
        this.tripId = tripId;
        this.state = state;
        this.title = title;
        this.ddayText = ddayText;
        this.avatars = avatars;
        this.progressPercent = progressPercent;
    }

    public static ArchiveTripUiModel ongoing(long tripId, String title, String ddayText,
                                              List<AvatarStackHelper.Entry> avatars, int progressPercent) {
        return new ArchiveTripUiModel(tripId, State.ONGOING, title, ddayText, avatars, progressPercent);
    }

    public static ArchiveTripUiModel planned(long tripId, String title, String ddayText) {
        return new ArchiveTripUiModel(tripId, State.PLANNED, title, ddayText, null, 0);
    }

    public static ArchiveTripUiModel past(long tripId, String title) {
        return new ArchiveTripUiModel(tripId, State.PAST, title, null, null, 0);
    }
}
