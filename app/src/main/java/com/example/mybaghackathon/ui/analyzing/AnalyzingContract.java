package com.example.mybaghackathon.ui.analyzing;

import com.example.mybaghackathon.model.AnalysisResult;

import java.util.List;

// S06 분석 중 화면의 View/Presenter 계약
public interface AnalyzingContract {

    interface View {
        void showAnalysisResult(AnalysisResult result);
        void showError(String message);
    }

    interface Presenter {
        void startAnalysis(List<Long> uploadIds);
        void onDestroy();
    }
}
