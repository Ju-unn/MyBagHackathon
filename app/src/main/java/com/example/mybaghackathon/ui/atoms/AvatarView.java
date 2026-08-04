package com.example.mybaghackathon.ui.atoms;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ViewAvatarBinding;

/**
 * A1 · 아바타 — 원형 이니셜 배지.
 * 크기는 사이즈 enum이 아니라 layout_width/layout_height 값(디자인 스펙 기준
 * 24/26/28/36dp)만으로 정해지므로 어떤 dp 값을 넣어도 그대로 동작함.
 *
 * 기능: 이니셜 텍스트와 배경색을 런타임에 바꿀 수 있는 원형 아바타 뷰.
 * view_avatar.xml을 inflate해서 내부 TextView를 채우고, 여러 아바타가
 * 겹쳐 표시될 때 배경과 구분되는 테두리(stroke)를 켤 수 있음.
 */
public class AvatarView extends FrameLayout {

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
