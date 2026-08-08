package com.example.mybaghackathon.ui.analyzing;

import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.AnalysisRepository;
import com.example.mybaghackathon.model.AnalysisResult;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// AnalyzingContract.Presenter 구현체 — analysisRepository.analyze() 호출을
// 백그라운드 스레드에서 실행하고 결과를 메인 스레드의 View로 전달한다
public class AnalyzingPresenter implements AnalyzingContract.Presenter {

    private final AnalyzingContract.View view;
    private final AnalysisRepository analysisRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean destroyed = false;

    public AnalyzingPresenter(AnalyzingContract.View view, AnalysisRepository analysisRepository) {
        this.view = view;
        this.analysisRepository = analysisRepository;
    }

    @Override
    public void startAnalysis(List<Long> uploadIds) {
        executor.execute(() -> {
            AppResult<AnalysisResult> result = analysisRepository.analyze(uploadIds);
            mainHandler.post(() -> {
                if (destroyed) return;
                if (result.isSuccess()) {
                    view.showAnalysisResult(result.getData());
                } else {
                    view.showError(result.getError().getMessage());
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        executor.shutdown();
    }
}
