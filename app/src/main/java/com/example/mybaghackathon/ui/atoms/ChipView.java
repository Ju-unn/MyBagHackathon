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

/** A5 · Chip — Style = Brand (active) / Neutral (inactive) / Danger. */
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
