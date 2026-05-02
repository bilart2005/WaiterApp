package com.waiter.app.database;

import android.content.Context;
import androidx.room.*;
import com.waiter.app.database.dao.*;
import com.waiter.app.database.entities.*;

@Database(
    entities = {MenuItem.class, Order.class, OrderItem.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract MenuItemDao menuItemDao();
    public abstract OrderDao orderDao();
    public abstract OrderItemDao orderItemDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "waiter_db"
                    )
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(androidx.sqlite.db.SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            // Pre-populate with sample menu
                            new Thread(() -> {
                                AppDatabase database = AppDatabase.getInstance(context);
                                MenuItemDao dao = database.menuItemDao();
                                dao.insert(new MenuItem("Борщ украинский","Супы",320,"Наваристый суп со свёклой и говядиной","Глютен, Молоко",35));
                                dao.insert(new MenuItem("Куриный суп","Супы",280,"Лёгкий суп с лапшой","Глютен",25));
                                dao.insert(new MenuItem("Стейк рибай","Горячее",890,"Говяжий стейк средней прожарки","Нет",40));
                                dao.insert(new MenuItem("Паста карбонара","Горячее",450,"Спагетти с беконом и сливочным соусом","Глютен, Яйца, Молоко",20));
                                dao.insert(new MenuItem("Цезарь с курицей","Салаты",380,"Свежий салат с курицей, пармезаном и соусом цезарь","Глютен, Молоко, Яйца",10));
                                dao.insert(new MenuItem("Греческий салат","Салаты",320,"Овощи с оливками и фетой","Молоко",10));
                                dao.insert(new MenuItem("Лимонад домашний","Напитки",180,"Лимон, мята, имбирь","Нет",5));
                                dao.insert(new MenuItem("Капучино","Напитки",220,"Эспрессо с молочной пенкой","Молоко",7));
                                dao.insert(new MenuItem("Тирамису","Десерты",350,"Итальянский десерт с маскарпоне","Глютен, Молоко, Яйца",0));
                                dao.insert(new MenuItem("Чизкейк","Десерты",320,"Нью-йоркский чизкейк","Глютен, Молоко, Яйца",0));
                            }).start();
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
