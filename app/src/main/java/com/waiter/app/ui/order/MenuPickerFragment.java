package com.waiter.app.ui.order;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.*;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.waiter.app.R;
import com.waiter.app.adapter.MenuPickerAdapter;
import com.waiter.app.database.entities.MenuItem;
import com.waiter.app.databinding.FragmentMenuPickerBinding;
import com.waiter.app.viewmodel.MainViewModel;

public class MenuPickerFragment extends Fragment {

    private FragmentMenuPickerBinding binding;
    private MainViewModel viewModel;
    private MenuPickerAdapter adapter;
    private long orderId;
    private int tableNumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuPickerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        assert getArguments() != null;
        orderId = getArguments().getLong("orderId");
        tableNumber = getArguments().getInt("tableNumber");

        binding.tvPickerTitle.setText("Добавить · Стол " + tableNumber);

        adapter = new MenuPickerAdapter(this::onDishSelected, this::onDishInfoClick);
        binding.rvMenuPicker.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMenuPicker.setAdapter(adapter);

        // Search
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { return false; }
            @Override public boolean onQueryTextChange(String q) {
                viewModel.setSearchQuery(q);
                return true;
            }
        });

        // Sections chips
        viewModel.allSections.observe(getViewLifecycleOwner(), sections -> {
            binding.chipGroupSections.removeAllViews();
            Chip all = new Chip(requireContext());
            all.setText("Все");
            all.setCheckable(true);
            all.setChecked(true);
            all.setOnClickListener(v -> viewModel.setSearchQuery(""));
            binding.chipGroupSections.addView(all);

            if (sections != null) {
                for (String s : sections) {
                    Chip chip = new Chip(requireContext());
                    chip.setText(s);
                    chip.setCheckable(true);
                    chip.setOnClickListener(v -> viewModel.setSearchQuery(s));
                    binding.chipGroupSections.addView(chip);
                }
            }
        });

        viewModel.searchResults.observe(getViewLifecycleOwner(), items -> adapter.submitList(items));
    }

    private void onDishSelected(MenuItem item) {
        // Диалог: количество + комментарий
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_dish, null);
        android.widget.EditText etQty = dialogView.findViewById(R.id.etQuantity);
        android.widget.EditText etComment = dialogView.findViewById(R.id.etComment);
        etQty.setText("1");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Добавить: " + item.name)
                .setView(dialogView)
                .setPositiveButton("Добавить", (d, w) -> {
                    int qty = 1;
                    try { qty = Integer.parseInt(etQty.getText().toString()); } catch (Exception ignored) {}
                    String comment = etComment.getText().toString().trim();
                    viewModel.addItemToOrder(orderId, item, qty, comment);
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void onDishInfoClick(MenuItem item) {
        Bundle args = new Bundle();
        args.putLong("menuItemId", item.id);
        Navigation.findNavController(requireView()).navigate(R.id.action_menuPicker_to_dishDetail, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
