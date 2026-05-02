package com.waiter.app.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "order_items",
    foreignKeys = {
        @ForeignKey(entity = Order.class, parentColumns = "id",
                    childColumns = "orderId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = MenuItem.class, parentColumns = "id",
                    childColumns = "menuItemId", onDelete = ForeignKey.SET_NULL)
    },
    indices = {@Index("orderId"), @Index("menuItemId")}
)
public class OrderItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long orderId;
    public Long menuItemId; // nullable — блюдо могло быть удалено из меню
    public String menuItemName; // snapshot имени на момент заказа
    public double menuItemPrice; // snapshot цены
    public int quantity;
    public String comment; // комментарий официанта
    public long addedAt;

    public OrderItem() {}

    public OrderItem(long orderId, MenuItem item, int quantity, String comment) {
        this.orderId = orderId;
        this.menuItemId = item.id;
        this.menuItemName = item.name;
        this.menuItemPrice = item.price;
        this.quantity = quantity;
        this.comment = comment;
        this.addedAt = System.currentTimeMillis();
    }
}
