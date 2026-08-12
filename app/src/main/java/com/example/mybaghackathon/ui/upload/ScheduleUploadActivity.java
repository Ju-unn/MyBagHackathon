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
import com.example.mybaghackathon.databinding.ActivityScheduleUploadBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.analyzing.AnalyzingActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * S05 · 일정 사진 업로드 — 화면 표시(View)만 담당. 사진 캐시 복사, 개수 제한,
 * 압축·업로드 흐름 같은 실제 로직은 UploadPresenter가 처리함(MVP).
 */
public class ScheduleUploadActivity extends AppCompatActivity implements UploadContract.View {

    public static final String EXTRA_UPLOAD_IDS = "upload_ids";
    public static final String EXTRA_SELECTED_URIS = "selected_uris";
    public static final String EXTRA_ROOM_NAME = "room_name";
    public static final String EXTRA_MEMBER_COUNT = "member_count";

    // registerForActivityResult는 onCreate/Presenter 생성보다 먼저 필드로 등록해야 해서
    // Presenter의 MAX_PHOTOS와 별개로 여기서도 필요함 (같은 값 5로 맞춰둠)
    private static final int MAX_PHOTOS = 5;

    private ActivityScheduleUploadBinding binding;
    private UploadContract.Presenter presenter;
    private String roomName;
    private int memberCount;
    private final Map<Uri, View> thumbnailViews = new HashMap<>();

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPicker =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(MAX_PHOTOS),
                    uris -> {
                        if (!uris.isEmpty()) presenter.onPhotosPicked(uris);
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        presenter = new UploadPresenter(this, getApplicationContext(),
                ((MyBagApplication) getApplication()).getAppContainer().uploadRepository);

        roomName = getIntent().getStringExtra(EXTRA_ROOM_NAME);
        memberCount = getIntent().getIntExtra(EXTRA_MEMBER_COUNT, 0);

        binding.uploadTopAppBar.topAppBarTitle.setText(R.string.upload_title);
        binding.uploadTopAppBar.topAppBarDesc.setText(R.string.upload_desc);
        binding.uploadTopAppBar.topAppBarDesc.setVisibility(View.VISIBLE);
        binding.uploadTopAppBar.topAppBarLargeBack.setVisibility(View.VISIBLE);
        binding.uploadTopAppBar.topAppBarLargeBack.setOnClickListener(v -> finish());

        binding.uploadDropzone.setOnClickListener(v -> {
            if (!presenter.canPickMorePhotos()) {
                showMaxPhotosReached();
                return;
            }
            photoPicker.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        binding.uploadBottomCta.bottomCtaDivider.setVisibility(View.VISIBLE);

        MaterialButton startAnalysis = binding.uploadBottomCta.bottomCtaPrimary;
        startAnalysis.setText(R.string.upload_start_analysis);
        startAnalysis.setOnClickListener(v -> presenter.onStartAnalysisClicked());

        // S06에서 "취소" 눌러서 돌아온 경우, 아까 고르던 사진 목록을 그대로 복원
        ArrayList<String> restoredPaths = getIntent().getStringArrayListExtra(EXTRA_SELECTED_URIS);
        if (restoredPaths != null) {
            presenter.onPhotosRestored(restoredPaths);
        }
    }

    // ===== UploadContract.View =====

    @Override
    public void showThumbnail(Uri uri) {
        LinearLayout previewRow = binding.uploadPreviewRow;
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
        deleteBadge.setOnClickListener(v -> presenter.onPhotoRemoved(uri));
        wrapper.addView(deleteBadge);

        previewRow.addView(wrapper, wrapperLp);
        thumbnailViews.put(uri, wrapper);
    }

    @Override
    public void removeThumbnail(Uri uri) {
        View wrapper = thumbnailViews.remove(uri);
        if (wrapper != null) {
            binding.uploadPreviewRow.removeView(wrapper);
        }
    }

    @Override
    public void showMaxPhotosReached() {
        Toast.makeText(this, R.string.upload_error_max_photos, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNoPhotosError() {
        Toast.makeText(this, R.string.upload_error_no_photos, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showCompressError() {
        Toast.makeText(this, R.string.upload_error_compress_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showUploadError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setUploading(boolean uploading) {
        binding.uploadBottomCta.bottomCtaPrimary.setEnabled(!uploading);
    }

    @Override
    public void navigateToAnalyzing(long[] uploadIds, ArrayList<String> selectedPaths) {
        Intent intent = new Intent(this, AnalyzingActivity.class);
        intent.putExtra(EXTRA_UPLOAD_IDS, uploadIds);
        intent.putStringArrayListExtra(EXTRA_SELECTED_URIS, selectedPaths);
        intent.putExtra(EXTRA_ROOM_NAME, roomName);
        intent.putExtra(EXTRA_MEMBER_COUNT, memberCount);
        startActivity(intent);
        finish();
    }

    // ===== 사진 전체화면 미리보기 (순수 화면 로직이라 그대로 유지) =====

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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
