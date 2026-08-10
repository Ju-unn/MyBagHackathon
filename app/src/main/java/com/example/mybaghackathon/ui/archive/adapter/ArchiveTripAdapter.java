package com.example.mybaghackathon.ui.archive.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.ui.archive.ArchiveTripUiModel;
import com.example.mybaghackathon.ui.organisms.SwipeRevealHelper;
import com.example.mybaghackathon.ui.organisms.TripRoomCardBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * S14 공용 여행(아카이브) 목록 어댑터. 카드 자체의 색상·구성은
 * TripRoomCardBinder가 결정하고, 어댑터는 상태에 맞는 바인더 메서드를 호출하고
 * 카드 간 간격·클릭·왼쪽 스와이프 삭제를 연결하는 역할만 한다.
 */
public class ArchiveTripAdapter extends RecyclerView.Adapter<ArchiveTripAdapter.ViewHolder> {

    public interface OnTripClickListener {
        void onTripClick(ArchiveTripUiModel trip);
    }

    public interface OnTripDeleteListener {
        void onTripDelete(ArchiveTripUiModel trip);
    }

    private List<ArchiveTripUiModel> items = Collections.emptyList();
    private final OnTripClickListener clickListener;
    private final OnTripDeleteListener deleteListener;
    private final SwipeRevealHelper.Tracker swipeTracker = new SwipeRevealHelper.Tracker();

    public ArchiveTripAdapter(OnTripClickListener clickListener, OnTripDeleteListener deleteListener) {
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    public void submitList(List<ArchiveTripUiModel> newItems) {
        items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    /** 삭제 확인 후 로컬 목록에서만 제거한다(서버 삭제 API 연동 전까지의 임시 동작). */
    public void removeItem(long tripId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).tripId == tripId) {
                items.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_swipeable_trip_room_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ArchiveTripUiModel trip = items.get(position);

        switch (trip.state) {
            case ONGOING:
                TripRoomCardBinder.bindActive(holder.foreground, trip.title, trip.ddayText,
                        trip.avatars, trip.progressPercent);
                break;
            case PLANNED:
                TripRoomCardBinder.bindUpcoming(holder.foreground, trip.title, trip.ddayText);
                break;
            case PAST:
                TripRoomCardBinder.bindPast(holder.foreground, trip.title);
                break;
        }

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        int spacing = holder.itemView.getResources().getDimensionPixelSize(R.dimen.space_md);
        lp.topMargin = position == 0 ? 0 : spacing;
        holder.itemView.setLayoutParams(lp);

        float revealWidthPx = holder.itemView.getResources().getDimension(R.dimen.trip_delete_reveal_width);
        SwipeRevealHelper.reset(holder.foreground);
        SwipeRevealHelper.attach(holder.foreground, revealWidthPx, swipeTracker);

        holder.foreground.setOnClickListener(v -> {
            if (SwipeRevealHelper.isOpen(swipeTracker, holder.foreground)) {
                SwipeRevealHelper.closeOpenRow(swipeTracker);
                return;
            }
            clickListener.onTripClick(trip);
        });
        holder.deleteButton.setOnClickListener(v -> {
            SwipeRevealHelper.closeOpenRow(swipeTracker);
            deleteListener.onTripDelete(trip);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View foreground;
        final View deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            foreground = itemView.findViewById(R.id.tripSwipeForeground);
            deleteButton = itemView.findViewById(R.id.tripSwipeDeleteButton);
        }
    }
}
