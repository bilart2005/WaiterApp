package com.waiter.app.adapter;

import android.view.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import com.waiter.app.database.entities.MenuItem;
import com.waiter.app.databinding.ItemMenuPickerBinding;

public class MenuPickerAdapter extends ListAdapter<MenuItem, MenuPickerAdapter.VH> {

    public interface OnDishClick { void onClick(MenuItem item); }

    private static final DiffUtil.ItemCallback<MenuItem> DIFF = new DiffUtil.ItemCallback<>() {
        @Override public boolean areItemsTheSame(@NonNull MenuItem a, @NonNull MenuItem b) { return a.id == b.id; }
        @Override public boolean areContentsTheSame(@NonNull MenuItem a, @NonNull MenuItem b) {
            return a.name.equals(b.name) && a.price == b.price;
        }
    };

    private final OnDishClick onAdd;
    private final OnDishClick onInfo;

    public MenuPickerAdapter(OnDishClick onAdd, OnDishClick onInfo) {
        super(DIFF);
        this.onAdd = onAdd;
        this.onInfo = onInfo;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemMenuPickerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MenuItem item = getItem(position);
        h.b.tvDishName.setText(item.name);
        h.b.tvDishSection.setText(item.section);
        h.b.tvDishPrice.setText(String.format("%.0f ₽", item.price));
        h.b.tvCookingTime.setText(item.cookingTimeMinutes > 0 ? item.cookingTimeMinutes + " мин" : "—");
        h.b.btnAdd.setOnClickListener(v -> onAdd.onClick(item));
        h.b.btnInfo.setOnClickListener(v -> onInfo.onClick(item));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemMenuPickerBinding b;
        VH(ItemMenuPickerBinding b) { super(b.getRoot()); this.b = b; }
    }
}
