package com.waiter.app.adapter;

import android.view.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import com.waiter.app.database.entities.Order;
import com.waiter.app.databinding.ItemTableBinding;
import java.util.Locale;
import java.util.Objects;

public class TableAdapter extends ListAdapter<TableAdapter.TableItem, TableAdapter.VH> {

    public static class TableItem {
        public final int tableNumber;
        public final Order order; // null = free

        public TableItem(int tableNumber, Order order) {
            this.tableNumber = tableNumber;
            this.order = order;
        }
    }

    private static final DiffUtil.ItemCallback<TableItem> DIFF = new DiffUtil.ItemCallback<>() {
        @Override public boolean areItemsTheSame(@NonNull TableItem a, @NonNull TableItem b) {
            return a.tableNumber == b.tableNumber;
        }
        @Override public boolean areContentsTheSame(@NonNull TableItem a, @NonNull TableItem b) {
            if (a.order == null && b.order == null) return true;
            if (a.order == null || b.order == null) return false;
            return a.order.id == b.order.id && a.order.totalAmount == b.order.totalAmount;
        }
    };

    private final OnTableClickListener listener;

    public TableAdapter(OnTableClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemTableBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TableItem item = getItem(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> listener.onTableClick(item));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemTableBinding b;
        VH(ItemTableBinding b) { super(b.getRoot()); this.b = b; }

        void bind(TableItem item) {
            b.tvTableNumber.setText("Стол " + item.tableNumber);
            if (item.order != null) {
                b.tvStatus.setText("Занят");
                b.getRoot().setCardBackgroundColor(0xFFFFECB3); // amber light
            } else {
                b.tvStatus.setText("Свободен");
                b.getRoot().setCardBackgroundColor(0xFFE8F5E9); // green light
            }
        }
    }

    public interface OnTableClickListener { void onTableClick(TableItem item); }
}
