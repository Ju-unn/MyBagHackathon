package com.example.mybaghackathon.ui.roomdetail;

import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.Weather;

import java.util.List;

/** S09 방 상세 화면에서 View와 Presenter가 주고받는 동작을 정의한다. */
public interface RoomDetailContract {

    interface View {
        void showLoading(boolean loading);

        void showLoadError(String message);

        void showTrip(Trip trip, boolean isHost);

        void showWeather(List<Weather> weather);

        void showWeatherPending(String message);

        void showWeatherEmpty();

        void showPackingRestrictions(List<PackingItem> items);

        void showError(String message);

        void showRetryableError(String message);

        void openWeatherFeedback(long tripId);

        void openChecklist(long tripId, int memberCount, boolean isHost);

        void showInviteShare(String inviteCode);
    }

    interface Presenter {
        void loadRoom(long tripId, boolean initialHost, String inviteCode);

        void restoreRoomContext(long tripId, boolean isHost, int memberCount, String inviteCode);

        void retry();

        void onTipsClicked();

        void onChecklistClicked();

        void onInviteClicked();

        void onDestroy();
    }
}
