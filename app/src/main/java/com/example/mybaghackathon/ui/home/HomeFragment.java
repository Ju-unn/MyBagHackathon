package com.example.mybaghackathon.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.mybaghackathon.databinding.FragmentHomeBinding;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.TripMember;
import com.example.mybaghackathon.ui.createroom.CreateRoomActivity;
import com.example.mybaghackathon.ui.home.adapter.TripRoomAdapter;
import com.example.mybaghackathon.ui.molecules.AvatarStackHelper;
import com.example.mybaghackathon.ui.roomdetail.RoomDetailActivity;
import com.example.mybaghackathon.util.DateUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * S03 · 홈 — MainActivity 하단 내비게이션 안에 있는 여행방 목록 탭.
 *
 * 기능: HomePresenter가 불러온 진행중인 여행방 목록을 RecyclerView로 보여주고,
 * 목록이 비어 있으면 EmptyState(NoRoom)를, 있으면 카드 목록 + 방 추가 카드를 보여준다.
 * 카드를 탭하면 RoomDetailActivity로 이동하고, 방 추가 버튼은 CreateRoomActivity를 연다.
 */
public class HomeFragment extends Fragment implements HomeContract.View {

    private static final int[] AVATAR_COLORS = {
            R.color.bag_avatar_1, R.color.bag_avatar_2, R.color.bag_avatar_3, R.color.bag_avatar_4
    };

    private FragmentHomeBinding binding;
    private TripRoomAdapter adapter;
    private HomeContract.Presenter presenter;
    private long currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        AppContainer appContainer = ((MyBagApplication) requireActivity().getApplication()).getAppContainer();
        presenter = new HomePresenter(this, appContainer.tripRepository, appContainer.packingRepository);
        currentUserId = appContainer.tokenStorage.getUserId();

        binding.homeTopAppBar.topAppBarTitle.setText(R.string.home_title);
        binding.homeTopAppBar.topAppBarAction.setVisibility(View.GONE);

        adapter = new TripRoomAdapter(
                trip -> {
                    Intent intent = new Intent(getContext(), RoomDetailActivity.class);
                    intent.putExtra(RoomDetailActivity.EXTRA_TRIP_ID, trip.tripId);
                    intent.putExtra(RoomDetailActivity.EXTRA_ROOM_NAME, trip.title);
                    startActivity(intent);
                },
                trip -> confirmDeleteTrip(trip.tripId, trip.title),
                trip -> confirmLeaveTrip(trip.tripId, trip.title));
        binding.homeTripRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.homeTripRecycler.setAdapter(adapter);

        binding.homeEmptyState.emptyStateTitle.setText(R.string.home_empty_title);
        binding.homeEmptyState.emptyStateDesc.setText(R.string.home_empty_desc);
        binding.homeEmptyState.emptyStateAction.setText(R.string.empty_state_create_room);
        binding.homeEmptyState.emptyStateAction.setOnClickListener(v -> openCreateRoom());
        binding.homeAddRoomFab.setOnClickListener(v -> openCreateRoom());

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.loadTrips();
    }

    // MainActivity가 탭 전환을 replace() 대신 hide()/show()로 처리하기 때문에,
    // 다른 탭에 있다 이 탭으로 돌아올 때는 onResume이 다시 불리지 않는다.
    // 그동안 방이 생성/삭제됐을 수 있으니 다시 보일 때마다 목록을 새로 불러온다.
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && binding != null) {
            presenter.loadTrips();
        }
    }

    private void openCreateRoom() {
        startActivity(new Intent(getContext(), CreateRoomActivity.class));
    }

    // ===== HomeContract.View =====

    @Override
    public void showTrips(List<Trip> trips, Map<Long, Integer> progressByTripId) {
        if (binding == null) {
            return;
        }
        List<TripRoomUiModel> uiModels = new ArrayList<>();
        for (int i = 0; i < trips.size(); i++) {
            uiModels.add(toUiModel(trips.get(i), progressByTripId, i == 0));
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

    private void bindTrips(List<TripRoomUiModel> trips) {
        adapter.submitList(trips);
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean empty = adapter.isEmpty();
        binding.homeEmptyStateSpacer.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.homeEmptyState.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.homeTripRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.homeAddRoomFab.setVisibility(empty ? View.GONE : View.VISIBLE);
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
        // 맨 앞 방이 지워졌을 수 있으니 목록을 다시 불러와 검정 강조 카드를 새로 계산한다.
        presenter.loadTrips();
    }

    @Override
    public void onTripLeft(long tripId) {
        if (binding == null) {
            return;
        }
        Toast.makeText(getContext(), R.string.trip_left_toast, Toast.LENGTH_SHORT).show();
        presenter.loadTrips();
    }

    /** 정렬된 목록의 맨 앞(가장 임박한/진행중인 방)은 항상 Active 카드(검정 강조)로 그리되,
     *  상태 라벨은 오늘이 여행 기간 안인지(D-day 도래)에 따라 진행중/여행 전으로 갈린다. */
    private TripRoomUiModel toUiModel(Trip trip, Map<Long, Integer> progressByTripId, boolean highlight) {
        String ddayText = DateUtils.formatDday(trip.getStartDate());
        boolean isOwner = trip.getOwnerUserId() == currentUserId;
        boolean isOngoing = DateUtils.isTravelingNow(trip.getStartDate(), trip.getEndDate());
        if (highlight) {
            int progress = progressByTripId.getOrDefault(trip.getTripId(), 0);
            return TripRoomUiModel.active(trip.getTripId(), trip.getTripName(), ddayText,
                    toAvatarEntries(trip.getMembers()), progress, isOwner, isOngoing);
        }
        return TripRoomUiModel.upcoming(trip.getTripId(), trip.getTripName(), ddayText, isOwner, isOngoing);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.onDestroy();
        }
        binding = null;
    }
}
