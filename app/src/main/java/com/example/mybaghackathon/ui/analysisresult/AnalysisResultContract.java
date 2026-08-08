package com.example.mybaghackathon.ui.analysisresult;

import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.RestrictedItem;

import java.util.ArrayList;

// S07 1차 결과 확인 화면의 View/Presenter 계약
public interface AnalysisResultContract {

    interface View {
        void showDestination(String value);
        void showSchedule(String value);
        void showLodging(String value);
        void showTransport(String value);
        void showInvalidDateError();
        void setConfirming(boolean confirming);
        void showConfirmError(String message);
        void navigateToRetry(long[] uploadIds, String roomName, int memberCount);
        void navigateToReview(long analysisId, String roomName, int memberCount,
                               ArrayList<RestrictedItem> restrictedItems, ArrayList<PackingItem> recommendedItems,
                               String destinationCountry, String destinationCity, String startDate, String endDate);
    }

    interface Presenter {
        void init(long analysisId, long[] uploadIds, String roomName, int memberCount,
                  ArrayList<RestrictedItem> restrictedItems, ArrayList<PackingItem> recommendedItems,
                  String destinationCountry, String destinationCity, String startDate, String endDate,
                  String accommodationName, String transportMode);

        String getDestinationDisplay();
        String getStartDate();
        String getEndDate();
        String getLodgingDisplay();
        String getTransportDisplay();

        void onDestinationEdited(String rawInput);
        boolean onScheduleEdited(String newStart, String newEnd);
        void onLodgingEdited(String value);
        void onTransportEdited(String value);
        void onRetryClicked();
        void onNextClicked();
        void onDestroy();
    }
}
