package com.example.mybaghackathon.ui.analysisresult;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.databinding.ActivityAnalysisResultBinding;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.RestrictedItem;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.analyzing.AnalyzingActivity;
import com.example.mybaghackathon.ui.overlay.EditFieldSheet;
import com.example.mybaghackathon.ui.review.ScheduleReviewActivity;
import com.example.mybaghackathon.ui.upload.ScheduleUploadActivity;

import java.util.ArrayList;

/**
 * S07 · 1차 결과 확인 — 화면 표시(View)만 담당. 필드 포맷팅, 수정 처리, 다음 화면
 * 데이터 구성 같은 로직은 AnalysisResultPresenter가 처리함(MVP).
 */
public class AnalysisResultActivity extends AppCompatActivity implements AnalysisResultContract.View {

    private ActivityAnalysisResultBinding binding;
    private AnalysisResultContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalysisResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        presenter = new AnalysisResultPresenter(this, getApplicationContext(),
                ((MyBagApplication) getApplication()).getAppContainer().analysisRepository);

        binding.confirmTopAppBar.topAppBarCompactTitle.setText(R.string.schedule_confirm_title);
        binding.confirmTopAppBar.topAppBarBack.setOnClickListener(v -> finish());

        setLabel(binding.confirmDestinationCard, R.string.schedule_confirm_destination_label);
        setLabel(binding.confirmScheduleCard, R.string.schedule_confirm_schedule_label);
        setLabel(binding.confirmLodgingCard, R.string.schedule_confirm_lodging_label);
        setLabel(binding.confirmTransportCard, R.string.schedule_confirm_transport_label);

        binding.confirmDestinationCard.findViewById(R.id.reviewFieldEdit)
                .setOnClickListener(v -> showDestinationEditDialog());
        binding.confirmScheduleCard.findViewById(R.id.reviewFieldEdit)
                .setOnClickListener(v -> showScheduleEditDialog());
        binding.confirmLodgingCard.findViewById(R.id.reviewFieldEdit)
                .setOnClickListener(v -> showLodgingEditDialog());
        binding.confirmTransportCard.findViewById(R.id.reviewFieldEdit)
                .setOnClickListener(v -> showTransportEditDialog());

        binding.confirmNextButton.setOnClickListener(v -> presenter.onNextClicked());

        Intent intent = getIntent();
        long analysisId = intent.getLongExtra(AnalyzingActivity.EXTRA_ANALYSIS_ID, -1);
        String destinationCountry = intent.getStringExtra(AnalyzingActivity.EXTRA_DESTINATION_COUNTRY);
        String destinationCity = intent.getStringExtra(AnalyzingActivity.EXTRA_DESTINATION_CITY);
        String startDate = intent.getStringExtra(AnalyzingActivity.EXTRA_START_DATE);
        String endDate = intent.getStringExtra(AnalyzingActivity.EXTRA_END_DATE);
        String transportMode = intent.getStringExtra(AnalyzingActivity.EXTRA_TRANSPORT_MODE);
        String accommodationName = intent.getStringExtra(AnalyzingActivity.EXTRA_ACCOMMODATION_NAME);
        String roomName = intent.getStringExtra(ScheduleUploadActivity.EXTRA_ROOM_NAME);
        int memberCount = intent.getIntExtra(ScheduleUploadActivity.EXTRA_MEMBER_COUNT, 0);
        @SuppressWarnings("unchecked")
        ArrayList<RestrictedItem> restrictedItems =
                (ArrayList<RestrictedItem>) intent.getSerializableExtra(AnalyzingActivity.EXTRA_RESTRICTED_ITEMS);
        @SuppressWarnings("unchecked")
        ArrayList<PackingItem> recommendedItems =
                (ArrayList<PackingItem>) intent.getSerializableExtra(AnalyzingActivity.EXTRA_RECOMMENDED_ITEMS);

