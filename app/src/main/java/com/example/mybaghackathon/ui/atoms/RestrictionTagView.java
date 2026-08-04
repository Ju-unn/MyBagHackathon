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
 * A10 · 제한 태그(RestrictionTag) — 타입 = 기내용만 / 위탁용만 / 반입금지.
 * 아이콘과 텍스트를 항상 함께 표시함(QA §5: 색상만으로 제한 사항을 전달하지
 * 말 것). 기내용만/위탁용만은 경고 톤 배경을 쓰지만 텍스트+아이콘은
 * stone/600을 사용함(흰 배경 위 amber 텍스트는 §5의 4.5:1 대비 기준을
 * 통과하지 못하기 때문).
 *
 * 기능: 짐 항목이 기내 반입만 가능한지, 위탁만 가능한지, 아예 반입 금지인지를
 * 아이콘+텍스트로 함께 표시해주는 태그 뷰.
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
