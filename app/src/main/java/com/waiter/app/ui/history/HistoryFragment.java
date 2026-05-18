package com.waiter.app.ui.history;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.waiter.app.adapter.HistoryAdapter;
import com.waiter.app.database.entities.Order;
import com.waiter.app.databinding.FragmentHistoryBinding;
import com.waiter.app.viewmodel.MainViewModel;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private MainViewModel viewModel;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        adapter = new HistoryAdapter(new HistoryAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(Order order) {
                // Переход к деталям заказа
                Bundle args = new Bundle();
                args.putLong("orderId", order.id);
                androidx.navigation.Navigation.findNavController(requireView())
                        .navigate(com.waiter.app.R.id.orderFragment, args);
            }

            @Override
            public void onOrderDelete(Order order) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Удалить запись из истории?")
                        .setMessage("Вы уверены, что хотите удалить данные об этом заказе?")
                        .setPositiveButton("Удалить", (d, w) -> viewModel.deleteOrder(order.id))
                        .setNegativeButton("Отмена", null)
                        .show();
            }
        });
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistory.setAdapter(adapter);

        viewModel.allOrders.observe(getViewLifecycleOwner(), orders -> {
            adapter.submitList(orders);
            binding.tvEmpty.setVisibility(
                    (orders == null || orders.isEmpty()) ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
