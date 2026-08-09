package com.example.mybaghackathon.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        AppContainer appContainer = ((MyBagApplication) requireActivity().getApplication()).getAppContainer();
        presenter = new HomePresenter(this, appContainer.tripRepository, appContainer.packingRepository);

        binding.homeTopAppBar.topAppBarTitle.setText(R.string.home_title);
        binding.homeTopAppBar.topAppBarAction.setVisibility(View.GONE);

        adapter = new TripRoomAdapter(trip -> {
            Intent intent = new Intent(getContext(), RoomDetailActivity.class);
            intent.putExtra(RoomDetailActivity.EXTRA_TRIP_ID, trip.tripId);
            intent.putExtra(RoomDetailActivity.EXTRA_ROOM_NAME, trip.title);
            startActivity(intent);
        });
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
        for (Trip trip : trips) {
            uiModels.add(toUiModel(trip, progressByTripId));
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
        boolean empty = trips.isEmpty();
        binding.homeEmptyState.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.homeTripRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.homeAddRoomFab.setVisibility(empty ? View.GONE : View.VISIBLE);
        adapter.submitList(trips);
    }

    /** 오늘이 여행 기간 안이면(=진행중) Active, 아니면 Upcoming 카드로 그린다. */
    private TripRoomUiModel toUiModel(Trip trip, Map<Long, Integer> progressByTripId) {
        String ddayText = DateUtils.formatDday(trip.getStartDate());
        if (DateUtils.isTravelingNow(trip.getStartDate(), trip.getEndDate())) {
            int progress = progressByTripId.getOrDefault(trip.getTripId(), 0);
            return TripRoomUiModel.active(trip.getTripId(), trip.getTripName(), ddayText,
                    toAvatarEntries(trip.getMembers()), progress);
        }
        return TripRoomUiModel.upcoming(trip.getTripId(), trip.getTripName(), ddayText);
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
            entries.add(new AvatarStackHelper.Entry(initial, color));
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
