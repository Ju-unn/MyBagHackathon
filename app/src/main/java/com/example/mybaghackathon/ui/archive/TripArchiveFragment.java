package com.example.mybaghackathon.ui.archive;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.FragmentArchiveBinding;
import com.example.mybaghackathon.ui.createroom.CreateRoomActivity;
import com.example.mybaghackathon.ui.organisms.TripRoomCardBinder;
import com.example.mybaghackathon.ui.roomdetail.RoomDetailActivity;

/**
 * S14 · MainActivity — TripArchiveFragment ("공용 여행" 탭): 사용자가 속한
 * 모든 방을 진행중/지난 여행으로 구분해서 보여줌.
 *
 * 기능: 진행중/지난 여행 세그먼트 칩을 눌러 목록을 필터링하고, 각 여행방을
 * 카드로 inflate해 리스트에 추가한 뒤 클릭 시 RoomDetailActivity로 이동시킴
 * (F-JTDZJG). 선택한 세그먼트에 방이 하나도 없으면 EmptyState(NoArchive)를
 * 보여주고 "방 만들기" 버튼으로 CreateRoomActivity를 연다.
 */
public class TripArchiveFragment extends Fragment {

    private static final String[] ACTIVE_TRIPS = {"도쿄 벚꽃 여행", "제주 가족 여행"};
    private static final String[] PAST_TRIPS = {"부산 여름 여행", "강릉 워크숍"};

    private FragmentArchiveBinding binding;
    private LinearLayout list;
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

        list = binding.archiveTripList;
        activeChip = binding.archiveSegmentActive;
        pastChip = binding.archiveSegmentPast;

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
        list.removeAllViews();

        String[] trips = active ? ACTIVE_TRIPS : PAST_TRIPS;
        boolean empty = trips.length == 0;
        list.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.archiveEmptyState.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);

        for (String title : trips) {
            addCard(title, active);
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

    private void addCard(String title, boolean active) {
        View card = LayoutInflater.from(getContext())
                .inflate(R.layout.organism_trip_room_card, list, false);
        if (active) {
            TripRoomCardBinder.bindUpcoming(card, title, "D-12", getString(R.string.home_upload_needed));
        } else {
            TripRoomCardBinder.bindPast(card, title);
        }
        card.setOnClickListener(v -> startActivity(new Intent(getContext(), RoomDetailActivity.class)));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (list.getChildCount() > 0) lp.topMargin = dp(12);
        list.addView(card, lp);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
