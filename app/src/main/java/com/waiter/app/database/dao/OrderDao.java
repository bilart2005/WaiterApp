package com.waiter.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.waiter.app.database.entities.Order;
import com.waiter.app.database.entities.OrderWithItems;
import java.util.List;

@Dao
public interface OrderDao {

    @Query("SELECT * FROM orders WHERE status = 'OPEN' ORDER BY tableNumber")
    LiveData<List<Order>> getOpenOrders();

    @Query("SELECT *, (SELECT COUNT(*) FROM order_items WHERE orderId = orders.id) as itemCount FROM orders WHERE status = 'OPEN' ORDER BY tableNumber")
    LiveData<List<OrderWithItems>> getOpenOrdersWithItems();

    @Query("SELECT * FROM orders WHERE status = 'CLOSED' ORDER BY closedAt DESC")
    LiveData<List<Order>> getClosedOrders();

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    LiveData<List<Order>> getAllOrders();

    @Query("SELECT * FROM orders WHERE tableNumber = :table AND status = 'OPEN'")
    Order getOpenOrderForTable(int table);

    @Query("SELECT * FROM orders WHERE tableNumber = :table AND status = 'OPEN'")
    LiveData<Order> getOpenOrderForTableLive(int table);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Order order);

    @Transaction
    @Query("UPDATE orders SET status = 'CLOSED', closedAt = :closedAt, totalAmount = :total WHERE id = :orderId")
    void closeOrder(long orderId, long closedAt, double total);

    @Query("SELECT * FROM orders WHERE id = :id")
    Order getById(long id);

    @Query("SELECT * FROM orders WHERE id = :id")
    LiveData<Order> getByIdLive(long id);

    @Query("UPDATE orders SET guestCount = :count WHERE id = :id")
    void updateGuestCount(long id, int count);

    @Query("UPDATE orders SET guestNames = :names WHERE id = :id")
    void updateGuestNames(long id, String names);

    @Query("UPDATE orders SET tableNotes = :notes WHERE id = :id")
    void updateTableNotes(long id, String notes);

    @Query("UPDATE orders SET posX = :x, posY = :y WHERE id = :id")
    void updateTablePosition(long id, float x, float y);

    @Query("UPDATE orders SET size = :s WHERE id = :id")
    void updateTableSize(long id, int s);

    @Query("UPDATE orders SET tableNumber = :newNumber WHERE id = :id")
    void updateTableNumber(long id, int newNumber);

    @Delete
    void delete(Order order);

    @Query("DELETE FROM orders WHERE id = :id")
    void deleteById(long id);
}
