package com.example.mybaghackathon.ui.molecules;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.ui.atoms.AvatarView;

import java.util.List;

/**
 * M3 · 아바타 스택 — LinearLayout(molecule_avatar_stack.xml 참고)에 겹쳐진
 * AvatarView 자식들을 채워 넣음. 개수는 전달한 엔트리 수만큼.
 *
 * 기능: 참여자 목록(이니셜+색상)을 받아서 AvatarView를 필요한 개수만큼 코드로
 * 생성하고 겹쳐서 배치해주는 정적 헬퍼. 방 상세, 홈 화면의 여행방 카드 등에서
 * 아바타를 겹쳐 보여줄 때 사용됨. 겹치는 아바타끼리 구분되도록 항상 흰 테두리로
 * 감싼다(pen.dev 디자인 스펙).
 */
public class AvatarStackHelper {

    public static class Entry {
        public final String initial;
        @ColorInt public final int color;
        public final String imageUrl;

        public Entry(String initial, @ColorInt int color, String imageUrl) {
            this.initial = initial;
            this.color = color;
            this.imageUrl = imageUrl;
        }
    }

    public static void populate(Context context, LinearLayout container, List<Entry> entries,
                                 int avatarSizeDp) {
        container.removeAllViews();
        int sizePx = dp(context, avatarSizeDp);
        int overlapPx = -dp(context, 8);
        int strokeColor = ContextCompat.getColor(context, R.color.white_primitive);

        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            AvatarView av = new AvatarView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizePx, sizePx);
            if (i > 0) lp.leftMargin = overlapPx;
            av.setLayoutParams(lp);
            av.setAvatarColor(e.color);
            av.setInitial(e.initial);
            av.setStrokeEnabled(true, strokeColor);
            av.setImageUrl(e.imageUrl);
            container.addView(av);
        }
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
