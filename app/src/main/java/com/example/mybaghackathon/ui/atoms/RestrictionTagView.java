package com.example.mybaghackathon.ui.atoms;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;

/**
 * A10 · RestrictionTag — Type = CabinOnly / CheckedOnly / Prohibited.
 * Icon + text always shown together (QA §5: never convey restriction by
 * color alone). CabinOnly/CheckedOnly use a warning-tinted background but
 * stone/600 text+icon (amber text on white fails 4.5:1 contrast per §5).
 */
public class RestrictionTagView extends FrameLayout {

    public static final int CABIN_ONLY = 0;
    public static final int CHECKED_ONLY = 1;
    public static final int PROHIBITED = 2;

    private LinearLayout row;
    private ImageView icon;
    private TextView label;

    public RestrictionTagView(Context context) {
        super(context);
        init(context, null);
    }

    public RestrictionTagView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RestrictionTagView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_restriction_tag, this, true);
        row = findViewById(R.id.restrictionTagRow);
        icon = findViewById(R.id.restrictionTagIcon);
        label = findViewById(R.id.restrictionTagLabel);

        int type = PROHIBITED;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BagRestrictionTagView);
            type = a.getInt(R.styleable.BagRestrictionTagView_restrictionType, PROHIBITED);
            a.recycle();
        }
        setType(type);
    }

    public void setType(int type) {
        int textColorRes = R.color.bag_text_tertiary_safe;
        switch (type) {
            case CABIN_ONLY:
                row.setBackgroundResource(R.drawable.bg_restriction_warning);
                icon.setImageResource(R.drawable.ic_plane);
                label.setText(R.string.restriction_cabin_only);
                break;
            case CHECKED_ONLY:
                row.setBackgroundResource(R.drawable.bg_restriction_warning);
                icon.setImageResource(R.drawable.ic_luggage);
                label.setText(R.string.restriction_checked_only);
                break;
            default:
                row.setBackgroundResource(R.drawable.bg_restriction_danger);
                icon.setImageResource(R.drawable.ic_ban);
                label.setText(R.string.restriction_prohibited);
                textColorRes = R.color.bag_status_danger;
                break;
        }
        int color = ContextCompat.getColor(getContext(), textColorRes);
        label.setTextColor(color);
        icon.setColorFilter(color);
    }
}
