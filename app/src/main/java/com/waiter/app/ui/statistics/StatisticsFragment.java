package com.waiter.app.ui.statistics;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.waiter.app.R;
import com.waiter.app.database.model.DishStat;
import com.waiter.app.database.model.TableStat;
import com.waiter.app.repository.WaiterRepository;

public class StatisticsFragment extends Fragment {

    private WaiterRepository repo;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Context appContext;

    private TextView tvTodayRevenue, tvTotalRevenue, tvOpenOrders, tvClosedOrders;
    private TextView tvAvgTime, tvTopDishes, tvTablePop;
    private View progressBar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context.getApplicationContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repo = new WaiterRepository((Application) appContext);

        tvTodayRevenue = view.findViewById(R.id.tv_today_revenue);
        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue);
        tvOpenOrders = view.findViewById(R.id.tv_open_orders);
        tvClosedOrders = view.findViewById(R.id.tv_closed_orders);
        tvAvgTime = view.findViewById(R.id.tv_avg_time);
        tvTopDishes = view.findViewById(R.id.tv_top_dishes);
        tvTablePop = view.findViewById(R.id.tv_table_popularity);
        progressBar = view.findViewById(R.id.progress_stats);

        view.findViewById(R.id.btn_refresh).setOnClickListener(v -> loadStats());
        loadStats();
    }

    private void loadStats() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        repo.getStats(result -> mainHandler.post(() -> {
            if (!isAdded()) return;
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (result == null) return;

            tvTodayRevenue.setText(String.format("%.0f руб.", result.todayRevenue));
            tvTotalRevenue.setText(String.format("%.0f руб.", result.totalRevenue));
            tvOpenOrders.setText(String.valueOf(result.openOrdersCount));
            tvClosedOrders.setText(String.valueOf(result.closedOrdersCount));
            tvAvgTime.setText(result.avgOrderMinutes > 0 ? result.avgOrderMinutes + " мин" : "—");

            if (result.topDishes != null && !result.topDishes.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                int rank = 1;
                for (DishStat ds : result.topDishes) {
                    sb.append(rank++).append(". ").append(ds.dishName)
                      .append(" — ").append(ds.totalOrdered).append(" шт.\n");
                }
                tvTopDishes.setText(sb.toString().trim());
            } else {
                tvTopDishes.setText("Нет данных (заказов ещё не было)");
            }

            if (result.tablePopularity != null && !result.tablePopularity.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (TableStat ts : result.tablePopularity) {
                    sb.append("Стол №").append(ts.tableNumber)
                      .append(" — ").append(ts.timesOccupied).append(" раз\n");
                }
                tvTablePop.setText(sb.toString().trim());
            } else {
                tvTablePop.setText("Нет данных (заказов ещё не было)");
            }
        }));
    }
}
