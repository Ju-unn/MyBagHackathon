package com.example.mybaghackathon.ui.feedback;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.AppContainer;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.data.repository.WeatherRepository;
import com.example.mybaghackathon.databinding.ActivityWeatherFeedbackBinding;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.Weather;
import com.example.mybaghackathon.model.WeatherFeedback;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** S10 · 여행지와 날씨를 바탕으로 한 옷차림·음식·숙소 안내 화면. */
public class WeatherFeedbackActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "trip_id";
    public static final String EXTRA_REGION = "feedback_region";
    public static final String EXTRA_TEMPERATURE = "feedback_temperature";
    public static final String EXTRA_PERIOD = "feedback_period";
    public static final String EXTRA_CLOTHING_ADVICE = "feedback_clothing_advice";
    public static final String EXTRA_FOOD_ADVICE = "feedback_food_advice";
    public static final String EXTRA_ACCOMMODATION_ADVICE = "feedback_accommodation_advice";

    private ActivityWeatherFeedbackBinding binding;
    private TripRepository tripRepository;
    private WeatherRepository weatherRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long tripId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeatherFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        AppContainer container = ((MyBagApplication) getApplication()).getAppContainer();
        tripRepository = container.tripRepository;
        weatherRepository = container.weatherRepository;
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, -1L);

        binding.feedbackTopAppBar.topAppBarTitle.setText(R.string.feedback_title);
        binding.feedbackTopAppBar.topAppBarAction.setVisibility(View.GONE);

        bindIntentFallback();
        if (tripId > 0L) {
            loadFeedback();
        }
    }

    private void bindIntentFallback() {
        bindTextIfPresent(binding.feedbackRegion, EXTRA_REGION);
        bindTextIfPresent(binding.feedbackTemperature, EXTRA_TEMPERATURE);
        bindTextIfPresent(binding.feedbackPeriod, EXTRA_PERIOD);
        bindTextIfPresent(binding.feedbackClothingDesc, EXTRA_CLOTHING_ADVICE);
        bindTextIfPresent(binding.feedbackFoodDesc, EXTRA_FOOD_ADVICE);
        bindTextIfPresent(binding.feedbackStayDesc, EXTRA_ACCOMMODATION_ADVICE);
    }

    private void loadFeedback() {
        executor.execute(() -> {
            AppResult<Trip> tripResult = tripRepository.getTripDetail(tripId);
            AppResult<WeatherFeedback> feedbackResult = weatherRepository.getFeedback(tripId);
            AppResult<List<Weather>> forecastResult = null;

            if (tripResult.isSuccess() && tripResult.getData() != null) {
                Trip trip = tripResult.getData();
                if (hasText(trip.getDestinationCity())
                        && hasText(trip.getStartDate())
                        && hasText(trip.getEndDate())) {
                    forecastResult = weatherRepository.getForecast(
                            trip.getDestinationCity(), trip.getStartDate(), trip.getEndDate());
                }
            }

            AppResult<List<Weather>> finalForecastResult = forecastResult;
            runOnUiThread(() -> bindServerData(tripResult, finalForecastResult, feedbackResult));
        });
    }

    private void bindServerData(
            AppResult<Trip> tripResult,
            AppResult<List<Weather>> forecastResult,
            AppResult<WeatherFeedback> feedbackResult
    ) {
        if (!canUpdateUi()) {
            return;
        }

        if (tripResult.isSuccess() && tripResult.getData() != null) {
            Trip trip = tripResult.getData();
            binding.feedbackRegion.setText(
                    joinNonEmpty(" ", trip.getDestinationCountry(), trip.getDestinationCity()));
            binding.feedbackPeriod.setText(
                    joinNonEmpty(" ~ ", trip.getStartDate(), trip.getEndDate()));
        }

        if (forecastResult != null && forecastResult.isSuccess()) {
            binding.feedbackTemperature.setText(summarizeTemperature(forecastResult.getData()));
        }

        if (feedbackResult.isSuccess() && feedbackResult.getData() != null) {
            WeatherFeedback feedback = feedbackResult.getData();
            setTextIfPresent(binding.feedbackClothingDesc, feedback.getClothing());
            setTextIfPresent(binding.feedbackFoodDesc, feedback.getFood());
            setTextIfPresent(binding.feedbackStayDesc, feedback.getAccommodationNotes());
        } else {
            String message = feedbackResult.getError() == null
                    ? "날씨 팁을 불러오지 못했습니다."
                    : feedbackResult.getError().getMessage();
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private String summarizeTemperature(List<Weather> weatherList) {
        if (weatherList == null || weatherList.isEmpty()) {
            return "-";
        }
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (Weather weather : weatherList) {
            min = Math.min(min, weather.getTempMin());
            max = Math.max(max, weather.getTempMax());
        }
        return "최저 " + Math.round(min) + "° · 최고 " + Math.round(max) + "°";
    }

    private void bindTextIfPresent(TextView view, String extraKey) {
        setTextIfPresent(view, getIntent().getStringExtra(extraKey));
    }

    private void setTextIfPresent(TextView view, String value) {
        if (hasText(value)) {
            view.setText(value.trim());
        }
    }

    private String joinNonEmpty(String separator, String first, String second) {
        boolean hasFirst = hasText(first);
        boolean hasSecond = hasText(second);
        if (hasFirst && hasSecond) {
            return first.trim() + separator + second.trim();
        }
        if (hasFirst) {
            return first.trim();
        }
        if (hasSecond) {
            return second.trim();
        }
        return "-";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean canUpdateUi() {
        return binding != null && !isFinishing() && !isDestroyed();
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        binding = null;
        super.onDestroy();
    }
}
