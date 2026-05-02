package com.waiter.app.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "menu_items")
public class MenuItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String section;
    public double price;
    public String description;
    public String allergens;
    public int cookingTimeMinutes;
    public String photoPath; // local file path or null
    public boolean available;

    public MenuItem() {}

    public MenuItem(String name, String section, double price, String description,
                    String allergens, int cookingTimeMinutes) {
        this.name = name;
        this.section = section;
        this.price = price;
        this.description = description;
        this.allergens = allergens;
        this.cookingTimeMinutes = cookingTimeMinutes;
        this.available = true;
    }
}
