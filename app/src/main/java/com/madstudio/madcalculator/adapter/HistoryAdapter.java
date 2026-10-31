package com.madstudio.madcalculator.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.madstudio.madcalculator.R;
import com.madstudio.madcalculator.model.HistoryItem;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnHistoryItemClickListener {
        void onHistoryItemClick(HistoryItem item);
    }

    private final List<HistoryItem> items = new ArrayList<>();
    private final OnHistoryItemClickListener listener;

    public HistoryAdapter(OnHistoryItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<HistoryItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvExpression;
        private final TextView tvTime;
        private final TextView tvResult;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpression = itemView.findViewById(R.id.tvItemExpression);
            tvTime = itemView.findViewById(R.id.tvItemTime);
            tvResult = itemView.findViewById(R.id.tvItemResult);
        }

        public void bind(HistoryItem item, OnHistoryItemClickListener listener) {
            tvExpression.setText(item.getExpression());
            tvTime.setText(item.getFormattedTime());
            tvResult.setText(item.getResult());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHistoryItemClick(item);
                }
            });
        }
    }
}
