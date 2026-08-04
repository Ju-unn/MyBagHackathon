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
 * A5 · 칩(Chip) — 스타일 = 브랜드(활성) / 뉴트럴(비활성) / 데인저.
 *
 * 기능: 클릭할 때마다 활성/비활성 상태가 토글되는 라벨 칩. 우선순위 선택,
 * 지역(국내/해외) 선택 등 여러 화면에서 선택 상태 표시용으로 재사용됨.
 */
public class ChipView extends FrameLayout {

    private TextView label;
    private boolean active;

    public ChipView(Context context) {
        super(context);
        init(context, null);
    }

    public ChipView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ChipView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_chip, this, true);
        label = findViewById(R.id.chipLabel);

        String text = "";
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BagChipView);
            String t = a.getString(R.styleable.BagChipView_chipLabel);
            if (t != null) text = t;
            active = a.getBoolean(R.styleable.BagChipView_chipActive, false);
            a.recycle();
        }
        label.setText(text);
        setActive(active);
        setOnClickListener(v -> setActive(!active));
    }

    public void setLabel(String text) {
        label.setText(text);
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            label.setBackgroundResource(R.drawable.bg_pill_brand);
            label.setTextColor(ContextCompat.getColor(getContext(), R.color.bag_text_on_brand));
        } else {
            label.setBackgroundResource(R.drawable.bg_pill_neutral);
            label.setTextColor(ContextCompat.getColor(getContext(), R.color.bag_text_tertiary_safe));
        }
    }

    public boolean isActive() {
        return active;
    }
}
