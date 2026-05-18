package com.waiter.app.adapter;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import com.waiter.app.R;
import com.waiter.app.database.entities.Order;
import com.waiter.app.database.entities.OrderItem;
import com.waiter.app.databinding.ItemServiceGroupBinding;
import com.waiter.app.databinding.ItemServiceItemBinding;
import java.util.*;

public class ServiceAdapter extends ListAdapter<ServiceAdapter.ServiceDisplayItem, RecyclerView.ViewHolder> {

    public interface ServiceListener {
        void onServedStatusChange(long itemId, boolean served);
        void onAddNote(long orderId);
        void onInfoClick(OrderItem item);
    }

    public static class ServiceDisplayItem {
        public final Order order;
        public final OrderItem item;
        public final boolean isHeader;
        public final boolean showGuestHeader;
        public final boolean showCategoryHeader;
        public final String guestLabel;

        private ServiceDisplayItem(Order order, OrderItem item, boolean isHeader, boolean showGuestHeader, boolean showCategoryHeader, String guestLabel) {
            this.order = order;
            this.item = item;
            this.isHeader = isHeader;
            this.showGuestHeader = showGuestHeader;
            this.showCategoryHeader = showCategoryHeader;
            this.guestLabel = guestLabel;
        }

        public static ServiceDisplayItem forGroupHeader(Order order) { 
            return new ServiceDisplayItem(order, null, true, false, false, null); 
        }
        public static ServiceDisplayItem forItem(OrderItem item, boolean showGuest, boolean showCat, String label) { 
            return new ServiceDisplayItem(null, item, false, showGuest, showCat, label); 
        }
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final ServiceListener listener;

    public ServiceAdapter(ServiceListener listener) {
        super(new DiffUtil.ItemCallback<ServiceDisplayItem>() {
            @Override public boolean areItemsTheSame(@NonNull ServiceDisplayItem a, @NonNull ServiceDisplayItem b) {
                if (a.isHeader && b.isHeader) return a.order.id == b.order.id;
                if (!a.isHeader && !b.isHeader) return a.item.id == b.item.id;
                return false;
            }
            @Override public boolean areContentsTheSame(@NonNull ServiceDisplayItem a, @NonNull ServiceDisplayItem b) {
                if (a.isHeader && b.isHeader) {
                    return Objects.equals(a.order.tableNotes, b.order.tableNotes) && a.order.tableNumber == b.order.tableNumber;
                }
                if (!a.isHeader && !b.isHeader) {
                    return a.item.isServed == b.item.isServed && a.item.quantity == b.item.quantity &&
                           a.showGuestHeader == b.showGuestHeader && a.showCategoryHeader == b.showCategoryHeader;
                }
                return false;
            }
        });
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isHeader ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderVH(ItemServiceGroupBinding.inflate(inflater, parent, false));
        }
        return new ItemVH(ItemServiceItemBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ServiceDisplayItem di = getItem(position);
        if (di.isHeader) {
            HeaderVH h = (HeaderVH) holder;
            h.b.tvTableName.setText("СТОЛ " + di.order.tableNumber);
            h.b.tvTableName.setTextColor(0xFF2E7D32);
            h.b.tvTableNotes.setText(di.order.tableNotes != null && !di.order.tableNotes.isEmpty() 
                    ? "📝 " + di.order.tableNotes : "Добавить заметку...");
            h.b.btnEditNote.setOnClickListener(v -> listener.onAddNote(di.order.id));
        } else {
            ItemVH h = (ItemVH) holder;
            OrderItem item = di.item;

            // Guest and Category grouping (same as Order screen)
            h.b.tvGuestHeader.setVisibility(di.showGuestHeader ? View.VISIBLE : View.GONE);
            if (di.showGuestHeader) h.b.tvGuestHeader.setText(di.guestLabel);

            h.b.tvCategoryHeader.setVisibility(di.showCategoryHeader ? View.VISIBLE : View.GONE);
            if (di.showCategoryHeader) h.b.tvCategoryHeader.setText(item.menuItemSection != null ? item.menuItemSection : "Прочее");

            // Item content (White text on dark cards)
            h.b.tvItemName.setText(item.menuItemName);
            h.b.tvItemName.setTextColor(0xFFFFFFFF);
            h.b.tvQty.setText("x" + item.quantity);
            h.b.tvQty.setTextColor(0xFFFFFFFF);
            h.b.cbServed.setChecked(item.isServed);
            
            h.b.cbServed.setOnClickListener(v -> listener.onServedStatusChange(item.id, h.b.cbServed.isChecked()));
            
            h.itemView.setOnLongClickListener(v -> {
                listener.onInfoClick(item);
                return true;
            });
        }
    }

    public void submitData(List<Order> openOrders, List<OrderItem> allItems) {
        if (openOrders == null || allItems == null) {
            submitList(null);
            return;
        }

        Map<Long, Order> orderMap = new HashMap<>();
        for (Order o : openOrders) orderMap.put(o.id, o);

        Map<Long, List<OrderItem>> groupedByOrder = new LinkedHashMap<>();
        for (OrderItem item : allItems) {
            if (!groupedByOrder.containsKey(item.orderId)) groupedByOrder.put(item.orderId, new ArrayList<>());
            groupedByOrder.get(item.orderId).add(item);
        }

        List<ServiceDisplayItem> displayList = new ArrayList<>();
        for (Map.Entry<Long, List<OrderItem>> entry : groupedByOrder.entrySet()) {
            Order order = orderMap.get(entry.getKey());
            if (order == null) continue;

            List<OrderItem> items = entry.getValue();
            boolean allServed = true;
            for (OrderItem item : items) if (!item.isServed) { allServed = false; break; }
            if (allServed) continue;

            // Header for Table
            displayList.add(ServiceDisplayItem.forGroupHeader(order));

            // Sort items within order: Guest > Category
            Collections.sort(items, (a, b) -> {
                if (a.guestNumber != b.guestNumber) return a.guestNumber - b.guestNumber;
                int pA = getCategoryPriority(a.menuItemSection);
                int pB = getCategoryPriority(b.menuItemSection);
                if (pA != pB) return pA - pB;
                return Long.compare(a.addedAt, b.addedAt);
            });

            int lastGuest = -1;
            String lastSection = null;
            for (OrderItem item : items) {
                boolean showGuest = (item.guestNumber != lastGuest);
                boolean showCat = showGuest || !Objects.equals(item.menuItemSection, lastSection);
                
                String guestName = order.getGuestName(item.guestNumber);
                String guestLabel = guestName.replace("Гость", "").trim() + " ГОСТЬ";

                displayList.add(ServiceDisplayItem.forItem(item, showGuest, showCat, guestLabel));
                
                lastGuest = item.guestNumber;
                lastSection = item.menuItemSection;
            }
        }
        submitList(displayList);
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

    static class HeaderVH extends RecyclerView.ViewHolder {
        final ItemServiceGroupBinding b;
        HeaderVH(ItemServiceGroupBinding b) { super(b.getRoot()); this.b = b; }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        final ItemServiceItemBinding b;
        ItemVH(ItemServiceItemBinding b) { super(b.getRoot()); this.b = b; }
    }
}
