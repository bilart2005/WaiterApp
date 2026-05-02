package com.waiter.app.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.waiter.app.database.entities.Order;
import com.waiter.app.databinding.ItemHistoryBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryAdapter extends ListAdapter<Order, HistoryAdapter.ViewHolder> {

    private final OnOrderClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault());

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public HistoryAdapter(OnOrderClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Order> DIFF_CALLBACK = new DiffUtil.ItemCallback<Order>() {
        @Override
        public boolean areItemsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
            return oldItem.tableNumber == newItem.tableNumber &&
                    oldItem.totalAmount == newItem.totalAmount &&
                    oldItem.closedAt == newItem.closedAt;
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = getItem(position);
        holder.binding.tvHistoryTable.setText("Стол " + order.tableNumber);
        holder.binding.tvHistoryTotal.setText(String.format("%.0f ₽", order.totalAmount));

        String date = dateFormat.format(new Date(order.closedAt));
        holder.binding.tvHistoryDate.setText(date);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemHistoryBinding binding;
        ViewHolder(ItemHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}