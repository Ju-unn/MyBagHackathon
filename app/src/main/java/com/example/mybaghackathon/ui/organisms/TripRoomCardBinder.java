package com.example.mybaghackathon.ui.organisms;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.ui.atoms.DDayBadgeView;
import com.example.mybaghackathon.ui.molecules.AvatarStackHelper;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * O4 · 여행방 카드(TripRoomCard) — pen.dev 스펙 기준 세 가지 상태(진행중/예정/
 * 지난 여행) 모두 동일하게 inflate된 organism_trip_room_card.xml에 값을 바인딩함:
 * 진행중 = bg/inverse + 데코레이션 + 아바타들 + 진행률.
 * 예정 = bg/surface + 1dp 테두리 + 뉴트럴 디데이, 아바타/진행률 없음.
 * 지난 여행 = bg/subtle, 불투명도 0.7, 디데이/아바타/진행률 없음.
 *
 * 기능: 하나의 카드 레이아웃을 상태별로(bindActive/bindUpcoming/bindPast)
 * 색상·표시 여부·텍스트를 다르게 채워주는 정적 바인더.
 */
public class TripRoomCardBinder {

    public static void bindActive(View root, String title, String ddayText,
                                   List<AvatarStackHelper.Entry> avatars, int percent) {
        Context ctx = root.getContext();
        MaterialCardView card = (MaterialCardView) root;
        card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.bag_bg_inverse));
        card.setStrokeWidth(0);
        card.setAlpha(1f);

        root.findViewById(R.id.tripCardDecoration).setVisibility(View.VISIBLE);
        root.findViewById(R.id.tripCardDecoration).getBackground().mutate()
                .setTint(0x593A7CA5); // brand @ 35% opacity

        TextView status = root.findViewById(R.id.tripCardStatus);
        status.setText(R.string.home_status_active);
        status.setTextColor(ContextCompat.getColor(ctx, R.color.bag_brand_default));

        TextView titleView = root.findViewById(R.id.tripCardTitle);
        titleView.setText(title);
        titleView.setTextColor(ContextCompat.getColor(ctx, R.color.bag_text_on_inverse));

        DDayBadgeView dday = root.findViewById(R.id.tripCardDDay);
        dday.setVisibility(View.VISIBLE);
        dday.setText(ddayText);
        dday.setBrand(true);

        root.findViewById(R.id.tripCardBottomRow).setVisibility(View.VISIBLE);
        LinearLayout stack = root.findViewById(R.id.tripCardAvatarStack);
        AvatarStackHelper.populate(ctx, stack, avatars, true, 28);

        TextView progressLabel = root.findViewById(R.id.tripCardProgressLabel);
        progressLabel.setText(ctx.getString(R.string.checklist_progress_format, percent));
        progressLabel.setTextColor(ContextCompat.getColor(ctx, R.color.bag_text_on_inverse));

        ProgressBar bar = root.findViewById(R.id.tripCardProgressBar);
        bar.setVisibility(View.VISIBLE);
        bar.setProgress(percent);

        root.findViewById(R.id.tripCardHint).setVisibility(View.GONE);
    }

    public static void bindUpcoming(View root, String title, String ddayText, String hint) {
        Context ctx = root.getContext();
        MaterialCardView card = (MaterialCardView) root;
        card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.bag_bg_surface));
        card.setStrokeColor(ContextCompat.getColor(ctx, R.color.bag_border_default));
        card.setStrokeWidth(dp(ctx, 1));
        card.setAlpha(1f);

        root.findViewById(R.id.tripCardDecoration).setVisibility(View.GONE);

        TextView status = root.findViewById(R.id.tripCardStatus);
        status.setText(R.string.home_status_upcoming);
        status.setTextColor(ContextCompat.getColor(ctx, R.color.bag_text_secondary));

        TextView titleView = root.findViewById(R.id.tripCardTitle);
        titleView.setText(title);
        titleView.setTextColor(ContextCompat.getColor(ctx, R.color.bag_text_primary));

        DDayBadgeView dday = root.findViewById(R.id.tripCardDDay);
        dday.setVisibility(View.VISIBLE);
        dday.setText(ddayText);
        dday.setBrand(false);

        root.findViewById(R.id.tripCardBottomRow).setVisibility(View.GONE);
        root.findViewById(R.id.tripCardProgressBar).setVisibility(View.GONE);

        TextView hintView = root.findViewById(R.id.tripCardHint);
        hintView.setVisibility(View.VISIBLE);
        hintView.setText(hint);
    }

    public static void bindPast(View root, String title) {
        Context ctx = root.getContext();
        MaterialCardView card = (MaterialCardView) root;
        card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.bag_bg_subtle));
        card.setStrokeWidth(0);
        card.setAlpha(0.7f);

        root.findViewById(R.id.tripCardDecoration).setVisibility(View.GONE);

        TextView status = root.findViewById(R.id.tripCardStatus);
        status.setText(R.string.home_status_past);
        status.setTextColor(ContextCompat.getColor(ctx, R.color.bag_text_secondary));

        TextView titleView = root.findViewById(R.id.tripCardTitle);
        titleView.setText(title);
        titleView.setTextColor(ContextCompat.getColor(ctx, R.color.bag_text_primary));

        root.findViewById(R.id.tripCardDDay).setVisibility(View.GONE);
        root.findViewById(R.id.tripCardBottomRow).setVisibility(View.GONE);
        root.findViewById(R.id.tripCardProgressBar).setVisibility(View.GONE);
        root.findViewById(R.id.tripCardHint).setVisibility(View.GONE);
    }

    private static int dp(Context ctx, int value) {
        return Math.round(value * ctx.getResources().getDisplayMetrics().density);
    }
}
