package com.waiter.app.ui.menu;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.waiter.app.database.entities.MenuItem;
import com.waiter.app.databinding.FragmentEditDishBinding;
import com.waiter.app.viewmodel.MainViewModel;

public class EditDishFragment extends Fragment {

    private FragmentEditDishBinding binding;
    private MainViewModel viewModel;
    private long editItemId = -1;
    private MenuItem editingItem = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditDishBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        if (getArguments() != null) {
            editItemId = getArguments().getLong("editItemId", -1);
        }

        if (editItemId != -1) {
            binding.tvEditTitle.setText("Редактировать блюдо");
            new Thread(() -> {
                com.waiter.app.database.AppDatabase db =
                        com.waiter.app.database.AppDatabase.getInstance(requireContext());
                editingItem = db.menuItemDao().getById(editItemId);
                if (editingItem != null) {
                    requireActivity().runOnUiThread(() -> {
                        binding.etName.setText(editingItem.name);
                        binding.etSection.setText(editingItem.section);
                        binding.etPrice.setText(String.valueOf((int) editingItem.price));
                        binding.etDescription.setText(editingItem.description);
                        binding.etAllergens.setText(editingItem.allergens);
                        binding.etCookingTime.setText(String.valueOf(editingItem.cookingTimeMinutes));
                    });
                }
            }).start();
        } else {
            binding.tvEditTitle.setText("Новое блюдо");
        }

        binding.btnSave.setOnClickListener(v -> saveDish());
        binding.btnCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void saveDish() {
        String name = binding.etName.getText().toString().trim();
        String section = binding.etSection.getText().toString().trim();
        String priceStr = binding.etPrice.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String allergens = binding.etAllergens.getText().toString().trim();
        String timeStr = binding.etCookingTime.getText().toString().trim();

        if (name.isEmpty() || section.isEmpty() || priceStr.isEmpty()) {
            binding.etName.setError(name.isEmpty() ? "Обязательное поле" : null);
            binding.etSection.setError(section.isEmpty() ? "Обязательное поле" : null);
            binding.etPrice.setError(priceStr.isEmpty() ? "Обязательное поле" : null);
            return;
        }

        double price;
        try { price = Double.parseDouble(priceStr); } catch (Exception e) { price = 0; }
        int time;
        try { time = Integer.parseInt(timeStr); } catch (Exception e) { time = 0; }

        if (editItemId != -1 && editingItem != null) {
            editingItem.name = name;
            editingItem.section = section;
            editingItem.price = price;
            editingItem.description = description;
            editingItem.allergens = allergens;
            editingItem.cookingTimeMinutes = time;
            viewModel.updateMenuItem(editingItem);
        } else {
            MenuItem item = new MenuItem(name, section, price, description, allergens, time);
            viewModel.insertMenuItem(item);
        }
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
