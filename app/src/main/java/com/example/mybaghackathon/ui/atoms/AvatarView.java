package com.example.mybaghackathon.ui.atoms;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;

/**
 * A1 · Avatar — circular initial badge.
 * Sizes are driven entirely by layout_width/layout_height (24/26/28/36dp
 * per the design spec) rather than a size enum, so any dp works.
 */
public class AvatarView extends FrameLayout {

    private TextView initialText;
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
        LayoutInflater.from(context).inflate(R.layout.view_avatar, this, true);
        initialText = findViewById(R.id.avatarInitialText);
        color = ContextCompat.getColor(context, R.color.bag_avatar_2);
        strokeEnabled = false;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BagAvatarView);
            String initial = a.getString(R.styleable.BagAvatarView_avatarInitial);
            color = a.getColor(R.styleable.BagAvatarView_avatarColor, color);
            strokeEnabled = a.getBoolean(R.styleable.BagAvatarView_avatarStroke, false);
            a.recycle();
            if (initial != null) initialText.setText(initial);
        }
        applyBackground();
    }

    public void setInitial(String initial) {
        initialText.setText(initial);
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
            initialText.setBackground(ring);
        }
    }

    private void applyBackground() {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(color);
        if (strokeEnabled) {
            LayerDrawable layered = new LayerDrawable(new android.graphics.drawable.Drawable[]{circle});
            initialText.setBackground(layered);
        } else {
            initialText.setBackground(circle);
        }
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
