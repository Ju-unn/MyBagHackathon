package com.example.mybaghackathon.model;

/**
 * 여행 일정과 숙소 자료로 업로드한 파일의 메타데이터를 나타내는 모델입니다.
 *
 * <p>실제 이미지 데이터와 EC2 내부 저장 경로는 포함하지 않습니다. uploadType은
 * ITINERARY, ACCOMMODATION 또는 EXTRA를 사용하고, uploadStatus는 서버가 전달한
 * 업로드 및 분석 상태 문자열을 사용합니다.</p>
 */
public class TripUpload {

    private long uploadId;
    private long tripId;
    private String uploadType;
    private String originalFilename;
    private String mimeType;
    private long fileSizeBytes;
    private int sortOrder;
    private String uploadStatus;
    private String createdAt;

    public TripUpload() {
    }

    public TripUpload(
            long uploadId,
            long tripId,
            String uploadType,
            String originalFilename,
            String mimeType,
            long fileSizeBytes,
            int sortOrder,
            String uploadStatus,
            String createdAt
    ) {
        this.uploadId = uploadId;
        this.tripId = tripId;
        this.uploadType = uploadType;
        this.originalFilename = originalFilename;
        this.mimeType = mimeType;
        this.fileSizeBytes = fileSizeBytes;
        this.sortOrder = sortOrder;
        this.uploadStatus = uploadStatus;
        this.createdAt = createdAt;
    }

    public long getUploadId() {
        return uploadId;
    }

    public void setUploadId(long uploadId) {
        this.uploadId = uploadId;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public String getUploadType() {
        return uploadType;
    }

    public void setUploadType(String uploadType) {
        this.uploadType = uploadType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
