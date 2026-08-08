package com.example.mybaghackathon.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.model.UserDefaultItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** S15 프로필의 기본 물품 목록 어댑터. 미리보기(ProfileFragment)와 전체보기(ProfileItemsActivity)가 함께 쓴다. */
final class ProfileItemAdapter extends RecyclerView.Adapter<ProfileItemAdapter.ViewHolder> {

    interface OnItemClickListener {
        void onItemClick(int position, UserDefaultItem item);
    }

    private List<UserDefaultItem> items = Collections.emptyList();
    private final OnItemClickListener listener;

    ProfileItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    void submitList(List<UserDefaultItem> newItems) {
        items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.molecule_default_item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserDefaultItem item = items.get(position);
        holder.label.setText(item.getItemName());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(position, item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView label;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            label = (TextView) itemView;
        }
    }
}
