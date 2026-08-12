package com.example.mybaghackathon.ui.checklist;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.AppContainer;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.databinding.ActivityChecklistBinding;
import com.example.mybaghackathon.databinding.ActivityChecklistSoloBinding;
import com.example.mybaghackathon.model.PackingItem;
import com.example.mybaghackathon.model.TripMember;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** S11~S13 체크리스트 View. 화면 전환과 표시, 사용자 입력 전달만 담당한다. */
public class ChecklistActivity extends AppCompatActivity
        implements ChecklistContract.View, ChecklistHost {

    public static final String EXTRA_TRIP_ID = "trip_id";
    public static final String EXTRA_MEMBER_COUNT = "member_count";
    public static final String EXTRA_IS_HOST = "is_host";

    private ActivityChecklistBinding multiBinding;
    private ActivityChecklistSoloBinding soloBinding;
    private ChecklistContract.Presenter presenter;
    private final List<PackingItem> items = new ArrayList<>();
    private final List<TripMember> members = new ArrayList<>();

    private long tripId;
    private long currentUserId;
    private int memberCount;
    private boolean isHost;
    private boolean soloMode;
    private boolean loaded;
    private boolean resumedOnce;
    private String tripName = "여행 체크리스트";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readArguments();
        initializePresenter();
        inflateModeLayout(savedInstanceState);
        presenter.loadChecklist(tripId, isHost);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resumedOnce && presenter != null) {
            presenter.refreshChecklist();
        }
        resumedOnce = true;
    }

    private void readArguments() {
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, -1L);
        memberCount = Math.max(1, getIntent().getIntExtra(EXTRA_MEMBER_COUNT, 1));
        isHost = getIntent().getBooleanExtra(EXTRA_IS_HOST, false);
        soloMode = memberCount <= 1;
    }

    private void initializePresenter() {
        AppContainer container = ((MyBagApplication) getApplication()).getAppContainer();
        currentUserId = container.tokenStorage.getUserId();
        presenter = new ChecklistPresenter(
                this,
                container.tripRepository,
                container.packingRepository,
                currentUserId
        );
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
    public void showChecklist(
            String tripName,
            List<PackingItem> items,
            List<TripMember> members,
            int memberCount,
            boolean isHost
    ) {
        if (!canUpdateUi()) {
            return;
        }

        int safeMemberCount = Math.max(1, memberCount);
        boolean actualSoloMode = safeMemberCount <= 1;
        if (actualSoloMode != soloMode) {
            getIntent().putExtra(EXTRA_MEMBER_COUNT, safeMemberCount);
            getIntent().putExtra(EXTRA_IS_HOST, isHost);
            recreate();
            return;
        }

        this.tripName = hasText(tripName) ? tripName : "여행 체크리스트";
        this.memberCount = safeMemberCount;
        this.isHost = isHost;
        this.items.clear();
        this.items.addAll(items == null ? Collections.emptyList() : items);
        this.members.clear();
        this.members.addAll(members == null ? Collections.emptyList() : members);
        loaded = true;

        updateHeader();
        notifyVisibleFragment();
    }

    @Override
    public void showError(String message) {
        if (canUpdateUi()) {
            Snackbar.make(contentRoot(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showRetryableError(String message) {
        if (canUpdateUi()) {
            Snackbar.make(contentRoot(), message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_retry, v -> presenter.refreshChecklist())
                    .show();
        }
    }

    @Override
    public void showLoading(boolean loading) {
        if (!canUpdateUi()) {
            return;
        }
        if (soloMode && soloBinding != null) {
            soloBinding.checklistSoloLoadingOverlay.setVisibility(
                    loading ? View.VISIBLE : View.GONE);
        } else if (multiBinding != null) {
            multiBinding.checklistLoadingOverlay.setVisibility(
                    loading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void showDeleteUndo(PackingItem item) {
        if (!canUpdateUi()) {
            return;
        }
        boolean[] restored = {false};
        Snackbar snackbar = Snackbar.make(contentRoot(), "항목을 삭제했습니다.", Snackbar.LENGTH_LONG);
        snackbar.setAction("실행 취소", v -> {
            restored[0] = true;
            presenter.undoDelete(item);
        });
        snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (!restored[0] && event != DISMISS_EVENT_ACTION && presenter != null) {
                    presenter.confirmDelete(item);
                }
            }
        });
        snackbar.show();
    }

    private void updateHeader() {
        // 1인방은 화면에 보이는 "내 목록"(선택 AI + 기본/개인 물품, 이름 병합) 기준,
        // 다인방은 공용 물품(item_group_id 단위) 기준으로 진행률을 센다.
        ChecklistProgressCalculator.Progress result = soloMode
                ? ChecklistProgressCalculator.calculateForMine(items, currentUserId)
                : ChecklistProgressCalculator.calculate(items);
        int progress = result.percent();
        String summary = result.completed + "/" + result.total + " 완료";

        if (soloMode && soloBinding != null) {
            soloBinding.checklistSoloTripName.setText(tripName);
            soloBinding.checklistSoloProgressLabel.setText(summary);
            soloBinding.checklistSoloProgressBar.setProgress(progress);
        } else if (multiBinding != null) {
            multiBinding.checklistTripName.setText(tripName);
            multiBinding.checklistProgressLabel.setText(summary);
            multiBinding.checklistProgressBar.setProgress(progress);
        }
    }

    private void notifyVisibleFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(
                soloMode ? R.id.checklistSoloFragmentContainer : R.id.checklistFragmentContainer);
        if (fragment instanceof ChecklistDataConsumer) {
            ((ChecklistDataConsumer) fragment).renderChecklist();
        }
    }

    private View contentRoot() {
        return soloMode ? soloBinding.getRoot() : multiBinding.getRoot();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean canUpdateUi() {
        return !isFinishing() && !isDestroyed()
                && (soloBinding != null || multiBinding != null);
    }

    @Override
    public void refreshChecklist() {
        presenter.refreshChecklist();
    }

    @Override
    public void addChecklistItem(String name, int priorityLevel, String scope) {
        presenter.addItem(name, priorityLevel, scope);
    }

    @Override
    public void updateChecklistItem(PackingItem item, String name, int priorityLevel) {
        presenter.updateItem(item, name, priorityLevel);
    }

    @Override
    public void toggleChecklistItem(PackingItem item) {
        presenter.toggleItem(item);
    }

    @Override
    public void assignChecklistItem(PackingItem item, Long userId) {
        presenter.assignItem(item, userId);
    }

    @Override
    public void assignChecklistItems(PackingItem item, List<Long> userIds) {
        presenter.assignItems(item, userIds);
    }

    @Override
    public void reassignGroupedItem(List<PackingItem> groupItems, List<Long> userIds) {
        presenter.reassignGroup(groupItems, userIds);
    }

    @Override
    public void deleteChecklistItemWithUndo(PackingItem item) {
        presenter.requestDelete(item);
    }

    @Override
    public void deleteChecklistItem(PackingItem item) {
        presenter.deleteItem(item);
    }

    @Override
    public void deleteChecklistItemGroup(List<PackingItem> group) {
        presenter.deleteGroup(group);
    }

    @Override
    public void removeMergedItem(PackingItem personalItem, PackingItem commonItem) {
        presenter.removeMergedItem(personalItem, commonItem);
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
    public boolean isCurrentUserHost() {
        return isHost;
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
        if (presenter != null) {
            presenter.onDestroy();
        }
        multiBinding = null;
        soloBinding = null;
        super.onDestroy();
    }
}
