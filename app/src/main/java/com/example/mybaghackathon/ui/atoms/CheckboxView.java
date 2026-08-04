package com.example.mybaghackathon.ui.atoms;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.example.mybaghackathon.R;

/**
 * A4 · Checkbox — State = Unchecked / Checked / Excluded.
 * Visual box is 22x22dp but the whole view claims a 48x48dp touch target
 * (QA checklist §5: touch targets need a transparent hit-area).
 */
public class CheckboxView extends FrameLayout {

    public static final int UNCHECKED = 0;
    public static final int CHECKED = 1;
    public static final int EXCLUDED = 2;

    private FrameLayout box;
    private ImageView icon;
    private int state = UNCHECKED;
    private OnCheckChangeListener listener;

    public interface OnCheckChangeListener {
        void onCheckChanged(int newState);
    }

    public CheckboxView(Context context) {
        super(context);
        init(context, null);
    }

    public CheckboxView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CheckboxView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_checkbox, this, true);
        box = findViewById(R.id.checkboxBox);
        icon = findViewById(R.id.checkboxIcon);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BagCheckboxView);
            state = a.getInt(R.styleable.BagCheckboxView_checkState, UNCHECKED);
            a.recycle();
        }
        render();

        setOnClickListener(v -> {
            setState(state == CHECKED ? UNCHECKED : CHECKED);
            if (listener != null) listener.onCheckChanged(state);
        });
    }

    public void setOnCheckChangeListener(OnCheckChangeListener l) {
        this.listener = l;
    }

    public int getState() {
        return state;
    }

    public void setState(int newState) {
        this.state = newState;
        render();
    }

    private void render() {
        switch (state) {
            case CHECKED:
                box.setBackgroundResource(R.drawable.bg_checkbox_checked);
                icon.setImageResource(R.drawable.ic_check);
                icon.setVisibility(View.VISIBLE);
                break;
            case EXCLUDED:
                box.setBackgroundResource(R.drawable.bg_checkbox_excluded);
                icon.setImageResource(R.drawable.ic_close_small);
                icon.setVisibility(View.VISIBLE);
                break;
            default:
                box.setBackgroundResource(R.drawable.bg_checkbox_unchecked);
                icon.setVisibility(View.INVISIBLE);
                break;
        }
    }
}