        presenter.init(analysisId, roomName, memberCount, restrictedItems, recommendedItems,
                destinationCountry, destinationCity, startDate, endDate, accommodationName, transportMode);
    }

    private void setLabel(View card, int labelRes) {
        ((TextView) card.findViewById(R.id.reviewFieldLabel)).setText(labelRes);
    }

    private void setValue(View card, String value) {
        ((TextView) card.findViewById(R.id.reviewFieldValue)).setText(value);
    }

    // ===== AnalysisResultContract.View =====

    @Override
    public void showDestination(String value) {
        setValue(binding.confirmDestinationCard, value);
    }

    @Override
    public void showSchedule(String value) {
        setValue(binding.confirmScheduleCard, value);
    }

    @Override
    public void showLodging(String value) {
        setValue(binding.confirmLodgingCard, value);
    }

    @Override
    public void showTransport(String value) {
        setValue(binding.confirmTransportCard, value);
    }

    @Override
    public void showInvalidDateError() {
        Toast.makeText(this, R.string.schedule_confirm_invalid_date, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setConfirming(boolean confirming) {
        binding.confirmNextButton.setEnabled(!confirming);
    }

    @Override
    public void showConfirmError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToReview(long analysisId, String roomName, int memberCount,
                                  ArrayList<RestrictedItem> restrictedItems, ArrayList<PackingItem> recommendedItems,
                                  String destinationCountry, String destinationCity, String startDate, String endDate) {
        Intent next = new Intent(this, ScheduleReviewActivity.class);
        next.putExtra(AnalyzingActivity.EXTRA_ANALYSIS_ID, analysisId);
        next.putExtra(ScheduleUploadActivity.EXTRA_ROOM_NAME, roomName);
        next.putExtra(ScheduleUploadActivity.EXTRA_MEMBER_COUNT, memberCount);
        next.putExtra(AnalyzingActivity.EXTRA_RESTRICTED_ITEMS, restrictedItems);
        next.putExtra(AnalyzingActivity.EXTRA_RECOMMENDED_ITEMS, recommendedItems);
        next.putExtra(AnalyzingActivity.EXTRA_DESTINATION_COUNTRY, destinationCountry);
        next.putExtra(AnalyzingActivity.EXTRA_DESTINATION_CITY, destinationCity);
        next.putExtra(AnalyzingActivity.EXTRA_START_DATE, startDate);
        next.putExtra(AnalyzingActivity.EXTRA_END_DATE, endDate);
        startActivity(next);
        finish();
    }

    // ===== 수정 다이얼로그 (순수 화면 로직이라 그대로 유지) =====

    private void showDestinationEditDialog() {
        ArrayList<String> hints = new ArrayList<>();
        hints.add(getString(R.string.schedule_confirm_destination_label));
        ArrayList<String> values = new ArrayList<>();
        values.add(presenter.getDestinationDisplay());

        EditFieldSheet sheet = EditFieldSheet.newInstance(
                getString(R.string.schedule_confirm_destination_label), hints, values);
        sheet.setOnFieldsSavedListener(result -> {
            presenter.onDestinationEdited(result.get(0));
            return true;
        });
        sheet.show(getSupportFragmentManager(), "edit_destination");
    }

    // 일정은 S08 날씨 조회에 실제로 쓰이는 값이라, 자유 텍스트 대신 시작일/종료일을 각각 입력받음
    private void showScheduleEditDialog() {
        ArrayList<String> hints = new ArrayList<>();
        hints.add(getString(R.string.schedule_confirm_start_date_hint));
        hints.add(getString(R.string.schedule_confirm_end_date_hint));
        ArrayList<String> values = new ArrayList<>();
        values.add(presenter.getStartDate());
        values.add(presenter.getEndDate());

        EditFieldSheet sheet = EditFieldSheet.newInstance(
                getString(R.string.schedule_confirm_schedule_label), hints, values, new boolean[]{true, true});
        sheet.setOnFieldsSavedListener(result -> presenter.onScheduleEdited(result.get(0), result.get(1)));
        sheet.show(getSupportFragmentManager(), "edit_schedule");
    }

    private void showLodgingEditDialog() {
        ArrayList<String> hints = new ArrayList<>();
        hints.add(getString(R.string.schedule_confirm_lodging_label));
        ArrayList<String> values = new ArrayList<>();
        values.add(presenter.getLodgingDisplay());

        EditFieldSheet sheet = EditFieldSheet.newInstance(
                getString(R.string.schedule_confirm_lodging_label), hints, values);
        sheet.setOnFieldsSavedListener(result -> {
            presenter.onLodgingEdited(result.get(0));
            return true;
        });
        sheet.show(getSupportFragmentManager(), "edit_lodging");
    }

    private void showTransportEditDialog() {
        ArrayList<String> hints = new ArrayList<>();
        hints.add(getString(R.string.schedule_confirm_transport_label));
        ArrayList<String> values = new ArrayList<>();
        values.add(presenter.getTransportDisplay());

        EditFieldSheet sheet = EditFieldSheet.newInstance(
                getString(R.string.schedule_confirm_transport_label), hints, values);
        sheet.setOnFieldsSavedListener(result -> {
            presenter.onTransportEdited(result.get(0));
            return true;
        });
        sheet.show(getSupportFragmentManager(), "edit_transport");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
