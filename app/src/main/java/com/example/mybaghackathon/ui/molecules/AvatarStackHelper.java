package com.example.mybaghackathon.ui.molecules;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;

import com.example.mybaghackathon.ui.atoms.AvatarView;

import java.util.List;

/**
 * M3 · 아바타 스택 — LinearLayout(molecule_avatar_stack.xml 참고)에 겹쳐진
 * AvatarView 자식들을 채워 넣음. 개수는 전달한 엔트리 수만큼.
 *
 * 기능: 참여자 목록(이니셜+색상)을 받아서 AvatarView를 필요한 개수만큼 코드로
 * 생성하고 겹쳐서 배치해주는 정적 헬퍼. 방 상세, 홈 화면의 여행방 카드 등에서
 * 아바타를 겹쳐 보여줄 때 사용됨.
 */
public class AvatarStackHelper {

    public static class Entry {
        public final String initial;
        @ColorInt public final int color;

        public Entry(String initial, @ColorInt int color) {
            this.initial = initial;
            this.color = color;
        }
    }

    /**
     * @param onDark true when the stack sits on a dark card (TripRoomCard
     *               Active) — the separator stroke should match that
     *               surface instead of the default white background.
     */
    public static void populate(Context context, LinearLayout container, List<Entry> entries,
                                 boolean onDark, int avatarSizeDp) {
        container.removeAllViews();
        int sizePx = dp(context, avatarSizeDp);
        int overlapPx = -dp(context, 8);
        int strokeColor = onDark ? 0xFF16181C : 0xFFFFFFFF;

        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            AvatarView av = new AvatarView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizePx, sizePx);
            if (i > 0) lp.leftMargin = overlapPx;
            av.setLayoutParams(lp);
            av.setAvatarColor(e.color);
            av.setInitial(e.initial);
            av.setStrokeEnabled(true, strokeColor);
            container.addView(av);
        }
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
