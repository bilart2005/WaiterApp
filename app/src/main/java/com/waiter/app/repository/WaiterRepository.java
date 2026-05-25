package com.waiter.app.repository;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.waiter.app.database.AppDatabase;
import com.waiter.app.database.dao.*;
import com.waiter.app.database.entities.*;
import com.waiter.app.database.model.DishStat;
import com.waiter.app.database.model.TableStat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WaiterRepository {

    private final MenuItemDao menuItemDao;
    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final TableDao tableDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public WaiterRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        menuItemDao = db.menuItemDao();
        orderDao = db.orderDao();
        orderItemDao = db.orderItemDao();
        tableDao = db.tableDao();

        executor.execute(() -> {
            try {
                if (menuItemDao.getCount() == 0) populateInitialData();
                if (tableDao.getCount() == 0) populateInitialTables();
            } catch (Exception e) {
                Log.e("WaiterRepository", "Error during initial population", e);
            }
        });
    }

    public void resetToDefaults(Runnable onDone) {
        executor.execute(() -> {
            try {
                menuItemDao.deleteAll();
                tableDao.deleteAll();
                populateInitialData();
                populateInitialTables();
                if (onDone != null) onDone.run();
            } catch (Exception e) {
                Log.e("WaiterRepository", "resetToDefaults error", e);
            }
        });
    }

    public void populateInitialTables() {
        if (tableDao.getCount() == 0) {
            tableDao.insert(new Table(1, 100, 100));
            tableDao.insert(new Table(2, 350, 100));
            tableDao.insert(new Table(3, 100, 350));
            tableDao.insert(new Table(4, 350, 350));
        }
    }

    public void populateInitialData() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem("Фриттата с пармезаном", "Основное", "Завтраки", 395, "Яичница запеченная с перцем и сыром", "", 15));
        items.add(new MenuItem("Омлет iL Патио", "Основное", "Завтраки", 495, "С грибами, томатами и фетой", "", 15));
        items.add(new MenuItem("Шакшука", "Основное", "Завтраки", 645, "Глазунья в остром соусе", "", 20));
        items.add(new MenuItem("Оливки к вину", "Основное", "Закуски", 250, "Оливки с косточкой", "", 3));
        items.add(new MenuItem("Утиный паштет", "Основное", "Закуски", 659, "С гренками и карамельным луком", "", 10));
        items.add(new MenuItem("Буррата с томатами", "Основное", "Закуски", 879, "С соусом песто", "", 10));
        items.add(new MenuItem("Минестроне", "Основное", "Супы", 519, "Итальянский овощной суп", "", 15));
        items.add(new MenuItem("Суп с белыми грибами", "Основное", "Супы", 569, "С паппарделле", "", 15));
        items.add(new MenuItem("Зуппа ди пеше", "Основное", "Супы", 859, "Морской суп с креветками", "", 20));
        items.add(new MenuItem("Цезарь с курицей", "Основное", "Салаты", 595, "Классический рецепт", "", 15));
        items.add(new MenuItem("Греческий салат", "Основное", "Салаты", 595, "С фетой и маслинами", "", 10));
        items.add(new MenuItem("Салат с лососем", "Основное", "Салаты", 780, "С авокадо и артишоками", "", 15));
        items.add(new MenuItem("Стейк рибай", "Основное", "Горячее", 1890, "Говяжий стейк", "", 25));
        items.add(new MenuItem("Паста карбонара", "Основное", "Горячее", 550, "С беконом и яйцом", "", 15));
        items.add(new MenuItem("Лазанья мясная", "Основное", "Горячее", 690, "Классическая", "", 20));
        items.add(new MenuItem("Тирамису", "Основное", "Десерты", 450, "Итальянская классика", "", 5));
        items.add(new MenuItem("Чизкейк", "Основное", "Десерты", 390, "С малиновым соусом", "", 5));
        items.add(new MenuItem("Лимонад домашний", "Основное", "Напитки", 220, "Мята и лимон", "", 5));
        items.add(new MenuItem("Капучино", "Основное", "Напитки", 239, "200 мл", "", 7));
        items.add(new MenuItem("Детские пельмешки", "Детское", "Горячее", 250, "Мини-пельмени со сметаной", "", 15));
        items.add(new MenuItem("Наггетсы с фри", "Детское", "Закуски", 320, "Куриные кусочки", "", 12));
        items.add(new MenuItem("Детский супчик", "Детское", "Супы", 180, "С буковками", "", 15));
        items.add(new MenuItem("Сок яблочный", "Детское", "Напитки", 120, "0.2л", "", 1));
        for (MenuItem item : items) {
            try { menuItemDao.insert(item); } catch (Exception e) { Log.e("Repo", "Populate error", e); }
        }
    }

    // ---- TABLES ----
    public LiveData<List<Table>> getAllTables() { return tableDao.getAllLive(); }
    public void insertTable(Table table) { executor.execute(() -> { try { tableDao.insert(table); } catch (Exception e) { Log.e("Repo", "insertTable", e); } }); }
    public void deleteTable(Table table) { executor.execute(() -> { try { tableDao.delete(table); } catch (Exception e) { Log.e("Repo", "deleteTable", e); } }); }
    public void updateTablePosition(long id, float x, float y) { executor.execute(() -> { try { tableDao.updatePosition(id, x, y); } catch (Exception e) { Log.e("Repo", "updatePos", e); } }); }
    public void updateTableSize(long id, int size) { executor.execute(() -> { try { tableDao.updateSize(id, size); } catch (Exception e) { Log.e("Repo", "updateSize", e); } }); }
    public void updateTableNumber(long id, int num) { executor.execute(() -> { try { tableDao.updateNumber(id, num); } catch (Exception e) { Log.e("Repo", "updateNum", e); } }); }

    // ---- MENU ----
    public LiveData<List<MenuItem>> getAllMenuItems() { return menuItemDao.getAllItems(); }
    public LiveData<List<String>> getAllSections() { return menuItemDao.getAllSections(); }
    public LiveData<List<String>> getAllMenuTypes() { return menuItemDao.getAllMenuTypes(); }
    public LiveData<List<MenuItem>> searchMenuItems(String query) { return menuItemDao.searchByName(query); }
    public LiveData<List<MenuItem>> getMenuItemsBySection(String section) { return menuItemDao.getBySection(section); }
    public LiveData<List<MenuItem>> getMenuItemsByMenuType(String type) { return menuItemDao.getByMenuType(type); }
    public LiveData<List<MenuItem>> getMenuItems(String type, String section) { return menuItemDao.getByTypeAndSection(type, section); }
    public void insertMenuItem(MenuItem item) { executor.execute(() -> { try { menuItemDao.insert(item); } catch (Exception e) { Log.e("Repo", "insertMenu", e); } }); }
    public void updateMenuItem(MenuItem item) { executor.execute(() -> { try { menuItemDao.update(item); } catch (Exception e) { Log.e("Repo", "updateMenu", e); } }); }
    public void deleteMenuItem(MenuItem item) { executor.execute(() -> { try { menuItemDao.delete(item); } catch (Exception e) { Log.e("Repo", "deleteMenu", e); } }); }

    // ---- EXPORT / IMPORT ----
    public void exportMenuToJson(Callback<String> cb) {
        executor.execute(() -> {
            try {
                List<MenuItem> all = menuItemDao.getAllItemsSync();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < all.size(); i++) {
                    MenuItem m = all.get(i);
                    sb.append(String.format(Locale.US, "{\"n\":\"%s\",\"t\":\"%s\",\"s\":\"%s\",\"p\":%.2f,\"d\":\"%s\",\"a\":\"%s\",\"m\":%d}",
                            m.name, m.menuType, m.section, m.price, m.description != null ? m.description : "", m.allergens != null ? m.allergens : "", m.cookingTimeMinutes));
                    if (i < all.size() - 1) sb.append(",");
                }
                sb.append("]");
                cb.onResult(sb.toString());
            } catch (Exception e) { cb.onResult(null); }
        });
    }

    public void importMenuFromJson(String json, Callback<Boolean> cb) {
        executor.execute(() -> {
            try {
                if (json == null || !json.startsWith("[")) { cb.onResult(false); return; }
                menuItemDao.deleteAll();
                tableDao.deleteAll();
                String body = json.substring(2, json.length() - 2);
                String[] parts = body.split("\\},\\{");
                for (String p : parts) {
                    Map<String, String> map = new HashMap<>();
                    String[] fields = p.split(",");
                    for (String f : fields) {
                        String[] kv = f.split(":");
                        String key = kv[0].replace("\"", "");
                        String val = kv[1].replace("\"", "");
                        map.put(key, val);
                    }
                    MenuItem m = new MenuItem(map.get("n"), map.get("t"), map.get("s"),
                            Double.parseDouble(map.get("p")), map.get("d"), map.get("a"),
                            Integer.parseInt(map.get("m")));
                    menuItemDao.insert(m);
                }
                cb.onResult(true);
            } catch (Exception e) { cb.onResult(false); }
        });
    }

    // ---- ORDERS ----
    public LiveData<List<Order>> getOpenOrders() { return orderDao.getOpenOrders(); }
    public LiveData<List<OrderWithItems>> getOpenOrdersWithItems() { return orderDao.getOpenOrdersWithItems(); }
    public LiveData<List<Order>> getAllOrders() { return orderDao.getAllOrders(); }
    public LiveData<List<Order>> getClosedOrders() { return orderDao.getClosedOrders(); }
    public LiveData<Order> getOpenOrderForTableLive(int table) { return orderDao.getOpenOrderForTableLive(table); }

    public void createOrderForTable(int tableNumber, Callback<Long> callback) {
        executor.execute(() -> {
            try {
                Order existing = orderDao.getOpenOrderForTable(tableNumber);
                if (existing != null) { callback.onResult(existing.id); }
                else { long id = orderDao.insert(new Order(tableNumber)); callback.onResult(id); }
            } catch (Exception e) { Log.e("Repo", "createOrder", e); }
        });
    }

    public void closeOrder(long orderId) {
        executor.execute(() -> {
            try {
                double total = orderItemDao.getTotalForOrder(orderId);
                orderDao.closeOrder(orderId, System.currentTimeMillis(), total);
            } catch (Exception e) { Log.e("Repo", "closeOrder", e); }
        });
    }

    public void updateGuestCount(long orderId, int count) { executor.execute(() -> { try { orderDao.updateGuestCount(orderId, count); } catch (Exception e) { Log.e("Repo", "updateGuestCount", e); } }); }
    public void updateGuestNames(long orderId, String names) { executor.execute(() -> { try { orderDao.updateGuestNames(orderId, names); } catch (Exception e) { Log.e("Repo", "updateGuestNames", e); } }); }
    public void updateTableNotes(long orderId, String notes) { executor.execute(() -> { try { orderDao.updateTableNotes(orderId, notes); } catch (Exception e) { Log.e("Repo", "updateTableNotes", e); } }); }
    public void deleteOrder(long orderId) { executor.execute(() -> { try { orderDao.deleteById(orderId); } catch (Exception e) { Log.e("Repo", "deleteOrder", e); } }); }
    public LiveData<Order> getOrderByIdLive(long id) { return orderDao.getByIdLive(id); }

    // ---- ORDER ITEMS ----
    public LiveData<List<OrderItem>> getItemsForOrder(long orderId) { return orderItemDao.getItemsForOrder(orderId); }
    public LiveData<List<OrderItem>> getAllItemsForOpenOrders() { return orderItemDao.getAllItemsForOpenOrders(); }

    public void addItemToOrder(long orderId, MenuItem menuItem, int qty, String comment, int guestNumber) {
        executor.execute(() -> {
            try { orderItemDao.insert(new OrderItem(orderId, menuItem, qty, comment, guestNumber)); }
            catch (Exception e) { Log.e("Repo", "addItem", e); }
        });
    }

    public void addCustomItemToOrder(long orderId, String name, double price, int qty, String comment, int guestNumber) {
        executor.execute(() -> {
            try {
                OrderItem item = new OrderItem();
                item.orderId = orderId; item.guestNumber = guestNumber; item.menuItemName = name;
                item.menuItemPrice = price; item.quantity = qty; item.comment = comment;
                orderItemDao.insert(item);
            } catch (Exception e) { Log.e("Repo", "addCustom", e); }
        });
    }

    public void updateOrderItemQuantity(long itemId, int qty) {
        executor.execute(() -> {
            try {
                if (qty <= 0) orderItemDao.deleteById(itemId);
                else orderItemDao.updateQuantity(itemId, qty);
            } catch (Exception e) { Log.e("Repo", "updateQty", e); }
        });
    }

    public void updateOrderItemComment(long itemId, String comment) { executor.execute(() -> { try { orderItemDao.updateComment(itemId, comment); } catch (Exception e) { Log.e("Repo", "updateComment", e); } }); }
    public void updateServedStatus(long itemId, boolean served) { executor.execute(() -> { try { orderItemDao.updateServedStatus(itemId, served); } catch (Exception e) { Log.e("Repo", "updateServed", e); } }); }
    public void deleteOrderItem(long itemId) { executor.execute(() -> { try { orderItemDao.deleteById(itemId); } catch (Exception e) { Log.e("Repo", "deleteItem", e); } }); }

    // ---- STATISTICS ----
    public void getStats(Callback<StatResult> cb) {
        executor.execute(() -> {
            try {
                StatResult r = new StatResult();
                r.totalRevenue = orderDao.getTotalRevenue();
                long todayStart = getTodayStartMillis();
                r.todayRevenue = orderDao.getRevenueFrom(todayStart);
                r.openOrdersCount = orderDao.getOpenOrdersCount();
                r.closedOrdersCount = orderDao.getClosedOrdersCount();
                r.menuCount = menuItemDao.getCount();
                r.tablesCount = tableDao.getCount();
                long avgMs = orderDao.getAvgOrderDurationMs();
                r.avgOrderMinutes = (int)(avgMs / 60000);
                r.topDishes = orderDao.getTopDishes();
                r.tablePopularity = orderDao.getTablePopularity();
                r.staleOrders = orderDao.getStaleOpenOrdersCount(System.currentTimeMillis() - 86400000L);
                cb.onResult(r);
            } catch (Exception e) {
                Log.e("Repo", "getStats error", e);
                cb.onResult(null);
            }
        });
    }

    private long getTodayStartMillis() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static class StatResult {
        public double totalRevenue;
        public double todayRevenue;
        public int openOrdersCount;
        public int closedOrdersCount;
        public int menuCount;
        public int tablesCount;
        public int avgOrderMinutes;
        public int staleOrders;
        public List<DishStat> topDishes;
        public List<TableStat> tablePopularity;
    }

    public interface Callback<T> { void onResult(T result); }
}
