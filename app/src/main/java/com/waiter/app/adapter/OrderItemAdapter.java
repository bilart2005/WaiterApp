package com.waiter.app.adapter;

import android.view.*;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.*;
import com.waiter.app.R;
import com.waiter.app.database.entities.Order;
import com.waiter.app.database.entities.OrderItem;
import com.waiter.app.databinding.ItemOrderItemBinding;
import java.util.*;

public class OrderItemAdapter extends ListAdapter<OrderItemAdapter.ItemWrapper, OrderItemAdapter.VH> {

    public interface OrderItemListener {
        void onQtyChange(OrderItem item, int newQty);
        void onCommentChange(OrderItem item);
        void onStatusChange(OrderItem item, boolean isServed);
        void onDelete(OrderItem item);
        void onInfoClick(OrderItem item);
    }

    // --- WRAPPER CLASS TO FIX GROUPING GLITCHES ---
    // This class stores the calculated header visibility at submission time.
    // Since it's part of the adapter's item list, DiffUtil will correctly
    // trigger a re-bind if a header needs to show/hide.
    public static class ItemWrapper {
        public final OrderItem item;
        public final boolean showGuestHeader;
        public final boolean showCategoryHeader;
        public final String guestNameDisplay;

        public ItemWrapper(OrderItem item, boolean showGuestHeader, boolean showCategoryHeader, String guestNameDisplay) {
            this.item = item;
            this.showGuestHeader = showGuestHeader;
            this.showCategoryHeader = showCategoryHeader;
            this.guestNameDisplay = guestNameDisplay;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemWrapper that = (ItemWrapper) o;
            return showGuestHeader == that.showGuestHeader &&
                    showCategoryHeader == that.showCategoryHeader &&
                    Objects.equals(item.id, that.item.id) &&
                    item.quantity == that.item.quantity &&
                    item.isServed == that.item.isServed &&
                    Objects.equals(item.comment, that.item.comment) &&
                    Objects.equals(guestNameDisplay, that.guestNameDisplay);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item.id, item.quantity, item.isServed, item.comment, showGuestHeader, showCategoryHeader, guestNameDisplay);
        }
    }

    private final OrderItemListener listener;
    private Order currentOrder;

    public OrderItemAdapter(OrderItemListener listener) {
        super(new DiffUtil.ItemCallback<ItemWrapper>() {
            @Override public boolean areItemsTheSame(@NonNull ItemWrapper a, @NonNull ItemWrapper b) { 
                return a.item.id == b.item.id; 
            }
            @Override public boolean areContentsTheSame(@NonNull ItemWrapper a, @NonNull ItemWrapper b) {
                return a.equals(b);
            }
        });
        this.listener = listener;
    }

    public void setOrder(Order order) {
        this.currentOrder = order;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemOrderItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ItemWrapper wrapper = getItem(position);
        OrderItem item = wrapper.item;

        // Headers visibility from wrapper (guaranteed correct)
        h.b.tvGuestHeader.setVisibility(wrapper.showGuestHeader ? View.VISIBLE : View.GONE);
        if (wrapper.showGuestHeader) {
            h.b.tvGuestHeader.setText(wrapper.guestNameDisplay);
        }

        h.b.tvCategoryHeader.setVisibility(wrapper.showCategoryHeader ? View.VISIBLE : View.GONE);
        if (wrapper.showCategoryHeader) {
            String section = item.menuItemSection;
            h.b.tvCategoryHeader.setText(section == null || section.isEmpty() ? "Прочее" : section);
        }

        // --- Item Content (Forced Black & Contrast) ---
        h.b.tvItemName.setText(item.menuItemName);
        h.b.tvItemName.setTextColor(0xFF000000); // Solid Black
        
        h.b.tvItemPrice.setText(String.format("%.0f ₽", item.menuItemPrice));
        h.b.tvQty.setText("x" + item.quantity);
        h.b.tvQty.setTextColor(0xFF000000); // Solid Black
        
        h.b.tvComment.setText(item.comment == null || item.comment.isEmpty() ? "Добавить заметку..." : "📝 " + item.comment);
        h.b.cbServed.setChecked(item.isServed);

        h.b.cbServed.setOnClickListener(v -> listener.onStatusChange(item, h.b.cbServed.isChecked()));
        h.b.tvComment.setOnClickListener(v -> listener.onCommentChange(item));
        
        h.itemView.setOnLongClickListener(v -> {
            listener.onInfoClick(item);
            return true;
        });
        
        h.b.btnOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Описание блюда");
            popup.getMenu().add("Изменить количество");
            popup.getMenu().add("Удалить");
            popup.setOnMenuItemClickListener(mi -> {
                String title = mi.getTitle().toString();
                if (title.equals("Удалить")) {
                    listener.onDelete(item);
                } else if (title.equals("Описание блюда")) {
                    listener.onInfoClick(item);
                } else {
                    listener.onQtyChange(item, item.quantity + 1);
                }
                return true;
            });
            popup.show();
        });
    }

    // --- OVERRIDE TO PROCESS RAW LIST INTO WRAPPERS ---
    public void submitOrderItems(List<OrderItem> rawList) {
        if (rawList == null) {
            submitList(null);
            return;
        }

        // 1. Sort the raw list correctly
        List<OrderItem> sorted = new ArrayList<>(rawList);
        Collections.sort(sorted, (a, b) -> {
            if (a.guestNumber != b.guestNumber) return a.guestNumber - b.guestNumber;
            int pA = getCategoryPriority(a.menuItemSection);
            int pB = getCategoryPriority(b.menuItemSection);
            if (pA != pB) return pA - pB;
            return Long.compare(a.addedAt, b.addedAt);
        });

        // 2. Generate wrappers with header flags
        List<ItemWrapper> wrappers = new ArrayList<>();
        String lastGuestName = null;
        int lastGuestNum = -1;
        String lastSection = null;

        for (OrderItem item : sorted) {
            boolean showGuest = (item.guestNumber != lastGuestNum);
            
            // Category header shows if guest changed OR section changed within same guest
            boolean showSection = showGuest || !Objects.equals(normalize(item.menuItemSection), normalize(lastSection));

            String guestName = (currentOrder != null) ? currentOrder.getGuestName(item.guestNumber) : "Гость " + item.guestNumber;
            String guestLabel = guestName.replace("Гость", "").trim() + " ГОСТЬ";

            wrappers.add(new ItemWrapper(item, showGuest, showSection, guestLabel));

            lastGuestNum = item.guestNumber;
            lastSection = item.menuItemSection;
        }

        submitList(wrappers);
    }

    private String normalize(String s) {
        return (s == null) ? "" : s.trim().toLowerCase();
    }

    private int getCategoryPriority(String section) {
        if (section == null || section.isEmpty()) return 99;
        switch (section.trim()) {
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

    static class VH extends RecyclerView.ViewHolder {
        final ItemOrderItemBinding b;
        VH(ItemOrderItemBinding b) { super(b.getRoot()); this.b = b; }
    }
}
