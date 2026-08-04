package com.example.mybaghackathon.ui;

import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * targetSdk 36에서는 EdgeToEdge가 앱 설정과 무관하게 강제 적용되어, 인셋 패딩을
 * 직접 넣어주지 않으면 화면 하단 CTA/상단 TopBar가 시스템 바에 가려 잘린다.
 */
public final class EdgeToEdgeUtil {

    private EdgeToEdgeUtil() { }

    public static void applySystemBarPadding(ComponentActivity activity, View root) {
        EdgeToEdge.enable(activity);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * root 하단에 바텀 네비게이션 등 화면에 붙는 뷰가 있을 때 사용.
     * root는 top/left/right만 인셋 패딩을 받고, 하단 인셋은 bottomView에 패딩으로 들어가
     * root와 bottomView 사이에 빈 여백이 생기지 않는다.
     */
    public static void applySystemBarPadding(ComponentActivity activity, View root, View bottomView) {
        EdgeToEdge.enable(activity);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            bottomView.setPadding(bottomView.getPaddingLeft(), bottomView.getPaddingTop(),
                    bottomView.getPaddingRight(), systemBars.bottom);
            return insets;
        });
    }
}
