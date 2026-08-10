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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentArchiveBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        AppContainer appContainer = ((MyBagApplication) requireActivity().getApplication()).getAppContainer();
        presenter = new ArchivePresenter(this, appContainer.tripRepository, appContainer.packingRepository);

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
                trip -> confirmDeleteTrip(trip.tripId, trip.title));
        binding.archiveTripRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.archiveTripRecycler.setAdapter(adapter);

        activeChip.setOnClickListener(v -> selectSegment(true));
        pastChip.setOnClickListener(v -> selectSegment(false));

        binding.archiveEmptyState.emptyStateTitle.setText(R.string.archive_empty_title);
        binding.archiveEmptyState.emptyStateDesc.setText(R.string.archive_empty_desc);
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
        for (Trip trip : trips) {
            uiModels.add(toOngoingUiModel(trip, progressByTripId));
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
            uiModels.add(ArchiveTripUiModel.past(trip.getTripId(), trip.getTripName()));
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
    }

    private void confirmDeleteTrip(long tripId, String title) {
        if (getContext() == null) {
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.trip_delete_dialog_title)
                .setMessage(getString(R.string.trip_delete_dialog_message_format, title))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> presenter.deleteTrip(tripId))
                .show();
    }

    @Override
    public void onTripDeleted(long tripId) {
        if (binding == null) {
            return;
        }
        adapter.removeItem(tripId);
        updateEmptyState();
        Toast.makeText(getContext(), R.string.trip_deleted_toast, Toast.LENGTH_SHORT).show();
    }

    /** 오늘이 여행 기간 안이면(=진행중) Ongoing 카드, 아니면 Planned 카드로 그린다. */
    private ArchiveTripUiModel toOngoingUiModel(Trip trip, Map<Long, Integer> progressByTripId) {
        String ddayText = DateUtils.formatDday(trip.getStartDate());
        if (DateUtils.isTravelingNow(trip.getStartDate(), trip.getEndDate())) {
            int progress = progressByTripId.getOrDefault(trip.getTripId(), 0);
            return ArchiveTripUiModel.ongoing(trip.getTripId(), trip.getTripName(), ddayText,
                    toAvatarEntries(trip.getMembers()), progress);
        }
        return ArchiveTripUiModel.planned(trip.getTripId(), trip.getTripName(), ddayText);
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
}
