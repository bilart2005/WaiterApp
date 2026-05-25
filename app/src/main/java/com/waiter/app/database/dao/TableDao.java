package com.waiter.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.waiter.app.database.entities.Table;
import java.util.List;

@Dao
public interface TableDao {
    @Query("SELECT * FROM tables ORDER BY number")
    LiveData<List<Table>> getAllLive();

    @Insert
    long insert(Table table);

    @Update
    void update(Table table);

    @Delete
    void delete(Table table);

    @Query("SELECT COUNT(*) FROM tables")
    int getCount();

    @Query("UPDATE tables SET posX = :x, posY = :y WHERE id = :id")
    void updatePosition(long id, float x, float y);

    @Query("UPDATE tables SET size = :s WHERE id = :id")
    void updateSize(long id, int s);

    @Query("UPDATE tables SET number = :num WHERE id = :id")
    void updateNumber(long id, int num);

    @Query("DELETE FROM tables")
    void deleteAll();
}
