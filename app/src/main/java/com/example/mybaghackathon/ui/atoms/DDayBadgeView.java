package com.example.mybaghackathon.ui.atoms;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;

/**
 * A2 · 배지/디데이 — 스타일 = 브랜드 / 뉴트럴.
 * 브랜드: brand/default 배경 + text/on-brand 텍스트.
 * 뉴트럴: bg/subtle 배경 + stone/600 텍스트.
 *
 * 기능: D-day 텍스트를 표시하는 배지 뷰. 여행방 카드가 진행중(Active)인지
 * 예정(Upcoming)인지에 따라 브랜드/뉴트럴 스타일을 전환함.
 */
public class DDayBadgeView extends FrameLayout {

    private TextView label;

    public DDayBadgeView(Context context) {
        super(context);
        init(context, null);
    }

    public DDayBadgeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DDayBadgeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_dday_badge, this, true);
        label = findViewById(R.id.ddayLabel);

        boolean brand = true;
        String text = "D-0";
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BagDDayBadgeView);
            String t = a.getString(R.styleable.BagDDayBadgeView_ddayText);
            if (t != null) text = t;
            brand = a.getBoolean(R.styleable.BagDDayBadgeView_ddayBrand, true);
            a.recycle();
        }
        label.setText(text);
        setBrand(brand);
    }

    public void setText(String text) {
        label.setText(text);
    }

    public void setBrand(boolean brand) {
        if (brand) {
            label.setBackgroundResource(R.drawable.bg_pill_brand);
            label.setTextColor(ContextCompat.getColor(getContext(), R.color.bag_text_on_brand));
        } else {
            label.setBackgroundResource(R.drawable.bg_pill_neutral);
            label.setTextColor(ContextCompat.getColor(getContext(), R.color.bag_text_tertiary_safe));
        }
    }
}
