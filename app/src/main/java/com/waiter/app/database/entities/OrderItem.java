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
    public int guestNumber; // к какому гостю относится
    public Long menuItemId; // nullable — блюдо могло быть удалено из меню
    public String menuItemName; // snapshot имени на момент заказа
    public double menuItemPrice; // snapshot цены
    public String menuItemSection; // snapshot категории
    public int quantity;
    public String comment; // комментарий официанта
    public boolean isServed; // отдано ли блюдо
    public long addedAt;

    public OrderItem() {
        this.guestNumber = 1;
        this.quantity = 1;
        this.isServed = false;
        this.addedAt = System.currentTimeMillis();
    }

    public OrderItem(long orderId, MenuItem item, int quantity, String comment) {
        this(orderId, item, quantity, comment, 1);
    }

    public OrderItem(long orderId, MenuItem item, int quantity, String comment, int guestNumber) {
        this.orderId = orderId;
        this.guestNumber = guestNumber;
        this.menuItemId = item.id;
        this.menuItemName = item.name;
        this.menuItemPrice = item.price;
        this.menuItemSection = item.section;
        this.quantity = quantity;
        this.comment = comment;
        this.isServed = false;
        this.addedAt = System.currentTimeMillis();
    }
}
