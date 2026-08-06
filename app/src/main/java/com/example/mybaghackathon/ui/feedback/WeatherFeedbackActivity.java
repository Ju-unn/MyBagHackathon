package com.example.mybaghackathon.ui.feedback;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityWeatherFeedbackBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;

/**
 * S09 · 날씨 피드백 — 여행에 대해 AI가 작성한 옷차림/음식/숙소 안내.
 *
 * 기능: 전달받은 여행지, 기온, 여행 기간, 옷차림/음식/숙소 안내를
 * XML에 정의된 각 TextView에 표시하는 화면.
 */
public class WeatherFeedbackActivity extends AppCompatActivity {

    public static final String EXTRA_REGION = "feedback_region";
    public static final String EXTRA_TEMPERATURE = "feedback_temperature";
    public static final String EXTRA_PERIOD = "feedback_period";
    public static final String EXTRA_CLOTHING_ADVICE = "feedback_clothing_advice";
    public static final String EXTRA_FOOD_ADVICE = "feedback_food_advice";
    public static final String EXTRA_ACCOMMODATION_ADVICE = "feedback_accommodation_advice";

    private ActivityWeatherFeedbackBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeatherFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        binding.feedbackTopAppBar.topAppBarTitle.setText(R.string.feedback_title);
        binding.feedbackTopAppBar.topAppBarAction.setVisibility(View.GONE);

        bindTextIfPresent(binding.feedbackRegion, EXTRA_REGION);
        bindTextIfPresent(binding.feedbackTemperature, EXTRA_TEMPERATURE);
        bindTextIfPresent(binding.feedbackPeriod, EXTRA_PERIOD);
        bindTextIfPresent(binding.feedbackClothingDesc, EXTRA_CLOTHING_ADVICE);
        bindTextIfPresent(binding.feedbackFoodDesc, EXTRA_FOOD_ADVICE);
        bindTextIfPresent(binding.feedbackStayDesc, EXTRA_ACCOMMODATION_ADVICE);
    }

    private void bindTextIfPresent(TextView view, String extraKey) {
        String value = getIntent().getStringExtra(extraKey);
        if (value != null && !value.trim().isEmpty()) {
            view.setText(value);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
