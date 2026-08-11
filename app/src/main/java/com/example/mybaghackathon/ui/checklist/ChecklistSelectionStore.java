package com.example.mybaghackathon.ui.checklist;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Persists the exact recommendations checked while creating a trip. */
public final class ChecklistSelectionStore {

    private static final String PREFS_NAME = "checklist_creation_selections";
    private static final String KEY_PREFIX = "trip_";

    private ChecklistSelectionStore() {
    }

    public static void save(Context context, long tripId, List<String> selectedNames) {
        if (context == null || tripId <= 0L) {
            return;
        }
        Set<String> normalizedNames = new HashSet<>();
        if (selectedNames != null) {
            for (String name : selectedNames) {
                String normalized = ChecklistSelectionFilter.normalize(name);
                if (!normalized.isEmpty()) {
                    normalizedNames.add(normalized);
                }
            }
        }
        preferences(context).edit()
                .putStringSet(KEY_PREFIX + tripId, normalizedNames)
                .apply();
    }

    /** Returns null only when this trip was not created on this device. */
    public static Set<String> load(Context context, long tripId) {
        if (context == null || tripId <= 0L) {
            return null;
        }
        SharedPreferences preferences = preferences(context);
        String key = KEY_PREFIX + tripId;
        if (!preferences.contains(key)) {
            return null;
        }
        Set<String> stored = preferences.getStringSet(key, new HashSet<>());
        return stored == null ? new HashSet<>() : new HashSet<>(stored);
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(
                PREFS_NAME, Context.MODE_PRIVATE);
    }
}
