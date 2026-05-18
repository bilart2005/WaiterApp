package com.waiter.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.waiter.app.database.entities.OrderItem;
import java.util.List;

@Dao
public interface OrderItemDao {

    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY addedAt")
    LiveData<List<OrderItem>> getItemsForOrder(long orderId);

    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY addedAt")
    List<OrderItem> getItemsForOrderSync(long orderId);

    @Query("SELECT SUM(quantity * menuItemPrice) FROM order_items WHERE orderId = :orderId")
    double getTotalForOrder(long orderId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(OrderItem item);

    @Update
    void update(OrderItem item);

    @Delete
    void delete(OrderItem item);

    @Query("DELETE FROM order_items WHERE id = :id")
    void deleteById(long id);

    @Query("UPDATE order_items SET quantity = :qty WHERE id = :id")
    void updateQuantity(long id, int qty);

    @Query("UPDATE order_items SET comment = :comment WHERE id = :id")
    void updateComment(long id, String comment);

    @Query("UPDATE order_items SET isServed = :served WHERE id = :id")
    void updateServedStatus(long id, boolean served);

    @Query("SELECT order_items.* FROM order_items JOIN orders ON order_items.orderId = orders.id WHERE orders.status = 'OPEN' ORDER BY orders.createdAt, order_items.addedAt")
    LiveData<List<OrderItem>> getAllItemsForOpenOrders();
}
