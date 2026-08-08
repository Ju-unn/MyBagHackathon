package com.example.mybaghackathon.ui.atoms;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ViewAvatarBinding;

/**
 * A1 · 아바타 — 원형 이니셜 배지 (+ 선택적 프로필 이미지).
 * 크기는 사이즈 enum이 아니라 layout_width/layout_height 값(디자인 스펙 기준
 * 24/26/28/36dp)만으로 정해지므로 어떤 dp 값을 넣어도 그대로 동작함.
 *
 * 기능: 이니셜 텍스트와 배경색을 런타임에 바꿀 수 있는 원형 아바타 뷰.
 * view_avatar.xml을 inflate해서 내부 TextView를 채우고, 여러 아바타가
 * 겹쳐 표시될 때 배경과 구분되는 테두리(stroke)를 켤 수 있음. setImageUrl로
 * 실제 프로필 이미지를 주면 Glide로 원형 크롭해 이니셜 위에 겹쳐 그린다.
 */
public class AvatarView extends FrameLayout {

    private static final String TAG = "AvatarView";

    private ViewAvatarBinding binding;
    private int color;
    private boolean strokeEnabled;

    public AvatarView(Context context) {
        super(context);
        init(context, null);
    }

    public AvatarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AvatarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        binding = ViewAvatarBinding.inflate(LayoutInflater.from(context), this);
        color = ContextCompat.getColor(context, R.color.bag_avatar_2);
        strokeEnabled = false;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BagAvatarView);
            String initial = a.getString(R.styleable.BagAvatarView_avatarInitial);
            color = a.getColor(R.styleable.BagAvatarView_avatarColor, color);
            strokeEnabled = a.getBoolean(R.styleable.BagAvatarView_avatarStroke, false);
            a.recycle();
            if (initial != null) binding.avatarInitialText.setText(initial);
        }
        applyBackground();
    }

    public void setInitial(String initial) {
        binding.avatarInitialText.setText(initial);
    }

    // imageUrl이 없으면 이니셜만 보이고, 있으면 Glide로 원형 크롭해 이니셜 위에 덮어 그린다
    public void setImageUrl(@Nullable String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Log.d(TAG, "setImageUrl: 이미지 URL 없음 — 이니셜만 표시");
            binding.avatarImage.setVisibility(GONE);
            Glide.with(binding.avatarImage).clear(binding.avatarImage);
            return;
        }
        binding.avatarImage.setVisibility(VISIBLE);
        Glide.with(binding.avatarImage)
                .load(toHttps(imageUrl))
                .transform(new CircleCrop())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                 @NonNull Target<Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "프로필 이미지 로드 실패: " + imageUrl, e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model,
                                                    Target<Drawable> target, @NonNull DataSource dataSource,
                                                    boolean isFirstResource) {
                        Log.d(TAG, "프로필 이미지 로드 성공: " + imageUrl);
                        return false;
                    }
                })
                .into(binding.avatarImage);
    }

    // 카카오 프로필 이미지는 http:// URL로 오는 경우가 많은데, API 28+에서는 cleartext(HTTP)
    // 트래픽이 기본 차단되어 로드가 실패한다. 카카오 CDN은 https도 지원하므로 스킴만 올려서 쓴다.
    private String toHttps(String url) {
        if (url.startsWith("http://")) {
            return "https://" + url.substring("http://".length());
        }
        return url;
    }

    public void setAvatarColor(@ColorInt int colorInt) {
        this.color = colorInt;
        applyBackground();
    }

    public void setStrokeEnabled(boolean enabled, @ColorInt int strokeColorToMatchBackground) {
        this.strokeEnabled = enabled;
        applyBackground();
        if (enabled) {
            GradientDrawable ring = new GradientDrawable();
            ring.setShape(GradientDrawable.OVAL);
            ring.setColor(color);
            ring.setStroke(dp(2), strokeColorToMatchBackground);
            binding.avatarInitialText.setBackground(ring);
        }
    }

    private void applyBackground() {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(color);
        if (strokeEnabled) {
            LayerDrawable layered = new LayerDrawable(new android.graphics.drawable.Drawable[]{circle});
            binding.avatarInitialText.setBackground(layered);
        } else {
            binding.avatarInitialText.setBackground(circle);
        }
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
