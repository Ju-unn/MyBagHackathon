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
 * A2 · Badge/DDay — Style = Brand / Neutral.
 * Brand: brand/default fill + text/on-brand. Neutral: bg/subtle fill + stone/600.
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
