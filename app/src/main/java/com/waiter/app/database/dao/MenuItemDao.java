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

    @Query("SELECT DISTINCT menuType FROM menu_items WHERE menuType IS NOT NULL AND menuType != '' ORDER BY menuType")
    LiveData<List<String>> getAllMenuTypes();

    @Query("SELECT * FROM menu_items WHERE name LIKE :query || '%' OR name LIKE '% ' || :query || '%' ORDER BY name")
    LiveData<List<MenuItem>> searchByName(String query);

    @Query("SELECT * FROM menu_items WHERE section = :section ORDER BY name")
    LiveData<List<MenuItem>> getBySection(String section);

    @Query("SELECT * FROM menu_items WHERE menuType = :type AND section = :section ORDER BY name")
    LiveData<List<MenuItem>> getByTypeAndSection(String type, String section);

    @Query("SELECT * FROM menu_items WHERE menuType = :type ORDER BY name")
    LiveData<List<MenuItem>> getByMenuType(String type);

    @Query("SELECT COUNT(*) FROM menu_items")
    int getCount();

    @Query("SELECT * FROM menu_items WHERE id = :id")
    MenuItem getById(long id);

    @Query("SELECT * FROM menu_items ORDER BY menuType, section, name")
    List<MenuItem> getAllItemsSync();

    @Query("DELETE FROM menu_items")
    void deleteAll();

    @Insert
    long insert(MenuItem item);

    @Update
    void update(MenuItem item);

    @Delete
    void delete(MenuItem item);

    @Query("DELETE FROM menu_items WHERE id = :id")
    void deleteById(long id);
}
