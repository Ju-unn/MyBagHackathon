package com.example.mybaghackathon.ui.archive;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.AppContainer;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.databinding.FragmentArchiveBinding;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.TripMember;
import com.example.mybaghackathon.ui.archive.adapter.ArchiveTripAdapter;
import com.example.mybaghackathon.ui.createroom.CreateRoomActivity;
import com.example.mybaghackathon.ui.molecules.AvatarStackHelper;
import com.example.mybaghackathon.ui.roomdetail.RoomDetailActivity;
import com.example.mybaghackathon.util.DateUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * S14 · MainActivity — TripArchiveFragment ("공용 여행" 탭): 사용자가 속한
 * 모든 방을 진행중/지난 여행으로 구분해서 RecyclerView로 보여줌.
 *
 * 기능: ArchivePresenter가 불러온 목록으로 진행중/지난 여행 세그먼트 칩을 눌러
 * 목록을 전환하고, 각 여행방을 카드로 바인딩해 리스트에 보여준 뒤 클릭 시
 * RoomDetailActivity로 이동시킨다(F-JTDZJG). 선택한 세그먼트에 방이 하나도
 * 없으면 EmptyState(NoArchive)를 보여주고 "방 만들기" 버튼으로
 * CreateRoomActivity를 연다.
 */
public class TripArchiveFragment extends Fragment implements ArchiveContract.View {

    private static final int[] AVATAR_COLORS = {
            R.color.bag_avatar_2, R.color.bag_avatar_1, R.color.bag_avatar_4, R.color.bag_avatar_3
    };

    private FragmentArchiveBinding binding;
    private ArchiveTripAdapter adapter;
    private ArchiveContract.Presenter presenter;
    private TextView activeChip;
    private TextView pastChip;
    private boolean showingOngoing = true;
    private long currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentArchiveBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        AppContainer appContainer = ((MyBagApplication) requireActivity().getApplication()).getAppContainer();
        currentUserId = appContainer.tokenStorage.getUserId();
        presenter = new ArchivePresenter(this, appContainer.tripRepository, appContainer.packingRepository,
                currentUserId, requireContext());

        binding.archiveTopAppBar.topAppBarTitle.setText(R.string.archive_title);
        binding.archiveTopAppBar.topAppBarAction.setVisibility(View.GONE);

        activeChip = binding.archiveSegmentActive;
        pastChip = binding.archiveSegmentPast;

        adapter = new ArchiveTripAdapter(
                trip -> {
                    Intent intent = new Intent(getContext(), RoomDetailActivity.class);
                    intent.putExtra(RoomDetailActivity.EXTRA_TRIP_ID, trip.tripId);
                    intent.putExtra(RoomDetailActivity.EXTRA_ROOM_NAME, trip.title);
                    startActivity(intent);
                },
                trip -> confirmDeleteTrip(trip.tripId, trip.title),
                trip -> confirmLeaveTrip(trip.tripId, trip.title));
        binding.archiveTripRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.archiveTripRecycler.setAdapter(adapter);

        activeChip.setOnClickListener(v -> selectSegment(true));
        pastChip.setOnClickListener(v -> selectSegment(false));

        binding.archiveEmptyState.emptyStateAction.setText(R.string.empty_state_create_room);
        binding.archiveEmptyState.emptyStateAction.setOnClickListener(
                v -> startActivity(new Intent(getContext(), CreateRoomActivity.class)));

        binding.archiveAddRoomFab.setOnClickListener(
                v -> startActivity(new Intent(getContext(), CreateRoomActivity.class)));

