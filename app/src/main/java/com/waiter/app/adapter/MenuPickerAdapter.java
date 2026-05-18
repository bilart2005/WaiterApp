package com.waiter.app.adapter;

import android.util.TypedValue;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.*;
import com.waiter.app.R;
import com.waiter.app.database.entities.MenuItem;
import com.waiter.app.databinding.ItemMenuPickerBinding;
import java.util.*;

public class MenuPickerAdapter extends ListAdapter<MenuPickerAdapter.DisplayItem, RecyclerView.ViewHolder> {

    public interface OnDishClick { void onClick(MenuItem item); }

    public static class DisplayItem {
        public final MenuItem item;
        public final String header;
        public final boolean isHeader;

        private DisplayItem(MenuItem item, String header, boolean isHeader) {
            this.item = item;
            this.header = header;
            this.isHeader = isHeader;
        }

        public static DisplayItem forItem(MenuItem item) { return new DisplayItem(item, null, false); }
        public static DisplayItem forHeader(String header) { return new DisplayItem(null, header, true); }
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    static final DiffUtil.ItemCallback<DisplayItem> DIFF = new DiffUtil.ItemCallback<>() {
        @Override public boolean areItemsTheSame(@NonNull DisplayItem a, @NonNull DisplayItem b) {
            if (a.isHeader && b.isHeader) return a.header.equals(b.header);
            if (!a.isHeader && !b.isHeader) return a.item.id == b.item.id;
            return false;
        }
        @Override public boolean areContentsTheSame(@NonNull DisplayItem a, @NonNull DisplayItem b) {
            if (a.isHeader && b.isHeader) return a.header.equals(b.header);
            if (!a.isHeader && !b.isHeader) return a.item.name.equals(b.item.name) && a.item.price == b.item.price;
            return false;
        }
    };

    private final OnDishClick onAdd;
    private final OnDishClick onInfo;
    private final Set<String> expandedSections = new HashSet<>();
    private List<MenuItem> lastItems;

    public MenuPickerAdapter(OnDishClick onAdd, OnDishClick onInfo) {
        super(DIFF);
        this.onAdd = onAdd;
        this.onInfo = onInfo;
    }

    public void toggleSection(String section) {
        if (expandedSections.contains(section)) expandedSections.remove(section);
        else expandedSections.add(section);
        if (lastItems != null) submitMenuItems(lastItems);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isHeader ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            TextView tv = view.findViewById(android.R.id.text1);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tv.setPadding(48, 24, 16, 8);
            tv.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.primary));
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setAllCaps(true);
            tv.setLetterSpacing(0.1f);
            return new HeaderVH(view);
        }
        return new ItemVH(ItemMenuPickerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        DisplayItem di = getItem(position);
        if (di.isHeader) {
            TextView tv = h.itemView.findViewById(android.R.id.text1);
            boolean expanded = expandedSections.contains(di.header);
            tv.setText((expanded ? "▼ " : "▶ ") + di.header);
            h.itemView.setOnClickListener(v -> toggleSection(di.header));
        } else {
            ItemVH vh = (ItemVH) h;
            MenuItem item = di.item;
            vh.b.tvDishName.setText(item.name);
            vh.b.tvDishPrice.setText(String.format("%.0f ₽", item.price));
            vh.b.tvCookingTime.setText(item.cookingTimeMinutes > 0 ? item.cookingTimeMinutes + "м" : "");
            
            vh.b.btnAdd.setOnClickListener(v -> onAdd.onClick(item));
            vh.b.btnInfo.setOnClickListener(v -> onInfo.onClick(item));
            
            vh.b.getRoot().setOnLongClickListener(v -> {
                onInfo.onClick(item);
                return true;
            });
            
            if (vh.b.getRoot().findViewById(R.id.click_overlay) != null) {
                vh.b.getRoot().findViewById(R.id.click_overlay).setOnClickListener(v -> onAdd.onClick(item));
            } else {
                vh.b.getRoot().setOnClickListener(v -> onAdd.onClick(item));
            }
        }
    }

    public void submitMenuItems(List<MenuItem> items) {
        this.lastItems = items;
        if (items == null) {
            submitList(null);
            return;
        }
        List<DisplayItem> displayList = new ArrayList<>();
        List<MenuItem> sorted = new ArrayList<>(items);
        Collections.sort(sorted, (a, b) -> {
            int pA = getCategoryPriority(a.section);
            int pB = getCategoryPriority(b.section);
            if (pA != pB) return pA - pB;
            String sA = a.section != null ? a.section : "";
            String sB = b.section != null ? b.section : "";
            if (!sA.equals(sB)) return sA.compareTo(sB);
            return a.name.compareTo(b.name);
        });

        String lastSection = null;
        for (MenuItem item : sorted) {
            String section = item.section != null ? item.section : "Прочее";
            if (!section.equals(lastSection)) {
                displayList.add(DisplayItem.forHeader(section));
                lastSection = section;
            }
            if (expandedSections.contains(section)) {
                displayList.add(DisplayItem.forItem(item));
            }
        }
        submitList(displayList);
    }

    private int getCategoryPriority(String section) {
        if (section == null) return 99;
        switch (section) {
            case "Напитки": return 1;
            case "Чай и Кофе": return 2;
            case "Пиво": return 3;
            case "Вина": return 4;
            case "Алкоголь": return 5;
            case "Завтраки": return 6;
            case "Закуски": return 7;
            case "Супы": return 8;
            case "Салаты": return 9;
            case "Паста": return 10;
            case "Пицца": return 11;
            case "Горячее": return 12;
            case "Гарниры": return 13;
            case "Десерты": return 14;
            default: return 30;
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        final ItemMenuPickerBinding b;
        ItemVH(ItemMenuPickerBinding b) { super(b.getRoot()); this.b = b; }
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        HeaderVH(View v) { super(v); }
    }
}
