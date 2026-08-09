package com.example.mybaghackathon.ui.archive.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.ui.archive.ArchiveTripUiModel;
import com.example.mybaghackathon.ui.organisms.TripRoomCardBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * S14 공용 여행(아카이브) 목록 어댑터. 카드 자체의 색상·구성은
 * TripRoomCardBinder가 결정하고, 어댑터는 상태에 맞는 바인더 메서드를 호출하고
 * 카드 간 간격·클릭을 연결하는 역할만 한다.
 */
public class ArchiveTripAdapter extends RecyclerView.Adapter<ArchiveTripAdapter.ViewHolder> {

    public interface OnTripClickListener {
        void onTripClick(ArchiveTripUiModel trip);
    }

    private List<ArchiveTripUiModel> items = Collections.emptyList();
    private final OnTripClickListener listener;

    public ArchiveTripAdapter(OnTripClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ArchiveTripUiModel> newItems) {
        items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.organism_trip_room_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ArchiveTripUiModel trip = items.get(position);

        switch (trip.state) {
            case ONGOING:
                TripRoomCardBinder.bindActive(holder.itemView, trip.title, trip.ddayText,
                        trip.avatars, trip.progressPercent);
                break;
            case PLANNED:
                TripRoomCardBinder.bindUpcoming(holder.itemView, trip.title, trip.ddayText);
                break;
            case PAST:
                TripRoomCardBinder.bindPast(holder.itemView, trip.title);
                break;
        }

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        int spacing = holder.itemView.getResources().getDimensionPixelSize(R.dimen.space_md);
        lp.topMargin = position == 0 ? 0 : spacing;
        holder.itemView.setLayoutParams(lp);

        holder.itemView.setOnClickListener(v -> listener.onTripClick(trip));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
