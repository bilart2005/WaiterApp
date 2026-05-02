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

    // Orders
    public final LiveData<List<Order>> openOrders;
    public final LiveData<List<Order>> allOrders;

    // Search
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    public final LiveData<List<MenuItem>> searchResults;

    // Current order context
    private final MutableLiveData<Long> currentOrderId = new MutableLiveData<>();
    private LiveData<List<OrderItem>> currentOrderItems;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repo = new WaiterRepository(application);
        allMenuItems = repo.getAllMenuItems();
        allSections = repo.getAllSections();
        openOrders = repo.getOpenOrders();
        allOrders = repo.getAllOrders();
        searchResults = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) return repo.getAllMenuItems();
            return repo.searchMenuItems(query.trim());
        });
    }

    public void setSearchQuery(String q) { searchQuery.setValue(q); }

    // Menu management
    public void insertMenuItem(MenuItem item) { repo.insertMenuItem(item); }
    public void updateMenuItem(MenuItem item) { repo.updateMenuItem(item); }
    public void deleteMenuItem(MenuItem item) { repo.deleteMenuItem(item); }
    public LiveData<List<MenuItem>> getMenuItemsBySection(String section) { return repo.getMenuItemsBySection(section); }

    // Order management
    public void openOrderForTable(int table, WaiterRepository.Callback<Long> cb) {
        repo.createOrderForTable(table, cb);
    }

    public void closeOrder(long orderId) { repo.closeOrder(orderId); }

    public LiveData<List<OrderItem>> getOrderItems(long orderId) {
        return repo.getItemsForOrder(orderId);
    }

    public void addItemToOrder(long orderId, MenuItem item, int qty, String comment) {
        repo.addItemToOrder(orderId, item, qty, comment);
    }

    public void updateQuantity(long itemId, int qty) { repo.updateOrderItemQuantity(itemId, qty); }
    public void updateComment(long itemId, String comment) { repo.updateOrderItemComment(itemId, comment); }
    public void deleteOrderItem(long itemId) { repo.deleteOrderItem(itemId); }

    public LiveData<Order> getOpenOrderForTable(int table) {
        return repo.getOpenOrderForTableLive(table);
    }

    public LiveData<List<Order>> getOpenOrders() { return openOrders; }
    public LiveData<List<Order>> getClosedOrders() { return repo.getClosedOrders(); }
}
