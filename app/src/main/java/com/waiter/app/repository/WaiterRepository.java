package com.waiter.app.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.waiter.app.database.AppDatabase;
import com.waiter.app.database.dao.*;
import com.waiter.app.database.entities.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WaiterRepository {

    private final MenuItemDao menuItemDao;
    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public WaiterRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        menuItemDao = db.menuItemDao();
        orderDao = db.orderDao();
        orderItemDao = db.orderItemDao();
    }

    // ---- MENU ----
    public LiveData<List<MenuItem>> getAllMenuItems() { return menuItemDao.getAllItems(); }
    public LiveData<List<String>> getAllSections() { return menuItemDao.getAllSections(); }
    public LiveData<List<MenuItem>> searchMenuItems(String query) { return menuItemDao.searchByName(query); }
    public LiveData<List<MenuItem>> getMenuItemsBySection(String section) { return menuItemDao.getBySection(section); }

    public void insertMenuItem(MenuItem item) { executor.execute(() -> menuItemDao.insert(item)); }
    public void updateMenuItem(MenuItem item) { executor.execute(() -> menuItemDao.update(item)); }
    public void deleteMenuItem(MenuItem item) { executor.execute(() -> menuItemDao.delete(item)); }

    // ---- ORDERS ----
    public LiveData<List<Order>> getOpenOrders() { return orderDao.getOpenOrders(); }
    public LiveData<List<Order>> getAllOrders() { return orderDao.getAllOrders(); }
    public LiveData<List<Order>> getClosedOrders() { return orderDao.getClosedOrders(); }
    public LiveData<Order> getOpenOrderForTableLive(int table) { return orderDao.getOpenOrderForTableLive(table); }

    public void createOrderForTable(int tableNumber, Callback<Long> callback) {
        executor.execute(() -> {
            Order existing = orderDao.getOpenOrderForTable(tableNumber);
            if (existing != null) {
                callback.onResult(existing.id);
            } else {
                long id = orderDao.insert(new Order(tableNumber));
                callback.onResult(id);
            }
        });
    }

    public void closeOrder(long orderId) {
        executor.execute(() -> {
            double total = orderItemDao.getTotalForOrder(orderId);
            orderDao.closeOrder(orderId, System.currentTimeMillis(), total);
        });
    }

    // ---- ORDER ITEMS ----
    public LiveData<List<OrderItem>> getItemsForOrder(long orderId) { return orderItemDao.getItemsForOrder(orderId); }

    public void addItemToOrder(long orderId, MenuItem menuItem, int qty, String comment) {
        executor.execute(() -> {
            OrderItem item = new OrderItem(orderId, menuItem, qty, comment);
            orderItemDao.insert(item);
        });
    }

    public void updateOrderItemQuantity(long itemId, int qty) {
        executor.execute(() -> {
            if (qty <= 0) orderItemDao.deleteById(itemId);
            else orderItemDao.updateQuantity(itemId, qty);
        });
    }

    public void updateOrderItemComment(long itemId, String comment) {
        executor.execute(() -> orderItemDao.updateComment(itemId, comment));
    }

    public void deleteOrderItem(long itemId) {
        executor.execute(() -> orderItemDao.deleteById(itemId));
    }

    public interface Callback<T> {
        void onResult(T result);
    }
}
