package com.waiter.app.adapter;

import static org.junit.Assert.*;
import com.waiter.app.database.entities.MenuItem;
import org.junit.Test;

public class MenuPickerAdapterTest {

    @Test
    public void areItemsTheSame_sameId_returnsTrue() {
        MenuItem item1 = new MenuItem("Pasta", "Main", 500, "", "", 15);
        item1.id = 1;
        MenuItem item2 = new MenuItem("Pizza", "Main", 600, "", "", 20);
        item2.id = 1;

        assertTrue(MenuPickerAdapter.DIFF.areItemsTheSame(item1, item2));
    }

    @Test
    public void areItemsTheSame_differentId_returnsFalse() {
        MenuItem item1 = new MenuItem("Pasta", "Main", 500, "", "", 15);
        item1.id = 1;
        MenuItem item2 = new MenuItem("Pasta", "Main", 500, "", "", 15);
        item2.id = 2;

        assertFalse(MenuPickerAdapter.DIFF.areItemsTheSame(item1, item2));
    }

    @Test
    public void areContentsTheSame_sameNameAndPrice_returnsTrue() {
        MenuItem item1 = new MenuItem("Pasta", "Main", 500, "Desc 1", "None", 15);
        item1.id = 1;
        MenuItem item2 = new MenuItem("Pasta", "Main", 500, "Desc 2", "Gluten", 20);
        item2.id = 1;

        // Note: areContentsTheSame only checks name and price in current implementation
        assertTrue(MenuPickerAdapter.DIFF.areContentsTheSame(item1, item2));
    }

    @Test
    public void areContentsTheSame_differentPrice_returnsFalse() {
        MenuItem item1 = new MenuItem("Pasta", "Main", 500, "", "", 15);
        MenuItem item2 = new MenuItem("Pasta", "Main", 550, "", "", 15);

        assertFalse(MenuPickerAdapter.DIFF.areContentsTheSame(item1, item2));
    }
}
