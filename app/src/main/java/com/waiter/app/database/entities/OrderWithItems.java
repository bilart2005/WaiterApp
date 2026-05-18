package com.waiter.app.database.entities;

import androidx.room.Embedded;

public class OrderWithItems {
    @Embedded
    public Order order;
    public int itemCount;
}
