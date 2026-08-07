package com.example.mybaghackathon.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

// 업로드 전 이미지 압축을 담당하는 유틸. 긴 변 1,600px 이하로 리사이즈 후 JPEG로 저장한다 (README 비용 절감 기준)
public final class ImageCompressor {

    private static final int MAX_DIMENSION = 1600;
    private static final int JPEG_QUALITY = 88;

    private ImageCompressor() {
    }

    /** uri의 이미지를 리사이즈·압축해 앱 캐시 디렉터리에 JPEG로 저장하고, 그 파일을 반환한다. */
    public static File compress(Context context, Uri uri) throws IOException {
        Bitmap original = decodeBitmap(context, uri);
        Bitmap scaled = scaleDown(original);

        File output = File.createTempFile("upload_", ".jpg", context.getCacheDir());
        try (FileOutputStream out = new FileOutputStream(output)) {
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
        }

        if (scaled != original) {
            original.recycle();
        }
        scaled.recycle();

        return output;
    }

    // 원본을 통째로 메모리에 올리지 않도록, 크기만 먼저 읽고(inJustDecodeBounds)
    // 필요한 배율(inSampleSize)만큼 축소된 상태로 바로 디코드한다
    private static Bitmap decodeBitmap(Context context, Uri uri) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(input, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw new IOException("이미지 크기를 읽을 수 없습니다: " + uri);
        }

        BitmapFactory.Options decode = new BitmapFactory.Options();
        decode.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight);

        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, decode);
            if (bitmap == null) {
                throw new IOException("이미지를 디코딩할 수 없습니다: " + uri);
            }
            return bitmap;
        }
    }

    // longSide를 MAX_DIMENSION 이하로 만드는 가장 작은 2의 거듭제곱 배율을 찾는다
    private static int calculateInSampleSize(int width, int height) {
        int longSide = Math.max(width, height);
        int inSampleSize = 1;
        while (longSide / (inSampleSize * 2) >= MAX_DIMENSION) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    private static Bitmap scaleDown(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();
        int longSide = Math.max(width, height);
        if (longSide <= MAX_DIMENSION) {
            return original;
        }

        float scale = (float) MAX_DIMENSION / longSide;
        int newWidth = Math.max(1, Math.round(width * scale));
        int newHeight = Math.max(1, Math.round(height * scale));
        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }
}