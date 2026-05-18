package com.waiter.app.ui.tables;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.waiter.app.R;
import com.waiter.app.database.entities.Order;
import com.waiter.app.database.entities.OrderWithItems;
import com.waiter.app.database.entities.Table;
import com.waiter.app.databinding.FragmentTablesBinding;
import com.waiter.app.databinding.ItemTableBinding;
import com.waiter.app.viewmodel.MainViewModel;
import java.util.*;

public class TablesFragment extends Fragment {

    private FragmentTablesBinding binding;
    private MainViewModel viewModel;

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

        viewModel.allTables.observe(getViewLifecycleOwner(), tables -> {
            viewModel.openOrdersWithItems.observe(getViewLifecycleOwner(), orders -> {
                renderTables(tables, orders);
            });
        });

        binding.btnAddTable.setOnClickListener(v -> addNewTable());
    }

    private void renderTables(List<Table> tables, List<OrderWithItems> orders) {
        binding.tableContainer.removeAllViews();
        if (tables == null) return;

        Map<Integer, OrderWithItems> orderMap = new HashMap<>();
        if (orders != null) for (OrderWithItems o : orders) orderMap.put(o.order.tableNumber, o);

        for (Table table : tables) {
            ItemTableBinding itemBinding = ItemTableBinding.inflate(getLayoutInflater(), binding.tableContainer, false);
            View tableView = itemBinding.getRoot();

            OrderWithItems activeOrderInfo = orderMap.get(table.number);
            boolean hasActiveOrder = activeOrderInfo != null;
            boolean isOccupied = hasActiveOrder && activeOrderInfo.itemCount > 0;

            if (isOccupied) {
                itemBinding.tvStatus.setText(activeOrderInfo.order.totalAmount > 0 
                        ? String.format("%.0f ₽", activeOrderInfo.order.totalAmount) : "Занят");
                itemBinding.tvStatus.setTextColor(0xFFFFFFFF);
                itemBinding.tvTableNumber.setTextColor(0xFFFFFFFF);
                itemBinding.getRoot().setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFD32F2F)); 
                itemBinding.btnQuickClose.setVisibility(View.VISIBLE);
                itemBinding.btnQuickClose.setOnClickListener(v -> {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Закрыть стол " + table.number + "?")
                            .setMessage("Заказ уйдет в историю.")
                            .setPositiveButton("Закрыть", (d, w) -> viewModel.closeOrder(activeOrderInfo.order.id))
                            .setNegativeButton("Отмена", null)
                            .show();
                });
            } else if (hasActiveOrder) {
                itemBinding.tvStatus.setText("Выбор");
                itemBinding.tvStatus.setTextColor(0xFF000000);
                itemBinding.tvTableNumber.setTextColor(0xFF000000);
                itemBinding.getRoot().setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFFBC02D));
                itemBinding.btnQuickClose.setVisibility(View.GONE);
            } else {
                itemBinding.tvStatus.setText("Свободно");
                itemBinding.tvStatus.setTextColor(0xFFFFFFFF);
                itemBinding.tvTableNumber.setTextColor(0xFFFFFFFF);
                itemBinding.getRoot().setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFF388E3C));
                itemBinding.btnQuickClose.setVisibility(View.GONE);
            }

            tableView.setX(table.posX);
            tableView.setY(table.posY);
            
            int sizePx = (int) (table.size * getResources().getDisplayMetrics().density);
            tableView.setLayoutParams(new FrameLayout.LayoutParams(sizePx, sizePx));

            itemBinding.tvTableNumber.setText(String.valueOf(table.number));

            setupDragAndDrop(tableView, table);

            tableView.setOnClickListener(v -> {
                if (hasActiveOrder) {
                    openOrder(activeOrderInfo.order.id, table.number);
                } else {
                    viewModel.openOrderForTable(table.number, id -> {
                        requireActivity().runOnUiThread(() -> openOrder(id, table.number));
                    });
                }
            });

            tableView.setOnLongClickListener(v -> {
                showEditDialog(table, hasActiveOrder ? activeOrderInfo.order : null);
                return true;
            });

            binding.tableContainer.addView(tableView);
        }
    }

    private void openOrder(long orderId, int tableNum) {
        Bundle args = new Bundle();
        args.putInt("tableNumber", tableNum);
        args.putLong("orderId", orderId);
        Navigation.findNavController(requireView()).navigate(R.id.action_tables_to_order, args);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDragAndDrop(View view, Table table) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;
            private long startTime;
            private float initialX, initialY;
            private boolean isLongPressed = false;
            private final Runnable longPressRunnable = new Runnable() {
                @Override public void run() { isLongPressed = true; view.performLongClick(); }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        initialX = v.getX(); initialY = v.getY();
                        startTime = System.currentTimeMillis();
                        isLongPressed = false;
                        v.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;
                        if (Math.hypot(newX - initialX, newY - initialY) > 20) {
                            v.removeCallbacks(longPressRunnable);
                            newX = Math.max(0, Math.min(newX, binding.tableContainer.getWidth() - v.getWidth()));
                            newY = Math.max(0, Math.min(newY, binding.tableContainer.getHeight() - v.getHeight()));
                            v.setX(newX); v.setY(newY);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.removeCallbacks(longPressRunnable);
                        if (Math.hypot(v.getX() - initialX, v.getY() - initialY) > 20) {
                            viewModel.updateTablePosition(table.id, v.getX(), v.getY());
                        } else if (!isLongPressed && System.currentTimeMillis() - startTime < 300) {
                            v.performClick();
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void addNewTable() {
        int nextNum = 1;
        List<Table> current = viewModel.allTables.getValue();
        if (current != null) {
            Set<Integer> nums = new HashSet<>();
            for (Table t : current) nums.add(t.number);
            while (nums.contains(nextNum)) nextNum++;
        }
        viewModel.insertTable(new Table(nextNum, 100, 100));
    }

    private void showEditDialog(Table table, Order activeOrder) {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_table, null);
        LinearLayout innerLayout = view.findViewById(R.id.inner_edit_layout);
        EditText etNumber = view.findViewById(R.id.etTableNumber);
        Slider sliderSize = view.findViewById(R.id.sliderSize);
        TextView tvSize = view.findViewById(R.id.tvSizeLabel);
        View btnDelete = view.findViewById(R.id.btnDeleteTable);
        
        etNumber.setText(String.valueOf(table.number));
        sliderSize.setValue((float) table.size);
        tvSize.setText("Размер стола: " + table.size + " dp");
        sliderSize.addOnChangeListener((s, value, fromUser) -> tvSize.setText("Размер стола: " + (int)value + " dp"));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Стол " + table.number)
                .setView(view)
                .setPositiveButton("Сохранить", (d, w) -> {
                    try {
                        int num = Integer.parseInt(etNumber.getText().toString());
                        viewModel.updateTableNumber(table.id, num);
                        viewModel.updateTableSize(table.id, (int)sliderSize.getValue());
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Отмена", null)
                .create();

        if (activeOrder != null && innerLayout != null) {
            com.google.android.material.button.MaterialButton btnClose = new com.google.android.material.button.MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnClose.setText("ЗАКРЫТЬ ЗАКАЗ (В ИСТОРИЮ)");
            btnClose.setTextColor(0xFFD32F2F);
            innerLayout.addView(btnClose, 0); 
            btnClose.setOnClickListener(v -> { viewModel.closeOrder(activeOrder.id); dialog.dismiss(); });
        }

        btnDelete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext()).setTitle("Удалить стол?").setMessage("Это удалит его со схемы.")
                .setPositiveButton("Удалить", (d, w) -> { viewModel.deleteTable(table); dialog.dismiss(); })
                .setNegativeButton("Отмена", null).show();
        });
        dialog.show();
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
