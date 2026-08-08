package com.example.mybaghackathon.ui.feedback;

import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.Weather;
import com.example.mybaghackathon.model.WeatherFeedback;

import java.util.List;

/** S10 날씨·의식주 팁 화면의 View/Presenter 계약. */
public interface WeatherFeedbackContract {

    interface View {
        void showTrip(Trip trip);

        void showForecast(List<Weather> weather);

        void showFeedback(WeatherFeedback feedback);

        // 예보/팁이 아직 체크포인트 갱신 전(ready=false)일 때 안내 문구를 보여준다. 에러 아님(showError와 구분)
        void showPending(String message);

        void showError(String message);
    }

    interface Presenter {
        void loadFeedback(long tripId);

        void onDestroy();
    }
}
