package com.example.mybaghackathon.ui.atoms;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ViewIconButtonBinding;

/**
 * A8 · 아이콘 버튼 — 스타일 = Filled / Ghost. 실제 원은 36dp지만
 * QA 체크리스트 기준 터치 영역은 48dp 전체를 차지함.
 *
 * 기능: 아이콘 하나만 담는 원형 버튼. 아이콘 리소스와 채움(Filled)/
 * 고스트(Ghost) 스타일을 런타임에 바꿀 수 있음.
 */
public class IconButtonView extends FrameLayout {

    private ViewIconButtonBinding binding;

    public IconButtonView(Context context) {
        super(context);
        init(context);
    }

    public IconButtonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IconButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = ViewIconButtonBinding.inflate(LayoutInflater.from(context), this, true);
        setFilled(true);
    }

    public void setIcon(@DrawableRes int resId) {
        binding.iconButtonIcon.setImageResource(resId);
    }

    public void setFilled(boolean filled) {
        if (filled) {
            binding.iconButtonCircle.setBackgroundResource(R.drawable.oval_solid);
            binding.iconButtonCircle.getBackground().mutate().setTint(ContextCompat.getColor(getContext(), R.color.bag_brand_default));
            binding.iconButtonCircle.setElevation(dp(4));
            binding.iconButtonIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.bag_text_on_brand));
        } else {
            binding.iconButtonCircle.setBackgroundResource(R.drawable.bg_pill_neutral);
            binding.iconButtonCircle.setElevation(0);
            binding.iconButtonIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.bag_text_primary));
        }
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
