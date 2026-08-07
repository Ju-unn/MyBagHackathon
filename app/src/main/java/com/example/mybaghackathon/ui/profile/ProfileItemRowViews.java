package com.example.mybaghackathon.ui.profile;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mybaghackathon.R;

/** 라벨 하나로 이루어진 기본 물품 행을 만드는 헬퍼. 프로필 미리보기와 전체보기 화면이 함께 쓴다. */
final class ProfileItemRowViews {

    private ProfileItemRowViews() {
    }

    static View create(Context context, String label, View.OnClickListener onRowClick) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(context, 48)));
        row.setBackgroundResource(resolveSelectableBackground(context));

        TextView text = new TextView(context);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        text.setLayoutParams(textLp);
        text.setText(label);
        text.setTextAppearance(R.style.TextAppearance_Bag_BodyL);
        row.addView(text);

        row.setOnClickListener(onRowClick);
        return row;
    }

    private static int resolveSelectableBackground(Context context) {
        android.util.TypedValue outValue = new android.util.TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        return outValue.resourceId;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
