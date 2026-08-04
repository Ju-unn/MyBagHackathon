package com.example.mybaghackathon.ui.atoms;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;

/**
 * A3 · 우선순위 점(PriorityDot) — 7x7dp 크기의 채워진 원. 레벨 = 높음/중간/낮음.
 * 사용 예: <com.example.mybaghackathon.ui.atoms.PriorityDotView
 *            android:layout_width="7dp" android:layout_height="7dp"
 *            app:priorityLevel="mid" />
 *
 * 기능: 우선순위 레벨(0/1/2)에 따라 색만 바뀌는 아주 작은 점 뷰.
 * 체크리스트 섹션 헤더에서 우선순위를 색으로 구분하는 데 사용됨.
 */
public class PriorityDotView extends View {

    public PriorityDotView(Context context) {
        super(context);
        init(context, null);
    }

    public PriorityDotView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PriorityDotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        setBackgroundResource(R.drawable.oval_solid);
        int level = 0;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BagPriorityDotView);
            level = a.getInt(R.styleable.BagPriorityDotView_priorityLevel, 0);
            a.recycle();
        }
        setLevel(level);
    }

    public void setLevel(int level) {
        int colorRes;
        switch (level) {
            case 1: colorRes = R.color.bag_priority_mid; break;
            case 2: colorRes = R.color.bag_priority_low; break;
            default: colorRes = R.color.bag_priority_high; break;
        }
        getBackground().mutate().setTint(ContextCompat.getColor(getContext(), colorRes));
    }
}
