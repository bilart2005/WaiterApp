package com.waiter.app.ui.diagnostics;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.waiter.app.R;
import com.waiter.app.database.AppDatabase;
import com.waiter.app.database.dao.MenuItemDao;
import com.waiter.app.database.dao.OrderDao;
import com.waiter.app.database.dao.TableDao;
import com.waiter.app.repository.WaiterRepository;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DiagnosticsFragment extends Fragment {

    private TextView tvLog;
    private Button btnDiag, btnExport, btnReset;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String lastDiagResult = "Диагностика не запускалась";
    private WaiterRepository repo;
    private AppDatabase db;

    // Сохраняем context заранее — НЕ вызываем requireActivity() внутри потоков
    private Context appContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context.getApplicationContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diagnostics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repo = new WaiterRepository((Application) appContext);
        db = AppDatabase.getInstance(appContext);

        tvLog = view.findViewById(R.id.tv_diag_log);
        btnDiag = view.findViewById(R.id.btn_run_diag);
        btnExport = view.findViewById(R.id.btn_export_report);
        btnReset = view.findViewById(R.id.btn_reset_data);

        btnDiag.setOnClickListener(v -> runDiagnostics());
        btnExport.setOnClickListener(v -> exportReport());
        btnReset.setOnClickListener(v -> confirmReset());
    }

    private void runDiagnostics() {
        tvLog.setText("⏳ Запуск диагностики...\n");
        btnDiag.setEnabled(false);
        final StringBuilder log = new StringBuilder();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MenuItemDao menuDao = db.menuItemDao();
                OrderDao orderDao = db.orderDao();
                TableDao tableDao = db.tableDao();

                // Тест 1
                appendLog(log, "Тест 1: Доступность базы данных... ");
                int menuCount = menuDao.getCount();
                appendLog(log, "✓ OK (позиций в меню: " + menuCount + ")\n");

                // Тест 2
                appendLog(log, "Тест 2: Меню не пустое... ");
                if (menuCount > 0) appendLog(log, "✓ OK\n");
                else appendLog(log, "✗ ВНИМАНИЕ: меню пустое!\n");

                // Тест 3
                appendLog(log, "Тест 3: Столики настроены... ");
                int tableCount = tableDao.getCount();
                if (tableCount > 0) appendLog(log, "✓ OK (столиков: " + tableCount + ")\n");
                else appendLog(log, "✗ ВНИМАНИЕ: нет столиков!\n");

                // Тест 4
                appendLog(log, "Тест 4: Зависшие заказы (>24ч)... ");
                long threshold = System.currentTimeMillis() - 86400000L;
                int stale = orderDao.getStaleOpenOrdersCount(threshold);
                if (stale == 0) appendLog(log, "✓ OK\n");
                else appendLog(log, "✗ НАЙДЕНО: " + stale + " заказ(ов)\n");

                // Тест 5
                appendLog(log, "Тест 5: Открытые заказы... ");
                int open = orderDao.getOpenOrdersCount();
                appendLog(log, "✓ OK (открыто: " + open + ")\n");

                appendLog(log, "\n--- Диагностика завершена ---");
                lastDiagResult = log.toString();

                mainHandler.post(() -> {
                    if (isAdded()) btnDiag.setEnabled(true);
                });

            } catch (Exception e) {
                lastDiagResult = "Ошибка: " + e.getMessage();
                mainHandler.post(() -> {
                    if (isAdded()) {
                        tvLog.setText("✗ Критическая ошибка: " + e.getMessage());
                        btnDiag.setEnabled(true);
                    }
                });
            }
        });
    }

    private void appendLog(StringBuilder sb, String text) {
        sb.append(text);
        mainHandler.post(() -> {
            if (isAdded()) tvLog.setText(sb.toString());
        });
        try { Thread.sleep(120); } catch (InterruptedException ignored) {}
    }

    private void exportReport() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                int menuCount = db.menuItemDao().getCount();
                int tableCount = db.tableDao().getCount();
                int openCount = db.orderDao().getOpenOrdersCount();
                int closedCount = db.orderDao().getClosedOrdersCount();
                double revenue = db.orderDao().getTotalRevenue();

                String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                String content = "=== ТЕХНИЧЕСКИЙ ОТЧЁТ WaiterApp ===\n" +
                        "Дата генерации: " + date + "\n\n" +
                        "--- ДАННЫЕ СИСТЕМЫ ---\n" +
                        "Позиций в меню:      " + menuCount + "\n" +
                        "Количество столиков: " + tableCount + "\n" +
                        "Открытых заказов:    " + openCount + "\n" +
                        "Закрытых заказов:    " + closedCount + "\n" +
                        "Общая выручка:       " + String.format(Locale.getDefault(), "%.2f руб.", revenue) + "\n\n" +
                        "--- РЕЗУЛЬТАТ ПОСЛЕДНЕЙ ДИАГНОСТИКИ ---\n" +
                        lastDiagResult + "\n";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, "waiter_report.txt");
                    values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    Uri uri = appContext.getContentResolver().insert(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        try (OutputStream os = appContext.getContentResolver().openOutputStream(uri)) {
                            if (os != null) os.write(content.getBytes("UTF-8"));
                        }
                    }
                } else {
                    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!dir.exists()) dir.mkdirs();
                    File file = new File(dir, "waiter_report.txt");
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(content.getBytes("UTF-8"));
                    }
                }

                mainHandler.post(() -> {
                    if (isAdded())
                        Toast.makeText(appContext, "Отчёт сохранён в Downloads/waiter_report.txt", Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (isAdded())
                        Toast.makeText(appContext, "Ошибка экспорта: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void confirmReset() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Сброс данных")
                .setMessage("Меню и столики будут пересозданы с начальными данными. Заказы не удаляются. Продолжить?")
                .setPositiveButton("Да, сбросить", (d, w) ->
                        repo.resetToDefaults(() -> mainHandler.post(() -> {
                            if (isAdded())
                                Toast.makeText(appContext, "Данные сброшены", Toast.LENGTH_SHORT).show();
                        }))
                )
                .setNegativeButton("Отмена", null)
                .show();
    }
}
