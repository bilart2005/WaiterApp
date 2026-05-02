package com.waiter.app.ui.menu;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.waiter.app.R;
import com.waiter.app.database.entities.MenuItem;
import com.waiter.app.databinding.FragmentDishDetailBinding;
import com.waiter.app.viewmodel.MainViewModel;

public class DishDetailFragment extends Fragment {

    private FragmentDishDetailBinding binding;
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDishDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        assert getArguments() != null;
        long menuItemId = getArguments().getLong("menuItemId", -1);

        // Load item in background
        new Thread(() -> {
            com.waiter.app.database.AppDatabase db =
                    com.waiter.app.database.AppDatabase.getInstance(requireContext());
            MenuItem item = db.menuItemDao().getById(menuItemId);
            if (item != null) {
                requireActivity().runOnUiThread(() -> bindItem(item));
            }
        }).start();

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void bindItem(MenuItem item) {
        binding.tvDishName.setText(item.name);
        binding.tvSection.setText(item.section);
        binding.tvDescription.setText(item.description);
        binding.tvPrice.setText(String.format("%.0f ₽", item.price));
        binding.tvCookingTime.setText(item.cookingTimeMinutes > 0
                ? item.cookingTimeMinutes + " мин" : "Готово");
        binding.tvAllergens.setText(
                (item.allergens != null && !item.allergens.isEmpty()) ? item.allergens : "Нет");

        if (item.photoPath != null && !item.photoPath.isEmpty()) {
            Glide.with(this).load(item.photoPath)
                    .placeholder(R.drawable.ic_dish_placeholder)
                    .into(binding.ivDishPhoto);
        } else {
            binding.ivDishPhoto.setImageResource(R.drawable.ic_dish_placeholder);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
