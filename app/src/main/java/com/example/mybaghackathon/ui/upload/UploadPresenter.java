package com.example.mybaghackathon.ui.upload;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.UploadRepository;
import com.example.mybaghackathon.util.ImageCompressor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// UploadContract.Presenter 구현체 — 사진 캐시 복사, 5장 제한, 압축·업로드 흐름을 담당
public class UploadPresenter implements UploadContract.Presenter {

    private static final int MAX_PHOTOS = 5;

    // 사진 종류는 사용자가 직접 고르지 않는다 — 일정표·숙소예약·항공권 등 섞인 사진을
    // 그대로 올리면 AI가 분석해서 구분한다(S07/S08 결과 화면 참고). 그래서 업로드
    // 시점엔 전부 ITINERARY로 보내는 게 의도된 동작.
    private static final String UPLOAD_TYPE_ITINERARY = "ITINERARY";

    private final UploadContract.View view;
    private final Context appContext;
    private final UploadRepository uploadRepository;
    private final List<Uri> selectedUris = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean destroyed = false;

    public UploadPresenter(UploadContract.View view, Context appContext, UploadRepository uploadRepository) {
        this.view = view;
        this.appContext = appContext;
        this.uploadRepository = uploadRepository;
    }

    @Override
    public boolean canPickMorePhotos() {
        return selectedUris.size() < MAX_PHOTOS;
    }

    @Override
    public void onPhotosPicked(List<Uri> pickedUris) {
        // 포토피커가 주는 주소(content://media/picker/...)는 이 화면 인스턴스가 없어지면
        // 못 읽게 되는 임시 권한이라, 고르자마자 우리 캐시 폴더로 복사해서 화면이
        // 다시 생성돼도(취소 왕복 등) 안전하게 다시 읽을 수 있는 주소로 바꿔둠
        List<Uri> cachedUris = new ArrayList<>();
        for (Uri uri : pickedUris) {
            Uri cached = cachePickedPhoto(uri);
            if (cached != null) cachedUris.add(cached);
        }
        addPhotos(cachedUris);
    }

    @Override
    public void onPhotosRestored(List<String> restoredPaths) {
        List<Uri> restoredUris = new ArrayList<>();
        for (String path : restoredPaths) {
            restoredUris.add(Uri.fromFile(new File(path)));
        }
        addPhotos(restoredUris);
    }

    private void addPhotos(List<Uri> uris) {
        boolean skippedSome = false;
        for (Uri uri : uris) {
            if (selectedUris.contains(uri)) continue; // 이미 고른 사진은 중복 추가하지 않음
            if (selectedUris.size() >= MAX_PHOTOS) {
                skippedSome = true;
                continue;
            }
            selectedUris.add(uri);
            view.showThumbnail(uri);
        }
        if (skippedSome) {
            view.showMaxPhotosReached();
        }
    }

    private Uri cachePickedPhoto(Uri sourceUri) {
        ContentResolver resolver = appContext.getContentResolver();
        File outFile = new File(appContext.getCacheDir(), "picked_" + System.nanoTime() + ".jpg");
        try (InputStream in = resolver.openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(outFile)) {
            if (in == null) return null;
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return Uri.fromFile(outFile);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void onPhotoRemoved(Uri uri) {
        selectedUris.remove(uri);
        view.removeThumbnail(uri);
    }

    @Override
    public void onStartAnalysisClicked() {
        if (selectedUris.isEmpty()) {
            view.showNoPhotosError();
            return;
        }

        view.setUploading(true);
        List<Uri> uris = new ArrayList<>(selectedUris);
        executor.execute(() -> {
            List<File> compressed = new ArrayList<>();
            try {
                for (Uri uri : uris) {
                    compressed.add(ImageCompressor.compress(appContext, uri));
                }

                AppResult<List<Long>> result = uploadRepository.upload(compressed, UPLOAD_TYPE_ITINERARY);
                mainHandler.post(() -> handleUploadResult(result, uris));
            } catch (IOException e) {
                mainHandler.post(() -> {
                    if (destroyed) return;
                    view.setUploading(false);
                    view.showCompressError();
                });
            } finally {
                // 업로드 성공/실패와 무관하게 압축된 임시 파일은 더 이상 필요 없으므로 캐시 폴더에서 정리
                for (File file : compressed) {
                    file.delete();
                }
            }
        });
    }

    private void handleUploadResult(AppResult<List<Long>> result, List<Uri> uris) {
        if (destroyed) return;
        if (result.isSuccess()) {
            long[] uploadIds = new long[result.getData().size()];
            for (int i = 0; i < uploadIds.length; i++) {
                uploadIds[i] = result.getData().get(i);
            }
            ArrayList<String> selectedPaths = new ArrayList<>();
            for (Uri uri : uris) {
                selectedPaths.add(uri.getPath());
            }
            view.navigateToAnalyzing(uploadIds, selectedPaths);
        } else {
            view.setUploading(false);
            view.showUploadError(result.getError().getMessage());
        }
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        executor.shutdown();
    }
}
