package com.example.mybaghackathon.ui.feedback;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityWeatherFeedbackBinding;
import com.example.mybaghackathon.ui.checklist.ChecklistActivity;
import com.google.android.material.button.MaterialButton;

/**
 * S09 · 날씨 피드백 — 여행에 대해 AI가 작성한 옷차림/음식/숙소 안내.
 *
 * 기능: 옷차림/음식/숙소 3개 섹션을 코드로 동적 생성해서 안내 문구를
 * 채워주고, "체크리스트 보기" 버튼으로 ChecklistActivity로 넘어가는 화면.
 */
public class WeatherFeedbackActivity extends AppCompatActivity {

    private ActivityWeatherFeedbackBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeatherFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView title = findViewById(R.id.topAppBarTitle);
        title.setText(R.string.feedback_title);
        findViewById(R.id.topAppBarAction).setVisibility(View.GONE);

        LinearLayout list = binding.feedbackSectionList;
        addSection(list, R.string.feedback_clothing_title,
                "낮 최고 14°, 밤 최저 6°로 일교차가 커요. 얇은 니트에 걸칠 수 있는 바람막이를 챙기세요.");
        addSection(list, R.string.feedback_food_title,
                "현지 음식은 대체로 담백한 편이에요. 향신료에 민감하다면 소화제를 챙기는 걸 추천해요.");
        addSection(list, R.string.feedback_stay_title,
                "숙소에 드라이어가 없을 수 있어요. 콘센트 규격이 다르니 멀티 어댑터를 준비하세요.");

        MaterialButton viewChecklist = binding.feedbackBottomCta.bottomCtaPrimary;
        viewChecklist.setText(R.string.feedback_view_checklist);
        viewChecklist.setOnClickListener(v -> {
            startActivity(new Intent(this, ChecklistActivity.class));
            finish();
        });
    }

    private void addSection(LinearLayout list, int titleRes, String body) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams sectionLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (list.getChildCount() > 0) sectionLp.topMargin = dp(24);
        section.setLayoutParams(sectionLp);

        TextView title = new TextView(this);
        title.setText(titleRes);
        title.setTextAppearance(R.style.TextAppearance_Bag_TitleS);
        section.addView(title);

        TextView desc = new TextView(this);
        desc.setText(body);
        desc.setTextAppearance(R.style.TextAppearance_Bag_BodyM);
        desc.setTextColor(ContextCompat.getColor(this, R.color.bag_text_secondary));
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        descLp.topMargin = dp(6);
        desc.setLayoutParams(descLp);
        section.addView(desc);

        list.addView(section);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
