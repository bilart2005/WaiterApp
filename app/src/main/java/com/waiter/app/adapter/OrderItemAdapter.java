package com.waiter.app.adapter;

import android.view.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import com.waiter.app.database.entities.OrderItem;
import com.waiter.app.databinding.ItemOrderItemBinding;

public class OrderItemAdapter extends ListAdapter<OrderItem, OrderItemAdapter.VH> {

    public interface OnQtyChange { void onChange(OrderItem item, int newQty); }
    public interface OnCommentClick { void onClick(OrderItem item); }

    private static final DiffUtil.ItemCallback<OrderItem> DIFF = new DiffUtil.ItemCallback<>() {
        @Override public boolean areItemsTheSame(@NonNull OrderItem a, @NonNull OrderItem b) { return a.id == b.id; }
        @Override public boolean areContentsTheSame(@NonNull OrderItem a, @NonNull OrderItem b) {
            return a.quantity == b.quantity && java.util.Objects.equals(a.comment, b.comment);
        }
    };

    private final OnQtyChange onQtyChange;
    private final OnCommentClick onCommentClick;

    public OrderItemAdapter(OnQtyChange onQtyChange, OnCommentClick onCommentClick) {
        super(DIFF);
        this.onQtyChange = onQtyChange;
        this.onCommentClick = onCommentClick;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemOrderItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        OrderItem item = getItem(position);
        h.b.tvItemName.setText(item.menuItemName);
        h.b.tvItemPrice.setText(String.format("%.0f ₽", item.menuItemPrice));
        h.b.tvQty.setText(String.valueOf(item.quantity));
        h.b.tvComment.setText(item.comment != null && !item.comment.isEmpty()
                ? "📝 " + item.comment : "Добавить комментарий");
        h.b.tvComment.setAlpha(item.comment != null && !item.comment.isEmpty() ? 1f : 0.5f);

        h.b.btnMinus.setOnClickListener(v -> onQtyChange.onChange(item, item.quantity - 1));
        h.b.btnPlus.setOnClickListener(v -> onQtyChange.onChange(item, item.quantity + 1));
        h.b.tvComment.setOnClickListener(v -> onCommentClick.onClick(item));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemOrderItemBinding b;
        VH(ItemOrderItemBinding b) { super(b.getRoot()); this.b = b; }
    }
}
