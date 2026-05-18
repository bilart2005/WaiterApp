package com.waiter.app.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tables")
public class Table {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public int number;
    public float posX;
    public float posY;
    public int size;

    public Table() {
        this.size = 120;
    }

    public Table(int number, float posX, float posY) {
        this.number = number;
        this.posX = posX;
        this.posY = posY;
        this.size = 120;
    }
}
