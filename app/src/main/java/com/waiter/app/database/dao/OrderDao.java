package com.waiter.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.waiter.app.database.entities.Order;
import java.util.List;

@Dao
public interface OrderDao {

    @Query("SELECT * FROM orders WHERE status = 'OPEN' ORDER BY tableNumber")
    LiveData<List<Order>> getOpenOrders();

    @Query("SELECT * FROM orders WHERE status = 'CLOSED' ORDER BY closedAt DESC")
    LiveData<List<Order>> getClosedOrders();

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    LiveData<List<Order>> getAllOrders();

    @Query("SELECT * FROM orders WHERE tableNumber = :table AND status = 'OPEN'")
    Order getOpenOrderForTable(int table);

    @Query("SELECT * FROM orders WHERE tableNumber = :table AND status = 'OPEN'")
    LiveData<Order> getOpenOrderForTableLive(int table);

    @Insert
    long insert(Order order);

    @Query("UPDATE orders SET status = 'CLOSED', closedAt = :closedAt, totalAmount = :total WHERE id = :orderId")
    void closeOrder(long orderId, long closedAt, double total);

    @Query("SELECT * FROM orders WHERE id = :id")
    Order getById(long id);
}