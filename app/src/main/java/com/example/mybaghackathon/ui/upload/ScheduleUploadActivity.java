package com.example.mybaghackathon.ui.upload;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.UploadRepository;
import com.example.mybaghackathon.databinding.ActivityScheduleUploadBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.analyzing.AnalyzingActivity;
import com.example.mybaghackathon.util.ImageCompressor;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * S05 · 일정 사진 업로드 — 드롭존을 탭해 일정표·예약 확인 사진을 한 번에
 * 여러 장 선택하고, 선택된 사진은 하단 미리보기 줄에 썸네일로 쌓인다.
 *
 * 기능: 시스템 포토 피커로 다중 이미지를 선택해 미리보기 줄에 채우고,
 * "AI 분석 시작하기" 버튼을 누르면 선택된 사진을 압축해 업로드한 뒤
 * 받은 uploadIds를 들고 AnalyzingActivity로 이동하는 화면.
 */
public class ScheduleUploadActivity extends AppCompatActivity {

    public static final String EXTRA_UPLOAD_IDS = "upload_ids";
    public static final String EXTRA_SELECTED_URIS = "selected_uris";

    // 사진 종류는 사용자가 직접 고르지 않는다 — 일정표·숙소예약·항공권 등 섞인 사진을
    // 그대로 올리면 AI가 분석해서 구분한다(S07/S08 결과 화면 참고). 그래서 업로드
    // 시점엔 전부 ITINERARY로 보내는 게 의도된 동작.
    private static final String UPLOAD_TYPE_ITINERARY = "ITINERARY";

    private ActivityScheduleUploadBinding binding;
    private UploadRepository uploadRepository;
    private final List<Uri> selectedUris = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPicker =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(),
                    this::onPhotosPicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        uploadRepository = ((MyBagApplication) getApplication()).getAppContainer().uploadRepository;

        binding.uploadTopAppBar.topAppBarTitle.setText(R.string.upload_title);
        binding.uploadTopAppBar.topAppBarDesc.setText(R.string.upload_desc);
        binding.uploadTopAppBar.topAppBarDesc.setVisibility(View.VISIBLE);

        binding.uploadDropzone.setOnClickListener(v -> photoPicker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        binding.uploadBottomCta.bottomCtaDivider.setVisibility(View.VISIBLE);

        MaterialButton startAnalysis = binding.uploadBottomCta.bottomCtaPrimary;
        startAnalysis.setText(R.string.upload_start_analysis);
        startAnalysis.setOnClickListener(v -> onStartAnalysis(startAnalysis));

        // S06에서 "취소" 눌러서 돌아온 경우, 아까 고르던 사진 목록을 그대로 복원
        ArrayList<Uri> restoredUris = getIntent().getParcelableArrayListExtra(EXTRA_SELECTED_URIS);
        if (restoredUris != null) {
            addPhotos(restoredUris);
        }
    }

    private void onPhotosPicked(List<Uri> uris) {
        if (uris.isEmpty()) return;
        addPhotos(uris);
    }

    private void addPhotos(List<Uri> uris) {
        LinearLayout previewRow = binding.uploadPreviewRow;
        int size = dp(64);
        int gap = dp(10);
        for (Uri uri : uris) {
            if (selectedUris.contains(uri)) continue; // 이미 고른 사진은 중복 추가하지 않음
            selectedUris.add(uri);

            ShapeableImageView thumb = new ShapeableImageView(this);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.setShapeAppearanceModel(thumb.getShapeAppearanceModel().toBuilder()
                    .setAllCornerSizes(dp(14))
                    .build());
            thumb.setImageURI(uri);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            if (previewRow.getChildCount() > 0) lp.setMarginStart(gap);
            previewRow.addView(thumb, lp);
        }
    }

    private void onStartAnalysis(MaterialButton button) {
        if (selectedUris.isEmpty()) {
            Toast.makeText(this, R.string.upload_error_no_photos, Toast.LENGTH_SHORT).show();
            return;
        }

        button.setEnabled(false);
        List<Uri> uris = new ArrayList<>(selectedUris);
        executor.execute(() -> {
            List<File> compressed = new ArrayList<>();
            try {
                for (Uri uri : uris) {
                    compressed.add(ImageCompressor.compress(this, uri));
                }

                AppResult<List<Long>> result = uploadRepository.upload(compressed, UPLOAD_TYPE_ITINERARY);
                runOnUiThread(() -> handleUploadResult(button, result));
            } catch (IOException e) {
                runOnUiThread(() -> {
                    button.setEnabled(true);
                    Toast.makeText(this, R.string.upload_error_compress_failed, Toast.LENGTH_SHORT).show();
                });
            } finally {
                // 업로드 성공/실패와 무관하게 압축된 임시 파일은 더 이상 필요 없으므로 캐시 폴더에서 정리
                for (File file : compressed) {
                    file.delete();
                }
            }
        });
    }

    private void handleUploadResult(MaterialButton button, AppResult<List<Long>> result) {
        if (isFinishing()) return;
        if (result.isSuccess()) {
            long[] uploadIds = new long[result.getData().size()];
            for (int i = 0; i < uploadIds.length; i++) {
                uploadIds[i] = result.getData().get(i);
            }
            Intent intent = new Intent(this, AnalyzingActivity.class);
            intent.putExtra(EXTRA_UPLOAD_IDS, uploadIds);
            intent.putParcelableArrayListExtra(EXTRA_SELECTED_URIS, new ArrayList<>(selectedUris));
            startActivity(intent);
            finish();
        } else {
            button.setEnabled(true);
            Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
