package com.example.mybaghackathon.ui.organisms;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * S03/S14 여행방 카드를 왼쪽으로 스와이프하면 뒤에 있는 삭제 버튼이 드러나게 하는
 * 제스처 헬퍼. RecyclerView 어댑터가 {@link Tracker} 하나를 만들어 두고 각 행을
 * bind할 때마다 {@link #attach}를 호출하면, 한 번에 하나의 행만 열려 있도록
 * 관리해준다. 열림/닫힘은 순수 시각 효과(translationX)일 뿐이라 리바인드되면
 * 항상 닫힌 상태로 되돌아간다(reset) — 화면에 몇 개 안 뜨는 여행방 리스트라
 * 리사이클 중 열림 상태를 유지해야 할 필요는 없다.
 */
public final class SwipeRevealHelper {

    /** 어댑터가 하나 들고 있는 "지금 열려 있는 행" 참조. */
    public static class Tracker {
        private View openForeground;
    }

    private SwipeRevealHelper() {
    }

    public static void reset(View foreground) {
        foreground.setTranslationX(0f);
    }

    public static boolean isOpen(Tracker tracker, View foreground) {
        return tracker.openForeground == foreground;
    }

    public static void closeOpenRow(Tracker tracker) {
        if (tracker.openForeground != null) {
            tracker.openForeground.animate().translationX(0f).setDuration(160L).start();
            tracker.openForeground = null;
        }
    }

    public static void attach(View foreground, float revealWidthPx, Tracker tracker) {
        int touchSlop = ViewConfiguration.get(foreground.getContext()).getScaledTouchSlop();
        foreground.setOnTouchListener(new View.OnTouchListener() {
            private float downRawX;
            private float downRawY;
            private float startTranslationX;
            private boolean swiping;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downRawX = event.getRawX();
                        downRawY = event.getRawY();
                        startTranslationX = view.getTranslationX();
                        swiping = false;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        // 화면 절대좌표(rawX/rawY) 기준으로 delta를 구해야 한다 — event.getX()는
                        // 뷰의 현재 translationX만큼 이미 보정된 좌표라 매 프레임 setTranslationX와
                        // 서로 되먹임(feedback)되어 느리게 스와이프할 때 값이 떨렸다.
                        float deltaX = event.getRawX() - downRawX;
                        float deltaY = event.getRawY() - downRawY;
                        if (!swiping && Math.abs(deltaX) > touchSlop
                                && Math.abs(deltaX) > Math.abs(deltaY)) {
                            swiping = true;
                            view.getParent().requestDisallowInterceptTouchEvent(true);
                            if (tracker.openForeground != null && tracker.openForeground != view) {
                                closeOpenRow(tracker);
                            }
                        }
                        if (swiping) {
                            view.setTranslationX(clamp(startTranslationX + deltaX, -revealWidthPx, 0f));
                            return true;
                        }
                        return false;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        if (!swiping) {
                            return false;
                        }
                        boolean openNow = view.getTranslationX() <= -revealWidthPx / 2f;
                        view.animate().translationX(openNow ? -revealWidthPx : 0f).setDuration(160L).start();
                        tracker.openForeground = openNow ? view : null;
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
