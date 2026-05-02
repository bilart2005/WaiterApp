package com.waiter.app.ui.order;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.waiter.app.R;
import com.waiter.app.adapter.OrderItemAdapter;
import com.waiter.app.databinding.FragmentOrderBinding;
import com.waiter.app.viewmodel.MainViewModel;
import java.util.Locale;

public class OrderFragment extends Fragment {

    private FragmentOrderBinding binding;
    private MainViewModel viewModel;
    private OrderItemAdapter adapter;
    private int tableNumber;
    private long orderId;

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

        assert getArguments() != null;
        tableNumber = getArguments().getInt("tableNumber");
        orderId = getArguments().getLong("orderId");

        binding.tvTableTitle.setText("Стол " + tableNumber);

        adapter = new OrderItemAdapter(
                (item, newQty) -> viewModel.updateQuantity(item.id, newQty),
                item -> {
                    // Диалог редактирования комментария
                    android.widget.EditText et = new android.widget.EditText(requireContext());
                    et.setText(item.comment);
                    et.setHint("Комментарий к блюду");
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(item.menuItemName)
                            .setView(et)
                            .setPositiveButton("Сохранить", (d, w) ->
                                    viewModel.updateComment(item.id, et.getText().toString()))
                            .setNegativeButton("Отмена", null)
                            .show();
                }
        );
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrderItems.setAdapter(adapter);

        viewModel.getOrderItems(orderId).observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            double total = 0;
            if (items != null) for (var item : items) total += item.menuItemPrice * item.quantity;
            binding.tvTotal.setText(String.format(Locale.getDefault(), "Итого: %.0f ₽", total));
            binding.btnCloseOrder.setEnabled(items != null && !items.isEmpty());
        });

        binding.btnAddDish.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong("orderId", orderId);
            args.putInt("tableNumber", tableNumber);
            Navigation.findNavController(v).navigate(R.id.action_order_to_menuPicker, args);
        });

        binding.btnCloseOrder.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Закрыть заказ")
                        .setMessage("Стол " + tableNumber + " будет освобождён. Сумма будет сохранена в истории.")
                        .setPositiveButton("Закрыть", (d, w) -> {
                            viewModel.closeOrder(orderId);
                            Navigation.findNavController(v).navigateUp();
                        })
                        .setNegativeButton("Отмена", null)
                        .show()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
