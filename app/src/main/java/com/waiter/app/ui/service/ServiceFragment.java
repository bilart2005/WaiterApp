package com.waiter.app.ui.service;

import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.waiter.app.R;
import com.waiter.app.adapter.ServiceAdapter;
import com.waiter.app.database.entities.OrderItem;
import com.waiter.app.databinding.FragmentServiceBinding;
import com.waiter.app.viewmodel.MainViewModel;
import java.util.List;

public class ServiceFragment extends Fragment {

    private FragmentServiceBinding binding;
    private MainViewModel viewModel;
    private ServiceAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentServiceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        adapter = new ServiceAdapter(new ServiceAdapter.ServiceListener() {
            @Override
            public void onServedStatusChange(long itemId, boolean served) {
                viewModel.updateServedStatus(itemId, served);
            }

            @Override
            public void onAddNote(long orderId) {
                showNoteDialog(orderId);
            }

            @Override
            public void onInfoClick(OrderItem item) {
                if (item.menuItemId != null) {
                    Bundle args = new Bundle();
                    args.putLong("menuItemId", item.menuItemId);
                    Navigation.findNavController(requireView()).navigate(R.id.action_global_dishDetail, args);
                }
            }
        });

        binding.rvService.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvService.setAdapter(adapter);

        // Combined observation
        viewModel.openOrders.observe(getViewLifecycleOwner(), orders -> {
            viewModel.getAllOpenOrderItems().observe(getViewLifecycleOwner(), items -> {
                adapter.submitData(orders, items);
                binding.tvEmptyService.setVisibility((items == null || items.isEmpty()) ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void showNoteDialog(long orderId) {
        viewModel.getOrderById(orderId).observe(getViewLifecycleOwner(), new androidx.lifecycle.Observer<com.waiter.app.database.entities.Order>() {
            @Override
            public void onChanged(com.waiter.app.database.entities.Order order) {
                viewModel.getOrderById(orderId).removeObserver(this);
                if (order == null) return;

                EditText et = new EditText(requireContext());
                et.setText(order.tableNotes);
                et.setHint("Напр. Попросили больше льда");
                et.setPadding(48, 32, 48, 32);

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Заметка к столу " + order.tableNumber)
                        .setView(et)
                        .setPositiveButton("Сохранить", (d, w) -> {
                            viewModel.updateTableNotes(orderId, et.getText().toString().trim());
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
