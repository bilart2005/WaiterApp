package com.waiter.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.waiter.app.database.entities.*;
import com.waiter.app.repository.WaiterRepository;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final WaiterRepository repo;

    // Menu
    public final LiveData<List<MenuItem>> allMenuItems;
    public final LiveData<List<String>> allSections;
    public final LiveData<List<String>> allMenuTypes;

    // Orders
    public final LiveData<List<Order>> openOrders;
    public final LiveData<List<OrderWithItems>> openOrdersWithItems;
    public final LiveData<List<Order>> allOrders;

    // Tables
    public final LiveData<List<Table>> allTables;

    // Filtering
    private final MutableLiveData<String> filterType = new MutableLiveData<>("Основное");
    private final MutableLiveData<String> filterSection = new MutableLiveData<>("");
    public final LiveData<List<MenuItem>> filteredMenuItems;

    // Search
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    public final LiveData<List<MenuItem>> searchResults;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repo = new WaiterRepository(application);
        allMenuItems = repo.getAllMenuItems();
        allSections = repo.getAllSections();
        allMenuTypes = repo.getAllMenuTypes();
        openOrders = repo.getOpenOrders();
        openOrdersWithItems = repo.getOpenOrdersWithItems();
        allOrders = repo.getAllOrders();
        allTables = repo.getAllTables();
        
        searchResults = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) return repo.getAllMenuItems();
            return repo.searchMenuItems(query.trim());
        });

        // Combined filter
        LiveData<Pair<String, String>> filters = new MediatorLiveData<Pair<String, String>>() {{
            addSource(filterType, t -> setValue(new Pair<>(t, filterSection.getValue())));
            addSource(filterSection, s -> setValue(new Pair<>(filterType.getValue(), s)));
        }};

        filteredMenuItems = Transformations.switchMap(filters, pair -> {
            String type = pair.first;
            String section = pair.second;
            if (type == null || type.isEmpty()) return repo.getMenuItemsBySection(section);
            if (section == null || section.isEmpty()) return repo.getMenuItemsByMenuType(type);
            return repo.getMenuItems(type, section);
        });
    }

    public void setMenuFilter(String type, String section) {
        filterType.setValue(type);
        filterSection.setValue(section);
    }

    public void setSearchQuery(String q) { searchQuery.setValue(q); }

    // Helper class for Pair (since it's not standard in some Java versions, or use android.util.Pair)
    private static class Pair<A, B> {
        public final A first;
        public final B second;
        public Pair(A a, B b) { this.first = a; this.second = b; }
    }

    // Tables management
    public void insertTable(Table table) { repo.insertTable(table); }
    public void deleteTable(Table table) { repo.deleteTable(table); }
    public void updateTablePosition(long id, float x, float y) { repo.updateTablePosition(id, x, y); }
    public void updateTableSize(long id, int size) { repo.updateTableSize(id, size); }
    public void updateTableNumber(long id, int num) { repo.updateTableNumber(id, num); }

    // Menu management
    public void insertMenuItem(MenuItem item) { repo.insertMenuItem(item); }
    public void updateMenuItem(MenuItem item) { repo.updateMenuItem(item); }
    public void deleteMenuItem(MenuItem item) { repo.deleteMenuItem(item); }
    public LiveData<List<MenuItem>> getMenuItemsBySection(String section) { return repo.getMenuItemsBySection(section); }
    public LiveData<List<MenuItem>> getMenuItems(String type, String section) {
        if (type == null || type.isEmpty()) return repo.getMenuItemsBySection(section);
        if (section == null || section.isEmpty()) return repo.getMenuItemsByMenuType(type);
        return repo.getMenuItems(type, section);
    }

    // Order management
    public void openOrderForTable(int table, WaiterRepository.Callback<Long> cb) {
        repo.createOrderForTable(table, cb);
    }

    public void closeOrder(long orderId) { repo.closeOrder(orderId); }
    public void deleteOrder(long orderId) { repo.deleteOrder(orderId); }

    public LiveData<List<OrderItem>> getOrderItems(long orderId) {
        return repo.getItemsForOrder(orderId);
    }
    public LiveData<List<OrderItem>> getAllOpenOrderItems() {
        return repo.getAllItemsForOpenOrders();
    }

    public void addItemToOrder(long orderId, MenuItem item, int qty, String comment, int guestNumber) {
        repo.addItemToOrder(orderId, item, qty, comment, guestNumber);
    }

    public void addCustomItemToOrder(long orderId, String name, double price, int qty, String comment, int guestNumber) {
        repo.addCustomItemToOrder(orderId, name, price, qty, comment, guestNumber);
    }

    public void updateQuantity(long itemId, int qty) { repo.updateOrderItemQuantity(itemId, qty); }
    public void updateComment(long itemId, String comment) { repo.updateOrderItemComment(itemId, comment); }
    public void updateServedStatus(long itemId, boolean served) { repo.updateServedStatus(itemId, served); }
    public void deleteOrderItem(long itemId) { repo.deleteOrderItem(itemId); }

    public void updateGuestCount(long orderId, int count) { repo.updateGuestCount(orderId, count); }

    public LiveData<Order> getOrderById(long id) { return repo.getOrderByIdLive(id); }

    public void updateGuestNames(long orderId, String names) { repo.updateGuestNames(orderId, names); }
    public void updateTableNotes(long orderId, String notes) { repo.updateTableNotes(orderId, notes); }

    public void exportData(WaiterRepository.Callback<String> cb) { repo.exportMenuToJson(cb); }
    public void importData(String json, WaiterRepository.Callback<Boolean> cb) { repo.importMenuFromJson(json, cb); }

    public LiveData<Order> getOpenOrderForTable(int table) {
        return repo.getOpenOrderForTableLive(table);
    }

    public LiveData<List<Order>> getClosedOrders() { return repo.getClosedOrders(); }
}
