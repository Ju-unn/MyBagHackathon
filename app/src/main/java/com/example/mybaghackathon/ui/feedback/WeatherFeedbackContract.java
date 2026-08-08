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

        void showError(String message);
    }

    interface Presenter {
        void loadFeedback(long tripId);

        void onDestroy();
    }
}
