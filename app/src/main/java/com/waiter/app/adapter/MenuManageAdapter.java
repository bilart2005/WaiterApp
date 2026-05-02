package com.waiter.app.adapter;

import android.view.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import com.waiter.app.database.entities.MenuItem;
import com.waiter.app.databinding.ItemMenuManageBinding;

public class MenuManageAdapter extends ListAdapter<MenuItem, MenuManageAdapter.VH> {

    public interface OnItemAction { void onAction(MenuItem item); }

    private static final DiffUtil.ItemCallback<MenuItem> DIFF = new DiffUtil.ItemCallback<>() {
        @Override public boolean areItemsTheSame(@NonNull MenuItem a, @NonNull MenuItem b) { return a.id == b.id; }
        @Override public boolean areContentsTheSame(@NonNull MenuItem a, @NonNull MenuItem b) {
            return a.name.equals(b.name) && a.price == b.price && a.section.equals(b.section);
        }
    };

    private final OnItemAction onEdit;
    private final OnItemAction onDelete;

    public MenuManageAdapter(OnItemAction onEdit, OnItemAction onDelete) {
        super(DIFF);
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemMenuManageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        MenuItem item = getItem(pos);
        h.b.tvName.setText(item.name);
        h.b.tvSection.setText(item.section);
        h.b.tvPrice.setText(String.format("%.0f ₽", item.price));
        h.b.btnEdit.setOnClickListener(v -> onEdit.onAction(item));
        h.b.btnDelete.setOnClickListener(v -> onDelete.onAction(item));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemMenuManageBinding b;
        VH(ItemMenuManageBinding b) { super(b.getRoot()); this.b = b; }
    }
}
