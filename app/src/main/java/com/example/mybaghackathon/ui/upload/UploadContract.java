package com.example.mybaghackathon.ui.upload;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

// S05 일정 사진 업로드 화면의 View/Presenter 계약
public interface UploadContract {

    interface View {
        void showThumbnail(Uri uri);
        void removeThumbnail(Uri uri);
        void showMaxPhotosReached();
        void showNoPhotosError();
        void showCompressError();
        void showUploadError(String message);
        void setUploading(boolean uploading);
        void navigateToAnalyzing(long[] uploadIds, ArrayList<String> selectedPaths);
    }

    interface Presenter {
        boolean canPickMorePhotos();
        void onPhotosPicked(List<Uri> pickedUris);
        void onPhotosRestored(List<String> restoredPaths);
        void onPhotoRemoved(Uri uri);
        void onStartAnalysisClicked();
        void onDestroy();
    }
}
