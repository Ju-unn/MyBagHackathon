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
}
