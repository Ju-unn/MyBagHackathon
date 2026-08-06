package com.example.mybaghackathon.ui.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.ui.home.TripRoomUiModel;
import com.example.mybaghackathon.ui.organisms.TripRoomCardBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * S03 홈 화면의 여행방 목록 어댑터. 카드 자체(진행중/준비전/완료)의 색상·구성은
 * TripRoomCardBinder가 결정하고, 어댑터는 상태에 맞는 바인더 메서드를 호출하고
 * 카드 간 간격·클릭을 연결하는 역할만 한다.
 */
public class TripRoomAdapter extends RecyclerView.Adapter<TripRoomAdapter.ViewHolder> {

    public interface OnTripClickListener {
        void onTripClick(TripRoomUiModel trip);
    }

    private List<TripRoomUiModel> items = Collections.emptyList();
    private final OnTripClickListener listener;

    public TripRoomAdapter(OnTripClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<TripRoomUiModel> newItems) {
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
        TripRoomUiModel trip = items.get(position);

        switch (trip.state) {
            case ACTIVE:
                TripRoomCardBinder.bindActive(holder.itemView, trip.title, trip.ddayText,
                        trip.avatars, trip.progressPercent);
                break;
            case UPCOMING:
                TripRoomCardBinder.bindUpcoming(holder.itemView, trip.title, trip.ddayText, trip.hint);
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
