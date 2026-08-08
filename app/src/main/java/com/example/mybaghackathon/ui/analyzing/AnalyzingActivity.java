package com.example.mybaghackathon.ui.analyzing;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.databinding.ActivityAnalyzingBinding;
import com.example.mybaghackathon.model.AnalysisResult;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.RestrictedItem;
import com.example.mybaghackathon.model.TripSchedule;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.analysisresult.AnalysisResultActivity;
import com.example.mybaghackathon.ui.upload.ScheduleUploadActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * S06 · 분석 중 — 화면 표시(View)만 담당. 실제 분석 API 호출은
 * AnalyzingPresenter가 처리함(MVP). 진행 문구를 한 줄씩 페이드 아웃/페이드 인으로
 * 반복 재생하며 보여주는 동안, Presenter가 뒤에서 분석 요청을 보낸다.
 */
public class AnalyzingActivity extends AppCompatActivity implements AnalyzingContract.View {

    public static final String EXTRA_ANALYSIS_ID = "analysis_id";
    public static final String EXTRA_DESTINATION_COUNTRY = "destination_country";
    public static final String EXTRA_DESTINATION_CITY = "destination_city";
    public static final String EXTRA_START_DATE = "start_date";
    public static final String EXTRA_END_DATE = "end_date";
    public static final String EXTRA_TRANSPORT_MODE = "transport_mode";
    public static final String EXTRA_ACCOMMODATION_NAME = "accommodation_name";
    public static final String EXTRA_RESTRICTED_ITEMS = "restricted_items";
    public static final String EXTRA_RECOMMENDED_ITEMS = "recommended_items";

    private static final int[] STEPS = {
            R.string.analyzing_step1, R.string.analyzing_step2,
            R.string.analyzing_step3, R.string.analyzing_step4};
    private static final long FADE_DURATION_MS = 300L;
    private static final long DISPLAY_DURATION_MS = 900L;

    private ActivityAnalyzingBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView stepText;
    private AnalyzingContract.Presenter presenter;
    private ArrayList<String> selectedPhotoPaths; // 취소 시 S05로 되돌려줄 사진 캐시 파일 경로 목록
    private String roomName;
    private int memberCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalyzingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        presenter = new AnalyzingPresenter(this,
                ((MyBagApplication) getApplication()).getAppContainer().analysisRepository);

        stepText = binding.analyzingStepText;
        stepText.setAlpha(0f);
        showStep(0);

        binding.analyzingCancel.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScheduleUploadActivity.class);
            intent.putStringArrayListExtra(ScheduleUploadActivity.EXTRA_SELECTED_URIS, selectedPhotoPaths);
            intent.putExtra(ScheduleUploadActivity.EXTRA_ROOM_NAME, roomName);
            intent.putExtra(ScheduleUploadActivity.EXTRA_MEMBER_COUNT, memberCount);
            startActivity(intent);
            finish();
        });

        long[] uploadIdsArray = getIntent().getLongArrayExtra(ScheduleUploadActivity.EXTRA_UPLOAD_IDS);
        selectedPhotoPaths = getIntent().getStringArrayListExtra(ScheduleUploadActivity.EXTRA_SELECTED_URIS);
        roomName = getIntent().getStringExtra(ScheduleUploadActivity.EXTRA_ROOM_NAME);
        memberCount = getIntent().getIntExtra(ScheduleUploadActivity.EXTRA_MEMBER_COUNT, 0);

        List<Long> uploadIds = new ArrayList<>();
        if (uploadIdsArray != null) {
            for (long id : uploadIdsArray) {
                uploadIds.add(id);
            }
        }
        presenter.startAnalysis(uploadIds);
    }

    // ===== AnalyzingContract.View =====

    @Override
    public void showAnalysisResult(AnalysisResult data) {
        if (isFinishing()) return;
        handler.removeCallbacksAndMessages(null);
        stepText.animate().cancel();

        TripSchedule schedule = data.getSchedule();

        Intent intent = new Intent(this, AnalysisResultActivity.class);
        intent.putExtra(EXTRA_ANALYSIS_ID, data.getAnalysisId());
        intent.putExtra(EXTRA_DESTINATION_COUNTRY, schedule.getDestinationCountry());
        intent.putExtra(EXTRA_DESTINATION_CITY, schedule.getDestinationCity());
        intent.putExtra(EXTRA_START_DATE, schedule.getStartDate());
        intent.putExtra(EXTRA_END_DATE, schedule.getEndDate());
        intent.putExtra(EXTRA_TRANSPORT_MODE, schedule.getTransportMode());
        intent.putExtra(EXTRA_ACCOMMODATION_NAME, data.getAccommodationName());
        intent.putExtra(EXTRA_RESTRICTED_ITEMS, new ArrayList<RestrictedItem>(data.getRestrictedItems()));
        intent.putExtra(EXTRA_RECOMMENDED_ITEMS, new ArrayList<PackingItem>(data.getRecommendedItems()));
        intent.putExtra(ScheduleUploadActivity.EXTRA_ROOM_NAME, roomName);
        intent.putExtra(ScheduleUploadActivity.EXTRA_MEMBER_COUNT, memberCount);
        startActivity(intent);
        finish();
    }

    @Override
    public void showError(String message) {
        if (isFinishing()) return;
        handler.removeCallbacksAndMessages(null);
        stepText.animate().cancel();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    // 분석 결과가 올 때까지 문구 4개를 계속 반복 재생함 (서버 응답 시간을 미리 알 수 없어서)
    private void showStep(int index) {
        int step = index % STEPS.length;
        stepText.setText(STEPS[step]);
        stepText.animate().alpha(1f).setDuration(FADE_DURATION_MS).withEndAction(() ->
                handler.postDelayed(() ->
                        stepText.animate().alpha(0f).setDuration(FADE_DURATION_MS).withEndAction(() ->
                                showStep(index + 1)).start(),
                        DISPLAY_DURATION_MS)).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        stepText.animate().cancel();
        presenter.onDestroy();
    }
}
