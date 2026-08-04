package com.example.mybaghackathon.ui.atoms;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;

/**
 * A3 · PriorityDot — a 7x7dp filled circle. Level = High / Mid / Low.
 * Usage: <com.example.mybaghackathon.ui.atoms.PriorityDotView
 *            android:layout_width="7dp" android:layout_height="7dp"
 *            app:priorityLevel="mid" />
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
