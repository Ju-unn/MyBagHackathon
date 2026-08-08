package com.example.mybaghackathon.ui.checklist;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.AppContainer;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.common.AppResult;
import com.example.mybaghackathon.data.repository.PackingRepository;
import com.example.mybaghackathon.data.repository.TripRepository;
import com.example.mybaghackathon.databinding.ActivityChecklistBinding;
import com.example.mybaghackathon.databinding.ActivityChecklistSoloBinding;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.TripMember;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** S11~S13 · 공용/내 목록/분담 현황의 데이터와 화면 전환을 관리한다. */
public class ChecklistActivity extends AppCompatActivity implements ChecklistHost {

    public static final String EXTRA_TRIP_ID = "trip_id";
    public static final String EXTRA_MEMBER_COUNT = "member_count";
    public static final String EXTRA_IS_HOST = "is_host";

    private ActivityChecklistBinding multiBinding;
    private ActivityChecklistSoloBinding soloBinding;
    private PackingRepository packingRepository;
    private TripRepository tripRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<PackingItem> items = new ArrayList<>();
    private final List<TripMember> members = new ArrayList<>();

    private long tripId;
    private long currentUserId;
    private int memberCount;
    private boolean isHost;
    private boolean soloMode;
    private boolean loaded;
    private boolean loading;
    private String tripName = "여행 체크리스트";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readArguments();
        initializeRepositories();
        inflateModeLayout(savedInstanceState);
        refreshChecklist();
    }

    private void readArguments() {
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, -1L);
        memberCount = Math.max(1, getIntent().getIntExtra(EXTRA_MEMBER_COUNT, 1));
        isHost = getIntent().getBooleanExtra(EXTRA_IS_HOST, false);
        soloMode = memberCount <= 1;
    }

    private void initializeRepositories() {
        AppContainer container = ((MyBagApplication) getApplication()).getAppContainer();
        packingRepository = container.packingRepository;
        tripRepository = container.tripRepository;
        currentUserId = container.tokenStorage.getUserId();
    }

    private void inflateModeLayout(Bundle savedInstanceState) {
        if (soloMode) {
            soloBinding = ActivityChecklistSoloBinding.inflate(getLayoutInflater());
            setContentView(soloBinding.getRoot());
            EdgeToEdgeUtil.applySystemBarPadding(this, soloBinding.getRoot());
            if (savedInstanceState == null) {
                showFragment(new ChecklistMineFragment(), R.id.checklistSoloFragmentContainer);
            }
            return;
        }

        multiBinding = ActivityChecklistBinding.inflate(getLayoutInflater());
        setContentView(multiBinding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, multiBinding.getRoot());

        if (savedInstanceState == null) {
            showFragment(new ChecklistCommonFragment(), R.id.checklistFragmentContainer);
        }
        multiBinding.checklistTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment;
                switch (tab.getPosition()) {
                    case 1:
                        fragment = new ChecklistMineFragment();
                        break;
                    case 2:
                        fragment = new ChecklistAssignmentFragment();
                        break;
                    default:
                        fragment = new ChecklistCommonFragment();
                        break;
                }
                showFragment(fragment, R.id.checklistFragmentContainer);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void showFragment(Fragment fragment, int containerId) {
        Bundle arguments = new Bundle();
        arguments.putLong(EXTRA_TRIP_ID, tripId);
        arguments.putInt(EXTRA_MEMBER_COUNT, memberCount);
        arguments.putBoolean(EXTRA_IS_HOST, isHost);
        fragment.setArguments(arguments);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, fragment)
                .commit();
    }

    @Override
    public void refreshChecklist() {
        if (loading || tripId <= 0L) {
            if (tripId <= 0L) {
                Toast.makeText(this, "여행방 정보가 없어 체크리스트를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        loading = true;
        executor.execute(() -> {
            AppResult<Trip> tripResult = tripRepository.getTripDetail(tripId);
            AppResult<List<PackingItem>> itemResult = packingRepository.listItems(tripId, null);
            runOnUiThread(() -> handleLoadResult(tripResult, itemResult));
        });
    }

    private void handleLoadResult(
            AppResult<Trip> tripResult,
            AppResult<List<PackingItem>> itemResult
    ) {
        loading = false;
        if (isFinishing() || isDestroyed()) {
            return;
        }

        if (tripResult.isSuccess() && tripResult.getData() != null) {
            Trip trip = tripResult.getData();
            if (hasText(trip.getTripName())) {
                tripName = trip.getTripName();
            }
            members.clear();
            members.addAll(trip.getMembers());
            int actualMemberCount = Math.max(1, members.size());
            boolean actualSoloMode = actualMemberCount <= 1;
            if (actualSoloMode != soloMode) {
                getIntent().putExtra(EXTRA_MEMBER_COUNT, actualMemberCount);
                recreate();
                return;
            }
            memberCount = actualMemberCount;
            if (currentUserId > 0) {
                isHost = trip.getOwnerUserId() == currentUserId;
            }
        }

        if (!itemResult.isSuccess()) {
            showError(itemResult);
            return;
        }

        items.clear();
        if (itemResult.getData() != null) {
            for (PackingItem item : itemResult.getData()) {
                if (!"DELETED".equalsIgnoreCase(item.getItemStatus())
                        && !"EXCLUDED".equalsIgnoreCase(item.getItemStatus())) {
                    items.add(item);
                }
            }
        }
        loaded = true;
        updateHeader();
        notifyVisibleFragment();
    }

    private void updateHeader() {
        int completed = 0;
        for (PackingItem item : items) {
            if (item.isCompleted()) {
                completed++;
            }
        }
        int progress = items.isEmpty() ? 0 : Math.round(completed * 100f / items.size());
        String summary = completed + "/" + items.size() + " 완료";

        if (soloMode && soloBinding != null) {
            soloBinding.checklistSoloTripName.setText(tripName);
            soloBinding.checklistSoloProgressLabel.setText(summary);
            soloBinding.checklistSoloProgressBar.setProgress(progress);
        } else if (multiBinding != null) {
            multiBinding.checklistTripName.setText(tripName);
            multiBinding.checklistProgressLabel.setText(summary);
            multiBinding.checklistProgressBar.setProgress(progress);
            multiBinding.checklistCollaborationNote.setText(memberCount + "명이 함께 준비 중이에요");
        }
    }

    private void notifyVisibleFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(
                soloMode ? R.id.checklistSoloFragmentContainer : R.id.checklistFragmentContainer);
        if (fragment instanceof ChecklistDataConsumer) {
            ((ChecklistDataConsumer) fragment).renderChecklist();
        }
    }

    @Override
    public void addChecklistItem(String name, int priorityLevel, String scope) {
        runRepositoryAction(() -> packingRepository.addItem(
                tripId, name, null, priorityCode(priorityLevel), scope));
    }

    @Override
    public void updateChecklistItem(PackingItem item, String name, int priorityLevel) {
        runRepositoryAction(() -> packingRepository.updateItem(
                item.getPackingItemId(), name, null, priorityCode(priorityLevel), null));
    }

    @Override
    public void toggleChecklistItem(PackingItem item) {
        runRepositoryAction(() -> packingRepository.toggleCheck(item.getPackingItemId()));
    }

    @Override
    public void assignChecklistItem(PackingItem item, Long userId) {
        runRepositoryAction(() -> packingRepository.assign(item.getPackingItemId(), userId));
    }

    private void runRepositoryAction(RepositoryAction action) {
        if (tripId <= 0L) {
            Toast.makeText(this, "여행방 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            AppResult<?> result = action.run();
            runOnUiThread(() -> {
                if (!result.isSuccess()) {
                    showError(result);
                    return;
                }
                refreshChecklist();
            });
        });
    }

    @Override
    public void deleteChecklistItemWithUndo(PackingItem item) {
        int originalIndex = items.indexOf(item);
        if (originalIndex < 0) {
            return;
        }

        items.remove(originalIndex);
        updateHeader();
        notifyVisibleFragment();

        boolean[] restored = {false};
        Snackbar snackbar = Snackbar.make(contentRoot(), "항목을 삭제했습니다.", Snackbar.LENGTH_LONG);
        snackbar.setAction("실행 취소", v -> {
            restored[0] = true;
            items.add(Math.min(originalIndex, items.size()), item);
            updateHeader();
            notifyVisibleFragment();
        });
        snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (!restored[0] && event != DISMISS_EVENT_ACTION
                        && !isFinishing() && !isDestroyed() && !executor.isShutdown()) {
                    executor.execute(() -> {
                        AppResult<Void> result = packingRepository.deleteItem(item.getPackingItemId());
                        runOnUiThread(() -> {
                            if (!result.isSuccess()) {
                                showError(result);
                            }
                            refreshChecklist();
                        });
                    });
                }
            }
        });
        snackbar.show();
    }

    private View contentRoot() {
        return soloMode ? soloBinding.getRoot() : multiBinding.getRoot();
    }

    private String priorityCode(int priorityLevel) {
        switch (priorityLevel) {
            case 1:
                return "RECOMMENDED";
            case 2:
                return "OPTIONAL";
            default:
                return "REQUIRED";
        }
    }

    private void showError(AppResult<?> result) {
        String message = result.getError() == null
                ? "요청을 처리하지 못했습니다."
                : result.getError().getMessage();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public List<PackingItem> getChecklistItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    @Override
    public List<TripMember> getTripMembers() {
        return Collections.unmodifiableList(new ArrayList<>(members));
    }

    @Override
    public long getCurrentUserId() {
        return currentUserId;
    }

    @Override
    public int getTripMemberCount() {
        return memberCount;
    }

    @Override
    public boolean isChecklistLoaded() {
        return loaded;
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        multiBinding = null;
        soloBinding = null;
        super.onDestroy();
    }

    private interface RepositoryAction {
        AppResult<?> run();
    }
}
