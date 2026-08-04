package com.example.mybaghackathon.ui.home;

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
import com.example.mybaghackathon.databinding.FragmentHomeBinding;
import com.example.mybaghackathon.ui.checklist.ChecklistActivity;
import com.example.mybaghackathon.ui.createroom.CreateRoomActivity;
import com.example.mybaghackathon.ui.molecules.AvatarStackHelper;
import com.example.mybaghackathon.ui.organisms.TripRoomCardBinder;
import com.example.mybaghackathon.ui.roomdetail.RoomDetailActivity;

import java.util.Arrays;

/**
 * S03 · 홈 — MainActivity 하단 내비게이션 안에 있는 여행방 목록 탭.
 *
 * 기능: 진행중/예정/지난 여행 카드를 각각 만들어 TripRoomCardBinder로
 * 바인딩하고, 클릭 시 알맞은 화면(체크리스트/방 상세)으로 이동시키며,
 * 방 추가 버튼으로 CreateRoomActivity를 여는 홈 탭 화면.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView title = binding.homeTopAppBar.topAppBarTitle;
        title.setText(R.string.home_title);
        binding.homeTopAppBar.topAppBarAction.setVisibility(View.GONE);

        LinearLayout list = binding.homeTripList;

        View active = inflateCard(list);
        TripRoomCardBinder.bindActive(active, "도쿄 벚꽃 여행", "D-12",
                Arrays.asList(
                        new AvatarStackHelper.Entry("민", ContextCompat.getColor(requireContext(), R.color.bag_avatar_1)),
                        new AvatarStackHelper.Entry("유", ContextCompat.getColor(requireContext(), R.color.bag_avatar_2)),
                        new AvatarStackHelper.Entry("김", ContextCompat.getColor(requireContext(), R.color.bag_avatar_3))),
                68);
        active.setOnClickListener(v -> startActivity(new Intent(getContext(), ChecklistActivity.class)));
        addWithSpacing(list, active);

        View upcoming = inflateCard(list);
        TripRoomCardBinder.bindUpcoming(upcoming, "제주 가족 여행", "D-30",
                getString(R.string.home_upload_needed));
        upcoming.setOnClickListener(v -> startActivity(new Intent(getContext(), RoomDetailActivity.class)));
        addWithSpacing(list, upcoming);

        View past = inflateCard(list);
        TripRoomCardBinder.bindPast(past, "부산 여름 여행");
        addWithSpacing(list, past);

        binding.homeAddRoomWrapper.setOnClickListener(v ->
                startActivity(new Intent(getContext(), CreateRoomActivity.class)));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private View inflateCard(ViewGroup parent) {
        return LayoutInflater.from(getContext())
                .inflate(R.layout.organism_trip_room_card, parent, false);
    }

    private void addWithSpacing(LinearLayout list, View card) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (list.getChildCount() > 0) {
            lp.topMargin = dp(12);
        }
        if (card.getParent() == null) {
            list.addView(card, lp);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
