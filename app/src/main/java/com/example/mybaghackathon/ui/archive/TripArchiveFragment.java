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
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.ui.atoms.ChipView;
import com.example.mybaghackathon.ui.checklist.ChecklistActivity;
import com.example.mybaghackathon.ui.organisms.TripRoomCardBinder;

/**
 * S13 · 보관함 — "공용 여행" 탭: 사용자가 속한 모든 방을 진행중/지난 여행으로
 * 구분해서 보여줌.
 *
 * 기능: 진행중/지난 여행 세그먼트 칩을 눌러 목록을 필터링하고, 각 여행방을
 * 카드로 inflate해 리스트에 추가한 뒤 클릭 시 체크리스트 화면으로 이동시킴.
 */
public class TripArchiveFragment extends Fragment {

    private LinearLayout list;
    private ChipView activeChip;
    private ChipView pastChip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_archive, container, false);

        TextView title = root.findViewById(R.id.topAppBarTitle);
        title.setText(R.string.archive_title);
        root.findViewById(R.id.topAppBarAction).setVisibility(View.GONE);

        list = root.findViewById(R.id.archiveTripList);
        activeChip = root.findViewById(R.id.archiveSegmentActive);
        pastChip = root.findViewById(R.id.archiveSegmentPast);

        activeChip.setOnClickListener(v -> selectSegment(true));
        pastChip.setOnClickListener(v -> selectSegment(false));

        selectSegment(true);
        return root;
    }

    private void selectSegment(boolean active) {
        activeChip.setActive(active);
        pastChip.setActive(!active);
        list.removeAllViews();

        if (active) {
            addCard("도쿄 벚꽃 여행", true);
            addCard("제주 가족 여행", true);
        } else {
            addCard("부산 여름 여행", false);
            addCard("강릉 워크숍", false);
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
        card.setOnClickListener(v -> startActivity(new Intent(getContext(), ChecklistActivity.class)));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (list.getChildCount() > 0) lp.topMargin = dp(12);
        list.addView(card, lp);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
