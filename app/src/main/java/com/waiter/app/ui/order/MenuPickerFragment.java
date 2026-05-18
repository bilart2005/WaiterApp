package com.waiter.app.ui.order;

import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.util.*;

public class MenuPickerFragment extends Fragment {

    private FragmentMenuPickerBinding binding;
    private MainViewModel viewModel;
    private MenuPickerAdapter adapter;
    private long orderId;
    private int tableNumber;
    private com.waiter.app.database.entities.Order currentOrder;
    private String selectedType = "Основное";
    private String selectedSection = "";
    private String currentSearchQuery = "";

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

        viewModel.getOrderById(orderId).observe(getViewLifecycleOwner(), order -> {
            this.currentOrder = order;
        });

        binding.tvPickerTitle.setText("Добавить · Стол " + tableNumber);

        adapter = new MenuPickerAdapter(this::onDishSelected, this::onDishInfoClick);
        GridLayoutManager glm = new GridLayoutManager(requireContext(), 3);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemViewType(position) == 0 ? 3 : 1; // 0 is TYPE_HEADER
            }
        });
        binding.rvMenuPicker.setLayoutManager(glm);
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
            if (binding.chipGroupSections.getChildCount() > 0) return; // Already loaded

            binding.chipGroupSections.removeAllViews();
            
            // Define standard categories in order
            String[] categories = {"Напитки", "Закуски", "Салаты", "Горячее", "Десерты"};
            
            for (String category : categories) {
                Chip chip = new Chip(requireContext());
                chip.setText(category);
                chip.setCheckable(true);
                chip.setOnClickListener(v -> viewModel.setSearchQuery(category));
                binding.chipGroupSections.addView(chip);
            }

            // Also add any other sections found in DB
            if (sections != null) {
                Set<String> standard = new HashSet<>(Arrays.asList(categories));
                for (String s : sections) {
                    if (!standard.contains(s)) {
                        Chip chip = new Chip(requireContext());
                        chip.setText(s);
                        chip.setCheckable(true);
                        chip.setOnClickListener(v -> viewModel.setSearchQuery(s));
                        binding.chipGroupSections.addView(chip);
                    }
                }
            }
        });

        // Search
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { return false; }
            @Override public boolean onQueryTextChange(String q) {
                currentSearchQuery = q;
                if (q.trim().isEmpty()) {
                    binding.chipGroupTypes.setVisibility(View.VISIBLE);
                    viewModel.setMenuFilter(selectedType, selectedSection);
                } else {
                    viewModel.setSearchQuery(q);
                }
                return true;
            }
        });

        viewModel.searchResults.observe(getViewLifecycleOwner(), items -> {
            if (currentSearchQuery.trim().isEmpty()) return;
            
            // Filter by selected type if a specific one is active
            List<MenuItem> filtered = new ArrayList<>();
            if (items != null) {
                for (MenuItem m : items) {
                    if (selectedType.equals(m.menuType)) filtered.add(m);
                }
            }
            adapter.submitMenuItems(filtered);
        });

        viewModel.filteredMenuItems.observe(getViewLifecycleOwner(), items -> {
            if (currentSearchQuery.trim().isEmpty()) {
                adapter.submitMenuItems(items);
            }
        });

        setupMenuCategories();
    }

    private void processSearchResults(List<MenuItem> items) {
        if (items == null || items.isEmpty()) {
            adapter.submitMenuItems(null);
            return;
        }

        Set<String> matchingTypes = new HashSet<>();
        for (MenuItem item : items) {
            if (item.menuType != null) matchingTypes.add(item.menuType);
        }

        if (matchingTypes.size() > 1) {
            // Show menu types to choose from in the sections row
            binding.chipGroupSections.removeAllViews();
            
            for (String type : matchingTypes) {
                Chip chip = new Chip(requireContext());
                chip.setText(type);
                chip.setCheckable(true);
                chip.setOnClickListener(v -> {
                    selectedType = type;
                    List<MenuItem> filtered = new ArrayList<>();
                    for (MenuItem m : items) if (type.equals(m.menuType)) filtered.add(m);
                    adapter.submitMenuItems(filtered);
                    setupMenuCategories(); // refresh tabs
                });
                binding.chipGroupSections.addView(chip);
            }
            adapter.submitMenuItems(null); // Clear until choice
        } else {
            adapter.submitMenuItems(items);
        }
    }

    private void setupMenuCategories() {
        // Types (Main, Kids, etc)
        viewModel.allMenuTypes.observe(getViewLifecycleOwner(), types -> {
            binding.chipGroupTypes.removeAllViews();
            
            List<String> allTypes = new ArrayList<>();
            allTypes.add("Основное");
            allTypes.add("Детское");
            if (types != null) {
                for (String t : types) if (!allTypes.contains(t)) allTypes.add(t);
            }

            for (String type : allTypes) {
                Chip chip = new Chip(requireContext());
                chip.setText(type);
                chip.setCheckable(true);
                chip.setChecked(type.equals(selectedType));
                chip.setOnClickListener(v -> {
                    selectedType = type;
                    viewModel.setMenuFilter(selectedType, selectedSection);
                });
                binding.chipGroupTypes.addView(chip);
            }
        });

        // Sections
        viewModel.allSections.observe(getViewLifecycleOwner(), sections -> {
            binding.chipGroupSections.removeAllViews();
            
            String[] categories = {"Напитки", "Закуски", "Салаты", "Горячее", "Десерты"};
            
            Chip allChip = new Chip(requireContext());
            allChip.setText("Все");
            allChip.setCheckable(true);
            allChip.setChecked(selectedSection.isEmpty());
            allChip.setOnClickListener(v -> {
                selectedSection = "";
                viewModel.setMenuFilter(selectedType, selectedSection);
            });
            binding.chipGroupSections.addView(allChip);

            for (String category : categories) {
                Chip chip = new Chip(requireContext());
                chip.setText(category);
                chip.setCheckable(true);
                chip.setChecked(category.equals(selectedSection));
                chip.setOnClickListener(v -> {
                    selectedSection = category;
                    viewModel.setMenuFilter(selectedType, selectedSection);
                });
                binding.chipGroupSections.addView(chip);
            }
        });
    }

    private void onDishSelected(MenuItem item) {
        // Диалог: количество + комментарий
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_dish, null);
        
        android.widget.Spinner spinnerGuest = new android.widget.Spinner(requireContext());
        int guestCount = (currentOrder != null) ? currentOrder.guestCount : 1;
        String[] guests = new String[guestCount];
        for (int i = 0; i < guestCount; i++) guests[i] = (currentOrder != null) ? currentOrder.getGuestName(i + 1) : "Гость " + (i+1);
        android.widget.ArrayAdapter<String> adapterGuest = new android.widget.ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, guests);
        adapterGuest.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGuest.setAdapter(adapterGuest);
        spinnerGuest.setPadding(32, 16, 32, 16);

        LinearLayout layout = (LinearLayout) dialogView;
        layout.setBackgroundColor(0xFFFFFFFF); // White background
        
        TextView tvGuest = new TextView(requireContext());
        tvGuest.setText("Для кого:");
        tvGuest.setTextColor(0xFF000000); // Black text
        tvGuest.setPadding(0, 8, 0, 4);
        layout.addView(tvGuest, 0);
        layout.addView(spinnerGuest, 1);

        android.widget.EditText etQty = dialogView.findViewById(R.id.etQuantity);
        android.widget.EditText etComment = dialogView.findViewById(R.id.etComment);
        etQty.setText("1");
        etQty.setTextColor(0xFF000000);
        etComment.setTextColor(0xFF000000);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.name + " (" + item.menuType + ")")
                .setView(layout)
                .setPositiveButton("Добавить", (d, w) -> {
                    int qty = 1;
                    try { qty = Integer.parseInt(etQty.getText().toString()); } catch (Exception ignored) {}
                    String comment = etComment.getText().toString().trim();
                    int guestIndex = spinnerGuest.getSelectedItemPosition();
                    viewModel.addItemToOrder(orderId, item, qty, comment, guestIndex + 1);
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
