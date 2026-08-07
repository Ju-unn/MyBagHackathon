package com.example.mybaghackathon.ui.archive;

import com.example.mybaghackathon.ui.molecules.AvatarStackHelper;

import java.util.List;

/**
 * S14 공용 여행(아카이브) 화면의 여행방 카드 하나를 그리는 데 필요한 값 묶음.
 * 홈 화면의 상태 구분(진행중/준비전)과 달리, 아카이브의 "진행 중" 탭은 체크
 * 리스트 진행률 데이터가 있는지 여부로만 카드 모양을 나누고 라벨은 항상
 * "진행중"으로 보여준다({@link #ONGOING}/{@link #PLANNED}). 완료된 여행만
 * {@link #PAST}로 별도 표시된다.
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
