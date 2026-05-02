package com.waiter.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.waiter.app.database.entities.MenuItem;
import java.util.List;

@Dao
public interface MenuItemDao {

    @Query("SELECT * FROM menu_items ORDER BY section, name")
    LiveData<List<MenuItem>> getAllItems();

    @Query("SELECT DISTINCT section FROM menu_items ORDER BY section")
    LiveData<List<String>> getAllSections();

    @Query("SELECT * FROM menu_items WHERE name LIKE :query || '%' OR name LIKE '% ' || :query || '%' ORDER BY name")
    LiveData<List<MenuItem>> searchByName(String query);

    @Query("SELECT * FROM menu_items WHERE section = :section ORDER BY name")
    LiveData<List<MenuItem>> getBySection(String section);

    @Query("SELECT * FROM menu_items WHERE id = :id")
    MenuItem getById(long id);

    @Insert
    long insert(MenuItem item);

    @Update
    void update(MenuItem item);

    @Delete
    void delete(MenuItem item);

    @Query("DELETE FROM menu_items WHERE id = :id")
    void deleteById(long id);
}
