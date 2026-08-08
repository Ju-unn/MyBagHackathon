package com.example.mybaghackathon.ui.upload;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    public static final String EXTRA_ROOM_NAME = "room_name";
    public static final String EXTRA_MEMBER_COUNT = "member_count";

    private static final int MAX_PHOTOS = 5;

    // 사진 종류는 사용자가 직접 고르지 않는다 — 일정표·숙소예약·항공권 등 섞인 사진을
    // 그대로 올리면 AI가 분석해서 구분한다(S07/S08 결과 화면 참고). 그래서 업로드
    // 시점엔 전부 ITINERARY로 보내는 게 의도된 동작.
    private static final String UPLOAD_TYPE_ITINERARY = "ITINERARY";

    private ActivityScheduleUploadBinding binding;
    private UploadRepository uploadRepository;
    private String roomName;
    private int memberCount;
    private final List<Uri> selectedUris = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPicker =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(MAX_PHOTOS),
                    this::onPhotosPicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        uploadRepository = ((MyBagApplication) getApplication()).getAppContainer().uploadRepository;

        roomName = getIntent().getStringExtra(EXTRA_ROOM_NAME);
        memberCount = getIntent().getIntExtra(EXTRA_MEMBER_COUNT, 0);

        binding.uploadTopAppBar.topAppBarTitle.setText(R.string.upload_title);
        binding.uploadTopAppBar.topAppBarDesc.setText(R.string.upload_desc);
        binding.uploadTopAppBar.topAppBarDesc.setVisibility(View.VISIBLE);

        binding.uploadDropzone.setOnClickListener(v -> {
            if (selectedUris.size() >= MAX_PHOTOS) {
                Toast.makeText(this, R.string.upload_error_max_photos, Toast.LENGTH_SHORT).show();
                return;
            }
            photoPicker.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        binding.uploadBottomCta.bottomCtaDivider.setVisibility(View.VISIBLE);

        MaterialButton startAnalysis = binding.uploadBottomCta.bottomCtaPrimary;
        startAnalysis.setText(R.string.upload_start_analysis);
        startAnalysis.setOnClickListener(v -> onStartAnalysis(startAnalysis));

        // S06에서 "취소" 눌러서 돌아온 경우, 아까 고르던 사진 목록을 그대로 복원.
        // 캐시에 이미 복사해둔 파일 경로라서 그대로 다시 읽을 수 있음
        ArrayList<String> restoredPaths = getIntent().getStringArrayListExtra(EXTRA_SELECTED_URIS);
        if (restoredPaths != null) {
            List<Uri> restoredUris = new ArrayList<>();
            for (String path : restoredPaths) {
                restoredUris.add(Uri.fromFile(new File(path)));
            }
            addPhotos(restoredUris);
        }
    }

    private void onPhotosPicked(List<Uri> uris) {
        if (uris.isEmpty()) return;
        // 포토피커가 주는 주소(content://media/picker/...)는 이 화면 인스턴스가 없어지면
        // 못 읽게 되는 임시 권한이라, 고르자마자 우리 캐시 폴더로 복사해서 화면이
        // 다시 생성돼도(취소 왕복 등) 안전하게 다시 읽을 수 있는 주소로 바꿔둠
        List<Uri> cachedUris = new ArrayList<>();
        for (Uri uri : uris) {
            Uri cached = cachePickedPhoto(uri);
            if (cached != null) cachedUris.add(cached);
        }
        addPhotos(cachedUris);
    }

    private Uri cachePickedPhoto(Uri sourceUri) {
        File outFile = new File(getCacheDir(), "picked_" + System.nanoTime() + ".jpg");
        try (InputStream in = getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(outFile)) {
            if (in == null) return null;
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return Uri.fromFile(outFile);
        } catch (IOException e) {
            return null;
        }
    }

    private void addPhotos(List<Uri> uris) {
        LinearLayout previewRow = binding.uploadPreviewRow;
        boolean skippedSome = false;
        for (Uri uri : uris) {
            if (selectedUris.contains(uri)) continue; // 이미 고른 사진은 중복 추가하지 않음
            if (selectedUris.size() >= MAX_PHOTOS) {
                skippedSome = true;
                continue;
            }
            selectedUris.add(uri);
            addThumbnail(previewRow, uri);
        }
        if (skippedSome) {
            Toast.makeText(this, R.string.upload_error_max_photos, Toast.LENGTH_SHORT).show();
        }
    }

    // 썸네일(탭하면 확대) + 우측 상단 삭제 배지가 있는 미리보기 타일 하나를 추가한다
    private void addThumbnail(LinearLayout previewRow, Uri uri) {
        int size = dp(64);
        int gap = dp(10);

        FrameLayout wrapper = new FrameLayout(this);
        LinearLayout.LayoutParams wrapperLp = new LinearLayout.LayoutParams(size, size);
        if (previewRow.getChildCount() > 0) wrapperLp.setMarginStart(gap);

        ShapeableImageView thumb = new ShapeableImageView(this);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumb.setShapeAppearanceModel(thumb.getShapeAppearanceModel().toBuilder()
                .setAllCornerSizes(dp(14))
                .build());
        thumb.setImageURI(uri);
        thumb.setLayoutParams(new FrameLayout.LayoutParams(size, size));
        thumb.setOnClickListener(v -> showPhotoPreview(uri));
        wrapper.addView(thumb);

        ImageView deleteBadge = new ImageView(this);
        int badgeSize = dp(20);
        FrameLayout.LayoutParams badgeLp = new FrameLayout.LayoutParams(badgeSize, badgeSize);
        badgeLp.gravity = Gravity.TOP | Gravity.END;
        badgeLp.topMargin = dp(2);
        badgeLp.rightMargin = dp(2);
        deleteBadge.setLayoutParams(badgeLp);
        deleteBadge.setBackgroundResource(R.drawable.bg_photo_delete_badge);
        deleteBadge.setImageResource(R.drawable.ic_close_small);
        deleteBadge.setColorFilter(Color.WHITE);
        int iconPadding = dp(4);
        deleteBadge.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        deleteBadge.setOnClickListener(v -> removePhoto(uri, wrapper, previewRow));
        wrapper.addView(deleteBadge);

        previewRow.addView(wrapper, wrapperLp);
    }

    private void removePhoto(Uri uri, View wrapper, LinearLayout previewRow) {
        selectedUris.remove(uri);
        previewRow.removeView(wrapper);
    }

    private void showPhotoPreview(Uri uri) {
        Dialog dialog = new Dialog(this, R.style.Theme_Bag_FullscreenDialog);
        dialog.setContentView(buildPhotoPreviewLayout(dialog, uri));
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    // 화면 전체를 덮는 검은 배경 대신, 살짝 어둡게만 처리한 배경 위에
    // 우리 카드 색(흰색·둥근모서리)의 팝업 카드 안에 사진을 보여줌
    private View buildPhotoPreviewLayout(Dialog dialog, Uri uri) {
        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.setBackgroundColor(0x99000000);
        root.setOnClickListener(v -> dialog.dismiss());

        FrameLayout card = new FrameLayout(this);
        card.setBackgroundResource(R.drawable.bg_card_photo_preview);
        card.setClickable(true); // 카드 안쪽을 탭했을 땐 닫히지 않도록 터치를 여기서 소비함
        int cardPadding = dp(8);
        card.setPadding(cardPadding, cardPadding, cardPadding, cardPadding);
        FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.gravity = Gravity.CENTER;
        int sideMargin = dp(24);
        cardLp.setMargins(sideMargin, dp(96), sideMargin, dp(96));

        ShapeableImageView image = new ShapeableImageView(this);
        image.setAdjustViewBounds(true);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setMaxHeight((int) (getResources().getDisplayMetrics().heightPixels * 0.55f));
        image.setImageURI(uri);
        image.setShapeAppearanceModel(image.getShapeAppearanceModel().toBuilder()
                .setAllCornerSizes(getResources().getDimension(R.dimen.radius_lg))
                .build());
        image.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        card.addView(image);

        root.addView(card, cardLp);

        ImageView closeButton = new ImageView(this);
        int closeSize = dp(36);
        FrameLayout.LayoutParams closeLp = new FrameLayout.LayoutParams(closeSize, closeSize);
        closeLp.gravity = Gravity.TOP | Gravity.END;
        closeLp.topMargin = dp(52);
        closeLp.rightMargin = dp(20);
        closeButton.setLayoutParams(closeLp);
        closeButton.setBackgroundResource(R.drawable.bg_photo_preview_close);
        closeButton.setImageResource(R.drawable.ic_close_small);
        closeButton.setColorFilter(getColor(R.color.bag_text_primary));
        int iconPadding = dp(8);
        closeButton.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        closeButton.setOnClickListener(v -> dialog.dismiss());
        root.addView(closeButton);

        return root;
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
            ArrayList<String> selectedPaths = new ArrayList<>();
            for (Uri uri : selectedUris) {
                selectedPaths.add(uri.getPath());
            }
            intent.putStringArrayListExtra(EXTRA_SELECTED_URIS, selectedPaths);
            intent.putExtra(EXTRA_ROOM_NAME, roomName);
            intent.putExtra(EXTRA_MEMBER_COUNT, memberCount);
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
