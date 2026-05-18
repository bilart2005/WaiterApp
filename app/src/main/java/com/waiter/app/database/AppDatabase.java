package com.waiter.app.database;

import android.content.Context;
import androidx.room.*;
import com.waiter.app.database.dao.*;
import com.waiter.app.database.entities.*;

@Database(
    entities = {MenuItem.class, Order.class, OrderItem.class, Table.class},
    version = 3,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract MenuItemDao menuItemDao();
    public abstract OrderDao orderDao();
    public abstract OrderItemDao orderItemDao();
    public abstract TableDao tableDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "waiter_db"
                    )
                    .fallbackToDestructiveMigration()
                    // Enable Write-Ahead Logging for better crash resilience
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
