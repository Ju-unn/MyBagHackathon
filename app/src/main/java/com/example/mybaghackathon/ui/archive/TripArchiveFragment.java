package com.example.mybaghackathon.ui.archive;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.FragmentArchiveBinding;
import com.example.mybaghackathon.ui.archive.adapter.ArchiveTripAdapter;
import com.example.mybaghackathon.ui.createroom.CreateRoomActivity;
import com.example.mybaghackathon.ui.molecules.AvatarStackHelper;
import com.example.mybaghackathon.ui.roomdetail.RoomDetailActivity;

import java.util.Arrays;
import java.util.List;

/**
 * S14 · MainActivity — TripArchiveFragment ("공용 여행" 탭): 사용자가 속한
 * 모든 방을 진행중/지난 여행으로 구분해서 RecyclerView로 보여줌.
 *
 * 기능: 진행중/지난 여행 세그먼트 칩을 눌러 목록을 필터링하고, 각 여행방을
 * 카드로 바인딩해 리스트에 보여준 뒤 클릭 시 RoomDetailActivity로 이동시킴
 * (F-JTDZJG). 선택한 세그먼트에 방이 하나도 없으면 EmptyState(NoArchive)를
 * 보여주고 "방 만들기" 버튼으로 CreateRoomActivity를 연다.
 */
public class TripArchiveFragment extends Fragment {

    private FragmentArchiveBinding binding;
    private ArchiveTripAdapter adapter;
    private TextView activeChip;
    private TextView pastChip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentArchiveBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.archiveTopAppBar.topAppBarTitle.setText(R.string.archive_title);
        binding.archiveTopAppBar.topAppBarAction.setVisibility(View.GONE);

        activeChip = binding.archiveSegmentActive;
        pastChip = binding.archiveSegmentPast;

        adapter = new ArchiveTripAdapter(trip ->
                startActivity(new Intent(getContext(), RoomDetailActivity.class)));
        binding.archiveTripRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.archiveTripRecycler.setAdapter(adapter);

        activeChip.setOnClickListener(v -> selectSegment(true));
        pastChip.setOnClickListener(v -> selectSegment(false));

        binding.archiveEmptyState.emptyStateTitle.setText(R.string.archive_empty_title);
        binding.archiveEmptyState.emptyStateDesc.setText(R.string.archive_empty_desc);
        binding.archiveEmptyState.emptyStateAction.setText(R.string.create_room_submit);
        binding.archiveEmptyState.emptyStateAction.setOnClickListener(
                v -> startActivity(new Intent(getContext(), CreateRoomActivity.class)));

        selectSegment(true);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void selectSegment(boolean active) {
        setSegmentSelected(activeChip, active);
        setSegmentSelected(pastChip, !active);

        List<ArchiveTripUiModel> trips = active ? ongoingTrips() : pastTrips();
        boolean empty = trips.isEmpty();
        binding.archiveTripRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.archiveEmptyState.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        adapter.submitList(trips);
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

    private List<ArchiveTripUiModel> ongoingTrips() {
        List<AvatarStackHelper.Entry> avatars = Arrays.asList(
                new AvatarStackHelper.Entry("김", ContextCompat.getColor(requireContext(), R.color.bag_avatar_1)),
                new AvatarStackHelper.Entry("민", ContextCompat.getColor(requireContext(), R.color.bag_avatar_2)),
                new AvatarStackHelper.Entry("유", ContextCompat.getColor(requireContext(), R.color.bag_avatar_3)));
        return Arrays.asList(
                ArchiveTripUiModel.ongoing(1L, "도쿄 벚꽃 여행", "D-12", avatars, 68),
                ArchiveTripUiModel.planned(2L, "오사카 미식 여행", "D-45"));
    }

    private List<ArchiveTripUiModel> pastTrips() {
        return Arrays.asList(
                ArchiveTripUiModel.past(3L, "부산 여름 여행"),
                ArchiveTripUiModel.past(4L, "오사카 벚꽃 여행"));
    }
}
