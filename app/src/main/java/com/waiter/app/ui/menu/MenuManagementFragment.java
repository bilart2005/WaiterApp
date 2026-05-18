package com.waiter.app.ui.menu;

import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.*;
import com.google.android.material.tabs.TabLayout;
import com.waiter.app.R;
import com.waiter.app.adapter.MenuManageAdapter;
import com.waiter.app.database.entities.MenuItem;
import com.waiter.app.databinding.FragmentMenuManagementBinding;
import com.waiter.app.viewmodel.MainViewModel;
import java.util.*;

public class MenuManagementFragment extends Fragment {

    private FragmentMenuManagementBinding binding;
    private MainViewModel viewModel;
    private MenuManageAdapter adapter;
    private String selectedMenuType = "Основное";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        adapter = new MenuManageAdapter(
                item -> {
                    Bundle args = new Bundle();
                    args.putLong("editItemId", item.id);
                    Navigation.findNavController(view).navigate(R.id.action_menu_to_editDish, args);
                },
                item -> {
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Удалить блюдо?")
                            .setMessage(item.name + " будет удалено из меню.")
                            .setPositiveButton("Удалить", (d, w) -> viewModel.deleteMenuItem(item))
                            .setNegativeButton("Отмена", null)
                            .show();
                },
                item -> {
                    // Duplicate as template
                    Bundle args = new Bundle();
                    args.putString("prefillMenuType", item.menuType);
                    args.putString("prefillSection", item.section);
                    Navigation.findNavController(view).navigate(R.id.action_menu_to_editDish, args);
                }
        );
        binding.rvMenuManage.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMenuManage.setAdapter(adapter);

        viewModel.allMenuTypes.observe(getViewLifecycleOwner(), types -> {
            binding.tabLayout.removeAllTabs();
            List<String> allTypes = new ArrayList<>();
            allTypes.add("Основное");
            allTypes.add("Детское");
            if (types != null) {
                for (String t : types) if (!allTypes.contains(t)) allTypes.add(t);
            }

            for (String t : allTypes) {
                TabLayout.Tab tab = binding.tabLayout.newTab().setText(t);
                binding.tabLayout.addTab(tab);
                if (t.equals(selectedMenuType)) tab.select();
            }
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() != null) {
                    selectedMenuType = tab.getText().toString();
                    updateFilter();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewModel.allMenuItems.observe(getViewLifecycleOwner(), items -> updateFilter());

        binding.fabAddDish.setOnClickListener(v -> navigateToAdd());
        binding.btnAddTop.setOnClickListener(v -> navigateToAdd());
        
        binding.btnExportImport.setOnClickListener(v -> showDataOptions());
    }

    private void showDataOptions() {
        String[] options = {"Скачать меню (JSON)", "Загрузить меню (JSON)"};
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Управление данными")
                .setItems(options, (d, which) -> {
                    if (which == 0) {
                        viewModel.exportData(json -> {
                            if (json != null) {
                                requireActivity().runOnUiThread(() -> {
                                    EditText et = new EditText(requireContext());
                                    et.setText(json);
                                    et.setPadding(48, 32, 48, 32);
                                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                                            .setTitle("Скопируйте этот текст")
                                            .setView(et)
                                            .setPositiveButton("Ок", null)
                                            .show();
                                });
                            }
                        });
                    } else {
                        EditText et = new EditText(requireContext());
                        et.setHint("Вставьте JSON сюда");
                        et.setPadding(48, 32, 48, 32);
                        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Импорт меню")
                                .setView(et)
                                .setPositiveButton("Загрузить", (d2, w2) -> {
                                    viewModel.importData(et.getText().toString().trim(), success -> {
                                        if (success) {
                                            requireActivity().runOnUiThread(() -> 
                                                android.widget.Toast.makeText(requireContext(), "Меню обновлено!", android.widget.Toast.LENGTH_SHORT).show());
                                        }
                                    });
                                })
                                .setNegativeButton("Отмена", null)
                                .show();
                    }
                })
                .show();
    }

    private void navigateToAdd() {
        Bundle args = new Bundle();
        args.putString("prefillMenuType", selectedMenuType);
        Navigation.findNavController(requireView()).navigate(R.id.action_menu_to_editDish, args);
    }

    private void updateFilter() {
        List<MenuItem> all = viewModel.allMenuItems.getValue();
        if (all == null) return;
        
        List<MenuItem> filtered = new ArrayList<>();
        for (MenuItem m : all) {
            if (selectedMenuType.equals(m.menuType)) filtered.add(m);
        }
        adapter.submitMenuItems(filtered);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
