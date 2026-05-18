package com.waiter.app.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "menu_items")
public class MenuItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String menuType; // "Основное", "Детское", "Завтрак"
    public String section;  // "Напитки", "Закуски", "Салаты", "Горячее", "Паста", "Пицца", "Десерты"
    public double price;
    public String description;
    public String allergens;
    public int cookingTimeMinutes;
    public String photoPath;

    public MenuItem() {}

    public MenuItem(String name, String menuType, String section, double price, String description, String allergens, int time) {
        this.name = name;
        this.menuType = menuType;
        this.section = section;
        this.price = price;
        this.description = description;
        this.allergens = allergens;
        this.cookingTimeMinutes = time;
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
