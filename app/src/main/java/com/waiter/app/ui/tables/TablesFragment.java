package com.waiter.app.ui.tables;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.waiter.app.R;
import com.waiter.app.adapter.TableAdapter;
import com.waiter.app.database.entities.Order;
import com.waiter.app.databinding.FragmentTablesBinding;
import com.waiter.app.viewmodel.MainViewModel;
import java.util.*;

public class TablesFragment extends Fragment {

    private FragmentTablesBinding binding;
    private MainViewModel viewModel;
    private TableAdapter adapter;

    // Конфигурация залa: 10 столов
    private static final int TABLE_COUNT = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTablesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        adapter = new TableAdapter(this::onTableClick);
        binding.rvTables.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvTables.setAdapter(adapter);

        // Build table list and overlay with open orders
        viewModel.getOpenOrders().observe(getViewLifecycleOwner(), orders -> {
            Map<Integer, Order> orderMap = new HashMap<>();
            if (orders != null) for (Order o : orders) orderMap.put(o.tableNumber, o);
            List<TableAdapter.TableItem> items = new ArrayList<>();
            for (int i = 1; i <= TABLE_COUNT; i++) {
                items.add(new TableAdapter.TableItem(i, orderMap.get(i)));
            }
            adapter.submitList(items);
        });
    }

    private void onTableClick(TableAdapter.TableItem tableItem) {
        if (tableItem.order != null) {
            // Стол занят — открыть существующий заказ
            Bundle args = new Bundle();
            args.putInt("tableNumber", tableItem.tableNumber);
            args.putLong("orderId", tableItem.order.id);
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_tables_to_order, args);
        } else {
            // Стол свободен — предложить создать заказ
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Стол " + tableItem.tableNumber)
                    .setMessage("Открыть новый заказ?")
                    .setPositiveButton("Открыть", (d, w) ->
                            viewModel.openOrderForTable(tableItem.tableNumber, orderId -> {
                                requireActivity().runOnUiThread(() -> {
                                    Bundle args = new Bundle();
                                    args.putInt("tableNumber", tableItem.tableNumber);
                                    args.putLong("orderId", orderId);
                                    Navigation.findNavController(requireView())
                                            .navigate(R.id.action_tables_to_order, args);
                                });
                            }))
                    .setNegativeButton("Отмена", null)
                    .show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
