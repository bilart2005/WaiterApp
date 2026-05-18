package com.waiter.app.ui.order;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.waiter.app.R;
import com.waiter.app.adapter.OrderItemAdapter;
import com.waiter.app.database.entities.MenuItem;
import com.waiter.app.database.entities.Order;
import com.waiter.app.database.entities.OrderItem;
import com.waiter.app.databinding.FragmentOrderBinding;
import com.waiter.app.viewmodel.MainViewModel;
import java.util.*;

public class OrderFragment extends Fragment {

    private FragmentOrderBinding binding;
    private MainViewModel viewModel;
    private OrderItemAdapter adapter;
    private long orderId;
    private Order currentOrder;
    private int selectedGuestNumber = 1;
    private List<MenuItem> menuItemsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        orderId = getArguments().getLong("orderId");

        setupRecyclerView();
        setupAutoComplete();
        
        // Setup Toolbar navigation
        if (binding.toolbar != null) {
            binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        viewModel.getOrderById(orderId).observe(getViewLifecycleOwner(), order -> {
            if (order == null) return;
            currentOrder = order;
            adapter.setOrder(order);
            binding.tvTableTitle.setText("Стол " + order.tableNumber + " (" + order.guestCount + " чел)");
            updateGuestChips();
        });

        viewModel.getOrderItems(orderId).observe(getViewLifecycleOwner(), items -> {
            adapter.submitOrderItems(items);
            updateTotal(items);
        });

        viewModel.allMenuItems.observe(getViewLifecycleOwner(), items -> {
            menuItemsList = items;
            updateAutoCompleteAdapter();
        });

        binding.btnAddGuest.setOnClickListener(v -> {
            if (currentOrder != null) {
                viewModel.updateGuestCount(orderId, currentOrder.guestCount + 1);
            }
        });

        binding.btnRenameGuests.setOnClickListener(v -> showRenameGuestsDialog());

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnCloseOrder.setOnClickListener(v -> closeOrder());
        binding.btnQuickCloseOrder.setOnClickListener(v -> closeOrder());
    }

    private void updateGuestChips() {
        binding.chipGroupGuests.removeAllViews();
        for (int i = 1; i <= currentOrder.guestCount; i++) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(requireContext());
            chip.setText(currentOrder.getGuestName(i));
            chip.setCheckable(true);
            chip.setId(i);
            if (i == selectedGuestNumber) chip.setChecked(true);
            
            final int guestNum = i;
            chip.setOnClickListener(v -> selectedGuestNumber = guestNum);
            binding.chipGroupGuests.addView(chip);
        }
    }

    private void showRenameGuestsDialog() {
        if (currentOrder == null) return;
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        String[] currentNames = currentOrder.guestNames != null ? currentOrder.guestNames.split("\\|") : new String[0];
        List<EditText> editTexts = new ArrayList<>();

        for (int i = 1; i <= currentOrder.guestCount; i++) {
            EditText et = new EditText(requireContext());
            et.setHint("Имя гостя " + i);
            if (i <= currentNames.length) et.setText(currentNames[i - 1]);
            layout.addView(et);
            editTexts.add(et);
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Имена гостей")
                .setView(layout)
                .setPositiveButton("Сохранить", (d, w) -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < editTexts.size(); i++) {
                        sb.append(editTexts.get(i).getText().toString().trim());
                        if (i < editTexts.size() - 1) sb.append("|");
                    }
                    viewModel.updateGuestNames(orderId, sb.toString());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void setupRecyclerView() {
        adapter = new OrderItemAdapter(new OrderItemAdapter.OrderItemListener() {
            @Override public void onQtyChange(OrderItem item, int newQty) { viewModel.updateQuantity(item.id, newQty); }
            @Override public void onStatusChange(OrderItem item, boolean isServed) { viewModel.updateServedStatus(item.id, isServed); }
            @Override public void onDelete(OrderItem item) { viewModel.deleteOrderItem(item.id); }
            @Override public void onCommentChange(OrderItem item) { showCommentDialog(item); }
            @Override public void onInfoClick(OrderItem item) {
                if (item.menuItemId != null) {
                    Bundle args = new Bundle();
                    args.putLong("menuItemId", item.menuItemId);
                    Navigation.findNavController(requireView()).navigate(R.id.action_order_to_dishDetail, args);
                } else {
                    Toast.makeText(requireContext(), "Информация о кастомном блюде недоступна", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrderItems.setAdapter(adapter);
    }

    private void setupAutoComplete() {
        binding.actvAddItem.setOnItemClickListener((parent, view, position, id) -> {
            MenuItem selected = (MenuItem) parent.getItemAtPosition(position);
            addItemToOrder(selected.name, selected.price, selected.id);
            binding.actvAddItem.setText("");
        });

        binding.actvAddItem.setOnEditorActionListener((v, actionId, event) -> {
            String text = binding.actvAddItem.getText().toString().trim();
            if (!text.isEmpty()) {
                addItemToOrder(text, 0, null);
                binding.actvAddItem.setText("");
            }
            return true;
        });
    }

    private void updateAutoCompleteAdapter() {
        ArrayAdapter<MenuItem> autoAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, menuItemsList);
        binding.actvAddItem.setAdapter(autoAdapter);
    }

    private void addItemToOrder(String name, double price, Long menuItemId) {
        if (currentOrder == null) return;
        
        if (menuItemId != null) {
            MenuItem item = null;
            for (MenuItem mi : menuItemsList) if (mi.id == menuItemId) item = mi;
            if (item != null) viewModel.addItemToOrder(orderId, item, 1, "", selectedGuestNumber);
        } else {
            viewModel.addCustomItemToOrder(orderId, name, price, 1, "", selectedGuestNumber);
        }
    }

    private void showCommentDialog(OrderItem item) {
        EditText et = new EditText(requireContext());
        et.setText(item.comment);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Заметка: " + item.menuItemName)
                .setView(et)
                .setPositiveButton("Ок", (d, w) -> viewModel.updateComment(item.id, et.getText().toString()))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void updateTotal(List<OrderItem> items) {
        double total = 0;
        if (items != null) for (var item : items) total += item.menuItemPrice * item.quantity;
        binding.tvTotal.setText(String.format(Locale.getDefault(), "Итого: %.0f ₽", total));
    }

    private void closeOrder() {
        if (currentOrder != null && "CLOSED".equals(currentOrder.status)) {
            Toast.makeText(requireContext(), "Заказ уже закрыт", Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Закрыть стол?")
                .setMessage("Заказ будет перемещен в историю.")
                .setPositiveButton("Закрыть", (d, w) -> {
                    viewModel.closeOrder(orderId);
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
