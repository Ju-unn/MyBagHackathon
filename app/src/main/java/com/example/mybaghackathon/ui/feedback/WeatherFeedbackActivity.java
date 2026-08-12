package com.example.mybaghackathon.ui.feedback;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.AppContainer;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.databinding.ActivityWeatherFeedbackBinding;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.Weather;
import com.example.mybaghackathon.model.WeatherFeedback;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** S10 여행지와 날씨를 바탕으로 한 옷차림·음식·숙소 안내 화면. */
public class WeatherFeedbackActivity extends AppCompatActivity
        implements WeatherFeedbackContract.View {

    public static final String EXTRA_TRIP_ID = "trip_id";
    public static final String EXTRA_REGION = "feedback_region";
    public static final String EXTRA_TEMPERATURE = "feedback_temperature";
    public static final String EXTRA_PERIOD = "feedback_period";
    public static final String EXTRA_CLOTHING_ADVICE = "feedback_clothing_advice";
    public static final String EXTRA_FOOD_ADVICE = "feedback_food_advice";
    public static final String EXTRA_ACCOMMODATION_ADVICE = "feedback_accommodation_advice";

    private ActivityWeatherFeedbackBinding binding;
    private WeatherFeedbackContract.Presenter presenter;
    private Trip lastTrip;
    private List<Weather> lastWeather = Collections.emptyList();
    private WeatherFeedback lastFeedback;
    private String terminalState;
    private String terminalMessage;

    private static final String STATE_CONTENT = "content";
    private static final String STATE_PENDING = "pending";
    private static final String STATE_EMPTY = "empty";

    @Override
    public void showLoading(boolean loading) {
        if (!canUpdateUi()) {
            return;
        }
        if (!loading) {
            binding.feedbackState.getRoot().setVisibility(View.GONE);
            return;
        }
        terminalState = null;
        binding.feedbackState.getRoot().setVisibility(View.VISIBLE);
        binding.feedbackState.screenStateProgress.setVisibility(View.VISIBLE);
        binding.feedbackState.screenStateTitle.setText(R.string.feedback_loading_title);
        binding.feedbackState.screenStateMessage.setText(R.string.feedback_loading_message);
        binding.feedbackState.screenStateRetry.setVisibility(View.GONE);
    }

    @Override
    public void showLoadError(String message) {
        if (!canUpdateUi()) {
            return;
        }
        terminalState = null;
        showState(R.string.feedback_error_title, message, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeatherFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        AppContainer container = ((MyBagApplication) getApplication()).getAppContainer();
        presenter = new WeatherFeedbackPresenter(
                this, container.tripRepository, container.weatherRepository);

        binding.feedbackTopAppBar.topAppBarTitle.setText(R.string.feedback_title);
        binding.feedbackTopAppBar.topAppBarAction.setVisibility(View.GONE);
        binding.feedbackTopAppBar.topAppBarLargeBack.setVisibility(View.VISIBLE);
        binding.feedbackTopAppBar.topAppBarLargeBack.setOnClickListener(v -> finish());
        bindIntentFallback();
        Object retained = getLastCustomNonConfigurationInstance();
        if (retained instanceof FeedbackScreenSnapshot) {
            restoreSnapshot((FeedbackScreenSnapshot) retained);
        } else {
            presenter.loadFeedback(getIntent().getLongExtra(EXTRA_TRIP_ID, -1L));
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

    @Override
    public void showTrip(Trip trip) {
        if (!canUpdateUi()) {
            return;
        }
        lastTrip = trip;
        binding.feedbackRegion.setText(
                joinNonEmpty(" ", trip.getDestinationCountry(), trip.getDestinationCity()));
        binding.feedbackPeriod.setText(
                joinNonEmpty(" ~ ", trip.getStartDate(), trip.getEndDate()));
    }

    @Override
    public void showForecast(List<Weather> weather) {
        if (canUpdateUi()) {
            lastWeather = weather == null
                    ? Collections.emptyList() : new ArrayList<>(weather);
            binding.feedbackTemperature.setText(summarizeTemperature(weather));
        }
    }

    @Override
    public void showFeedback(WeatherFeedback feedback) {
        if (!canUpdateUi()) {
            return;
        }
        lastFeedback = feedback;
        terminalState = STATE_CONTENT;
        terminalMessage = null;
        setTextIfPresent(binding.feedbackClothingDesc, feedback.getClothing());
        setTextIfPresent(binding.feedbackFoodDesc, feedback.getFood());
        setTextIfPresent(binding.feedbackStayDesc, feedback.getAccommodationNotes());
        binding.feedbackState.getRoot().setVisibility(View.GONE);
    }

    @Override
    public void showPending(String message) {
        if (canUpdateUi()) {
            terminalState = STATE_PENDING;
            terminalMessage = message;
            showState(R.string.feedback_pending_title, message, true);
        }
    }

    @Override
    public void showEmpty() {
        if (canUpdateUi()) {
            terminalState = STATE_EMPTY;
            terminalMessage = getString(R.string.feedback_empty_message);
            showState(R.string.feedback_empty_title,
                    terminalMessage, true);
        }
    }

    @Override
    public void showError(String message) {
        if (canUpdateUi()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showRetryableError(String message) {
        if (canUpdateUi()) {
            Snackbar.make(binding.feedbackContent, message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_retry, v -> presenter.retry())
                    .show();
        }
    }

    private void showState(int titleRes, String message, boolean retryable) {
        binding.feedbackState.getRoot().setVisibility(View.VISIBLE);
        binding.feedbackState.screenStateProgress.setVisibility(View.GONE);
        binding.feedbackState.screenStateTitle.setText(titleRes);
        binding.feedbackState.screenStateMessage.setText(message);
        binding.feedbackState.screenStateRetry.setVisibility(
                retryable ? View.VISIBLE : View.GONE);
        binding.feedbackState.screenStateRetry.setOnClickListener(
                retryable ? v -> presenter.retry() : null);
    }

    private void restoreSnapshot(FeedbackScreenSnapshot snapshot) {
        long tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, -1L);
        presenter.restoreFeedbackContext(tripId);
        showTrip(snapshot.trip);
        showForecast(snapshot.weather);
        if (STATE_PENDING.equals(snapshot.terminalState)) {
            showPending(snapshot.message);
        } else if (STATE_EMPTY.equals(snapshot.terminalState)) {
            showEmpty();
        } else if (snapshot.feedback != null) {
            showFeedback(snapshot.feedback);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (lastTrip == null || terminalState == null) {
            return null;
        }
        return new FeedbackScreenSnapshot(
                lastTrip,
                new ArrayList<>(lastWeather),
                lastFeedback,
                terminalState,
                terminalMessage);
    }

    private static final class FeedbackScreenSnapshot {
        private final Trip trip;
        private final List<Weather> weather;
        private final WeatherFeedback feedback;
        private final String terminalState;
        private final String message;

        private FeedbackScreenSnapshot(
                Trip trip,
                List<Weather> weather,
                WeatherFeedback feedback,
                String terminalState,
                String message
        ) {
            this.trip = trip;
            this.weather = weather;
            this.feedback = feedback;
            this.terminalState = terminalState;
            this.message = message;
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
        if (presenter != null) {
            presenter.onDestroy();
        }
        binding = null;
        super.onDestroy();
    }
}
