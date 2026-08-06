package com.example.mybaghackathon.ui.upload;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityScheduleUploadBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.analyzing.AnalyzingActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

/**
 * S05 · 일정 사진 업로드 — 드롭존을 탭해 일정표·예약 확인 사진을 한 번에
 * 여러 장 선택하고, 선택된 사진은 하단 미리보기 줄에 썸네일로 쌓인다.
 *
 * 기능: 시스템 포토 피커로 다중 이미지를 선택해 미리보기 줄에 채우고,
 * "AI 분석 시작하기" 버튼으로 AnalyzingActivity로 이동하는 화면.
 */
public class ScheduleUploadActivity extends AppCompatActivity {

    private ActivityScheduleUploadBinding binding;

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPicker =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(),
                    this::onPhotosPicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        binding.uploadTopAppBar.topAppBarTitle.setText(R.string.upload_title);
        binding.uploadTopAppBar.topAppBarDesc.setText(R.string.upload_desc);
        binding.uploadTopAppBar.topAppBarDesc.setVisibility(View.VISIBLE);

        binding.uploadDropzone.setOnClickListener(v -> photoPicker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        binding.uploadBottomCta.bottomCtaDivider.setVisibility(View.VISIBLE);

        MaterialButton startAnalysis = binding.uploadBottomCta.bottomCtaPrimary;
        startAnalysis.setText(R.string.upload_start_analysis);
        startAnalysis.setOnClickListener(v -> {
            startActivity(new Intent(this, AnalyzingActivity.class));
            finish();
        });
    }

    private void onPhotosPicked(List<Uri> uris) {
        if (uris.isEmpty()) return;
        LinearLayout previewRow = binding.uploadPreviewRow;
        int size = dp(64);
        int gap = dp(10);
        for (Uri uri : uris) {
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
