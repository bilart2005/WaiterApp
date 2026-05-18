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
    public int guestCount;
    public double totalAmount;

    public String guestNames;
    public String tableNotes; // Added for Service tab notes

    public float posX; // X coordinate for layout
    public float posY; // Y coordinate for layout
    public int size;

    public Order() {
        this.size = 120;
    }

    public Order(int tableNumber) {
        this.tableNumber = tableNumber;
        this.createdAt = System.currentTimeMillis();
        this.closedAt = 0;
        this.status = "OPEN";
        this.guestCount = 1;
        this.totalAmount = 0;
        this.guestNames = "";
        this.posX = 100;
        this.posY = 100;
        this.size = 120;
    }

    public String getGuestName(int guestNumber) {
        if (guestNames == null || guestNames.isEmpty()) return "Гость " + guestNumber;
        String[] names = guestNames.split("\\|");
        if (guestNumber <= names.length && !names[guestNumber - 1].isEmpty()) {
            return names[guestNumber - 1];
        }
        return "Гость " + guestNumber;
    }
}
