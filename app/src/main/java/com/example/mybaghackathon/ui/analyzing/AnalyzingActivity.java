package com.example.mybaghackathon.ui.analyzing;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.AnalysisRepository;
import com.example.mybaghackathon.databinding.ActivityAnalyzingBinding;
import com.example.mybaghackathon.model.AnalysisResult;
import com.example.mybaghackathon.model.TripSchedule;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.analysisresult.AnalysisResultActivity;
import com.example.mybaghackathon.ui.upload.ScheduleUploadActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * S06 · 분석 중 — 진행 문구를 한 줄씩 페이드 아웃/페이드 인으로 반복 재생하며
 * 보여주는 동안, 뒤에서 실제 분석 요청을 보내고 결과가 오면 S07로 이동함.
 */
public class AnalyzingActivity extends AppCompatActivity {

    public static final String EXTRA_ANALYSIS_ID = "analysis_id";
    public static final String EXTRA_DESTINATION_COUNTRY = "destination_country";
    public static final String EXTRA_DESTINATION_CITY = "destination_city";
    public static final String EXTRA_START_DATE = "start_date";
    public static final String EXTRA_END_DATE = "end_date";
    public static final String EXTRA_TRANSPORT_MODE = "transport_mode";
    public static final String EXTRA_ACCOMMODATION_NAME = "accommodation_name";

    private static final int[] STEPS = {
            R.string.analyzing_step1, R.string.analyzing_step2,
            R.string.analyzing_step3, R.string.analyzing_step4};
    private static final long FADE_DURATION_MS = 300L;
    private static final long DISPLAY_DURATION_MS = 900L;

    private ActivityAnalyzingBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView stepText;
    private AnalysisRepository analysisRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long[] uploadIdsArray; // 성공 시 S07로 다시 넘겨줘서, "다시 분석하기" 때 재사용할 수 있게 보관
    private ArrayList<Uri> selectedUris; // 취소 시 S05로 되돌려줄 사진 목록

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalyzingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        analysisRepository = ((MyBagApplication) getApplication()).getAppContainer().analysisRepository;

        stepText = binding.analyzingStepText;
        stepText.setAlpha(0f);
        showStep(0);

        binding.analyzingCancel.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScheduleUploadActivity.class);
            intent.putParcelableArrayListExtra(ScheduleUploadActivity.EXTRA_SELECTED_URIS, selectedUris);
            startActivity(intent);
            finish();
        });

        startAnalysis();
    }

    private void startAnalysis() {
        uploadIdsArray = getIntent().getLongArrayExtra(ScheduleUploadActivity.EXTRA_UPLOAD_IDS);
        selectedUris = getIntent().getParcelableArrayListExtra(ScheduleUploadActivity.EXTRA_SELECTED_URIS);
        List<Long> uploadIds = new ArrayList<>();
        if (uploadIdsArray != null) {
            for (long id : uploadIdsArray) {
                uploadIds.add(id);
            }
        }

        executor.execute(() -> {
            AppResult<AnalysisResult> result = analysisRepository.analyze(uploadIds);
            runOnUiThread(() -> handleResult(result));
        });
    }

    private void handleResult(AppResult<AnalysisResult> result) {
        if (isFinishing()) return;
        handler.removeCallbacksAndMessages(null);
        stepText.animate().cancel();

        if (result.isSuccess()) {
            AnalysisResult data = result.getData();
            TripSchedule schedule = data.getSchedule();

            Intent intent = new Intent(this, AnalysisResultActivity.class);
            intent.putExtra(ScheduleUploadActivity.EXTRA_UPLOAD_IDS, uploadIdsArray);
            intent.putExtra(EXTRA_ANALYSIS_ID, data.getAnalysisId());
            intent.putExtra(EXTRA_DESTINATION_COUNTRY, schedule.getDestinationCountry());
            intent.putExtra(EXTRA_DESTINATION_CITY, schedule.getDestinationCity());
            intent.putExtra(EXTRA_START_DATE, schedule.getStartDate());
            intent.putExtra(EXTRA_END_DATE, schedule.getEndDate());
            intent.putExtra(EXTRA_TRANSPORT_MODE, schedule.getTransportMode());
            intent.putExtra(EXTRA_ACCOMMODATION_NAME, data.getAccommodationName());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
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
        executor.shutdown();
    }
}
