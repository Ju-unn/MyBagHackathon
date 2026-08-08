package com.example.mybaghackathon.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.FragmentHomeBinding;
import com.example.mybaghackathon.model.Trip;
import com.example.mybaghackathon.model.TripMember;
import com.example.mybaghackathon.ui.createroom.CreateRoomActivity;
import com.example.mybaghackathon.ui.home.adapter.TripRoomAdapter;
import com.example.mybaghackathon.ui.molecules.AvatarStackHelper;
import com.example.mybaghackathon.ui.roomdetail.RoomDetailActivity;
import com.example.mybaghackathon.util.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * S03 · 홈 — MainActivity 하단 내비게이션 안에 있는 여행방 목록 탭.
 *
 * 기능: 여행방 목록을 RecyclerView로 보여주고, 목록이 비어 있으면
 * EmptyState(NoRoom)를, 있으면 카드 목록 + 방 추가 카드를 보여준다.
 * 카드를 탭하면 알맞은 화면(체크리스트/방 상세)으로 이동하고, 방 추가
 * 버튼은 CreateRoomActivity를 연다.
 */
public class HomeFragment extends Fragment {

    // TODO: TripRepository/서버 연동 시 실제 계약된 status 값으로 교체 (아직 백엔드 미정)
    private static final String TRIP_STATUS_ACTIVE = "ACTIVE";
    private static final String TRIP_STATUS_UPCOMING = "UPCOMING";
    private static final String TRIP_STATUS_COMPLETED = "COMPLETED";

    private static final int[] AVATAR_COLORS = {
            R.color.bag_avatar_1, R.color.bag_avatar_2, R.color.bag_avatar_3, R.color.bag_avatar_4
    };

    private FragmentHomeBinding binding;
    private TripRoomAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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
        binding.homeEmptyState.emptyStateAction.setText(R.string.create_room_submit);
        binding.homeEmptyState.emptyStateAction.setOnClickListener(v -> openCreateRoom());
        binding.homeAddRoomWrapper.setOnClickListener(v -> openCreateRoom());

        bindTrips(toHomeUiModels(mockTrips()));

        return root;
    }

    private void bindTrips(List<TripRoomUiModel> trips) {
        boolean empty = trips.isEmpty();
        binding.homeEmptyState.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.homeTripRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.homeAddRoomWrapper.setVisibility(empty ? View.GONE : View.VISIBLE);
        adapter.submitList(trips);
    }

    private void openCreateRoom() {
        startActivity(new Intent(getContext(), CreateRoomActivity.class));
    }

    /** Trip 목록을 홈 카드용 모델로 바꾸면서, 완료된(지난) 여행은 제외한다. */
    private List<TripRoomUiModel> toHomeUiModels(List<Trip> trips) {
        List<TripRoomUiModel> result = new ArrayList<>();
        for (Trip trip : trips) {
            if (TRIP_STATUS_COMPLETED.equals(trip.getStatus())) {
                continue; // 홈은 완료 여행을 보여주지 않음(Archive 탭 전용)
            }
            result.add(toUiModel(trip));
        }
        return result;
    }

    /** Trip(+TripMember)에는 없는 화면 전용 값(D-day 텍스트·힌트·체크리스트 진행률)만 여기서 계산한다. */
    private TripRoomUiModel toUiModel(Trip trip) {
        String ddayText = DateUtils.formatDday(trip.getStartDate());
        if (TRIP_STATUS_ACTIVE.equals(trip.getStatus())) {
            return TripRoomUiModel.active(trip.getTripId(), trip.getTripName(), ddayText,
                    toAvatarEntries(trip.getMembers()), mockChecklistPercent(trip.getTripId()));
        }
        return TripRoomUiModel.upcoming(trip.getTripId(), trip.getTripName(), ddayText,
                getString(R.string.home_upload_needed));
    }

    private List<AvatarStackHelper.Entry> toAvatarEntries(List<TripMember> members) {
        List<AvatarStackHelper.Entry> entries = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            String nickname = members.get(i).getNickname();
            String initial = nickname == null || nickname.isEmpty() ? "" : nickname.substring(0, 1);
            int color = ContextCompat.getColor(requireContext(), AVATAR_COLORS[i % AVATAR_COLORS.length]);
            entries.add(new AvatarStackHelper.Entry(initial, color));
        }
        return entries;
    }

    /** 체크리스트 완료율은 Trip 도메인에 없는 값 — 실제 체크리스트 API 연동 전까지의 임시 목업. */
    private int mockChecklistPercent(long tripId) {
        return tripId == 1L ? 68 : 0;
    }

    private List<Trip> mockTrips() {
        List<Trip> trips = new ArrayList<>();
        trips.add(new Trip(1L, 100L, "도쿄 벚꽃 여행", 3, "OVERSEAS", "일본", "도쿄",
                "2026-08-18", "2026-08-22", TRIP_STATUS_ACTIVE, null,
                Arrays.asList(
                        new TripMember(1L, "민", null, "MEMBER", "ACTIVE", null),
                        new TripMember(2L, "유", null, "MEMBER", "ACTIVE", null),
                        new TripMember(3L, "김", null, "OWNER", "ACTIVE", null))));
        trips.add(new Trip(2L, 100L, "제주 가족 여행", 2, "DOMESTIC", "대한민국", "제주",
                "2026-09-05", "2026-09-08", TRIP_STATUS_UPCOMING, null, Collections.emptyList()));
        trips.add(new Trip(3L, 100L, "부산 여름 여행", 2, "DOMESTIC", "대한민국", "부산",
                "2026-07-01", "2026-07-03", TRIP_STATUS_COMPLETED, null, Collections.emptyList())); // 홈에서는 필터링됨
        return trips;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
