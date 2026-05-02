package com.waiter.app.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "orders")
public class Order {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public int tableNumber;
    public long createdAt;
    public long closedAt; // 0 = still open
    public String status; // "OPEN", "CLOSED"
    public double totalAmount;

    public Order() {}

    public Order(int tableNumber) {
        this.tableNumber = tableNumber;
        this.createdAt = System.currentTimeMillis();
        this.closedAt = 0;
        this.status = "OPEN";
        this.totalAmount = 0;
    }
}