        selectSegment(true);
        return root;
    }

    @Override
    public void onDestroyView() {
        if (presenter != null) {
            presenter.onDestroy();
        }
        binding = null;
        super.onDestroyView();
    }

    // 방 생성/삭제/나가기 화면을 갔다가 이 탭이 보이는 채로 MainActivity로 돌아올 때
    // (탭 전환이 아니라 하위 액티비티에서의 복귀) 반영하기 위해 필요하다.
    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            reloadCurrentSegment();
        }
    }

    // MainActivity가 탭 전환을 replace() 대신 hide()/show()로 처리하기 때문에,
    // 다른 탭에 있다 이 탭으로 돌아올 때는 onResume이 다시 불리지 않는다.
    // 그동안 방이 생성/삭제됐을 수 있으니 다시 보일 때마다 현재 세그먼트를 다시 불러온다.
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && binding != null) {
            reloadCurrentSegment();
        }
    }

    private void selectSegment(boolean ongoing) {
        showingOngoing = ongoing;
        setSegmentSelected(activeChip, ongoing);
        setSegmentSelected(pastChip, !ongoing);

        if (ongoing) {
            presenter.loadOngoingTrips();
        } else {
            presenter.loadPastTrips();
        }
    }

    private void setSegmentSelected(TextView segment, boolean selected) {
        if (selected) {
            segment.setBackgroundResource(R.drawable.bg_segment_selected);
            segment.setTextAppearance(R.style.TextAppearance_Bag_TitleS);
        } else {
            segment.setBackground(null);
            segment.setTextAppearance(R.style.TextAppearance_Bag_BodyM);
            segment.setTextColor(ContextCompat.getColor(requireContext(), R.color.bag_text_tertiary_safe));
        }
    }

    // ===== ArchiveContract.View =====

    @Override
    public void showOngoingTrips(List<Trip> trips, Map<Long, Integer> progressByTripId) {
        if (binding == null || !showingOngoing) {
            return;
        }
        List<ArchiveTripUiModel> uiModels = new ArrayList<>();
        for (int i = 0; i < trips.size(); i++) {
            uiModels.add(toOngoingUiModel(trips.get(i), progressByTripId, i == 0));
        }
        bindTrips(uiModels);
    }

    @Override
    public void showPastTrips(List<Trip> trips) {
        if (binding == null || showingOngoing) {
            return;
        }
        List<ArchiveTripUiModel> uiModels = new ArrayList<>();
        for (Trip trip : trips) {
            boolean isOwner = trip.getOwnerUserId() == currentUserId;
            String ddayText = DateUtils.formatDday(trip.getStartDate());
            uiModels.add(ArchiveTripUiModel.past(trip.getTripId(), trip.getTripName(), ddayText, isOwner));
        }
        bindTrips(uiModels);
    }

    @Override
    public void showError(String message) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void bindTrips(List<ArchiveTripUiModel> trips) {
        adapter.submitList(trips);
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean empty = adapter.isEmpty();
        binding.archiveTripRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.archiveEmptyState.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.archiveAddRoomFab.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (!empty) {
            return;
        }
        // 새로 만든 방은 항상 "진행중"으로 시작해서 "지난 여행"에는 절대 안 뜨므로,
        // 지난 여행 탭이 비었을 땐 방 만들기를 권하지 않는다.
        binding.archiveEmptyState.emptyStateTitle.setText(
                showingOngoing ? R.string.archive_empty_title : R.string.archive_empty_past_title);
        binding.archiveEmptyState.emptyStateDesc.setText(
                showingOngoing ? R.string.archive_empty_desc : R.string.archive_empty_past_desc);
        binding.archiveEmptyState.emptyStateAction.setVisibility(
                showingOngoing ? View.VISIBLE : View.GONE);
    }

    private void confirmDeleteTrip(long tripId, String title) {
        if (getContext() == null) {
            return;
        }
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Bag_ConfirmDialog)
                .setTitle(R.string.trip_delete_dialog_title)
                .setMessage(getString(R.string.trip_delete_dialog_message_format, title))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (d, which) -> presenter.deleteTrip(tripId))
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.clay_600));
    }

    private void confirmLeaveTrip(long tripId, String title) {
        if (getContext() == null) {
            return;
        }
        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Bag_ConfirmDialog)
                .setTitle(R.string.trip_leave_dialog_title)
                .setMessage(getString(R.string.trip_leave_dialog_message_format, title))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_leave, (dialog, which) -> presenter.leaveTrip(tripId))
                .show();
    }

    @Override
    public void onTripDeleted(long tripId) {
        if (binding == null) {
            return;
        }
        Toast.makeText(getContext(), R.string.trip_deleted_toast, Toast.LENGTH_SHORT).show();
        reloadCurrentSegment();
    }

    @Override
    public void onTripLeft(long tripId) {
        if (binding == null) {
            return;
        }
        Toast.makeText(getContext(), R.string.trip_left_toast, Toast.LENGTH_SHORT).show();
        reloadCurrentSegment();
    }

    // 맨 앞 방이 지워졌을 수 있으니 현재 보고 있는 세그먼트를 다시 불러와 검정 강조 카드를 새로 계산한다.
    private void reloadCurrentSegment() {
        if (showingOngoing) {
            presenter.loadOngoingTrips();
        } else {
            presenter.loadPastTrips();
        }
    }

    /** 정렬된 목록의 맨 앞(가장 임박한/진행중인 방)만 Ongoing 카드(검정 강조)로 그리고
     *  나머지는 흰색 카드로 그리되, 아바타·체크리스트 진행률은 모든 카드가 똑같이 자세히 보여준다.
     *  상태 라벨은 오늘이 여행 기간 안인지(D-day 도래)에 따라 진행중/여행 전으로 갈린다. */
    private ArchiveTripUiModel toOngoingUiModel(Trip trip, Map<Long, Integer> progressByTripId, boolean highlight) {
        String ddayText = DateUtils.formatDday(trip.getStartDate());
        boolean isOwner = trip.getOwnerUserId() == currentUserId;
        boolean isOngoing = DateUtils.isTravelingNow(trip.getStartDate(), trip.getEndDate());
        int progress = progressByTripId.getOrDefault(trip.getTripId(), 0);
        List<AvatarStackHelper.Entry> avatars = toAvatarEntries(trip.getMembers());
        boolean solo = isSoloTrip(trip);
        if (highlight) {
            return ArchiveTripUiModel.ongoing(trip.getTripId(), trip.getTripName(), ddayText,
                    avatars, progress, isOwner, isOngoing, solo);
        }
        return ArchiveTripUiModel.planned(trip.getTripId(), trip.getTripName(), ddayText,
                avatars, progress, isOwner, isOngoing, solo);
    }

    // ArchivePresenter#isSoloTrip과 동일한 기준 — 진행률 라벨을 "체크리스트"(1인) vs
    // "공용 리스트"(다인)로 가르는 데도 체크리스트 화면과 같은 판정이 필요하다.
    private boolean isSoloTrip(Trip trip) {
        Integer expected = trip.getExpectedMemberCount();
        int effectiveCount = expected == null || expected <= 0
                ? Math.max(1, trip.getMembers().size())
                : expected;
        return effectiveCount <= 1;
    }

    private List<AvatarStackHelper.Entry> toAvatarEntries(List<TripMember> members) {
        List<AvatarStackHelper.Entry> entries = new ArrayList<>();
        if (members == null) {
            return entries;
        }
        for (int i = 0; i < members.size(); i++) {
            String nickname = members.get(i).getNickname();
            String initial = nickname == null || nickname.isEmpty() ? "" : nickname.substring(0, 1);
            int color = ContextCompat.getColor(requireContext(), AVATAR_COLORS[i % AVATAR_COLORS.length]);
            entries.add(new AvatarStackHelper.Entry(initial, color, members.get(i).getProfileImageUrl()));
        }
        return entries;
    }
}
