package com.waiter.app.ui.menu;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.waiter.app.R;
import com.waiter.app.adapter.MenuManageAdapter;
import com.waiter.app.database.entities.MenuItem;
import com.waiter.app.databinding.FragmentMenuManagementBinding;
import com.waiter.app.viewmodel.MainViewModel;

public class MenuManagementFragment extends Fragment {

    private FragmentMenuManagementBinding binding;
    private MainViewModel viewModel;
    private MenuManageAdapter adapter;

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
                    // Edit — navigate to add/edit form
                    Bundle args = new Bundle();
                    args.putLong("editItemId", item.id);
                    Navigation.findNavController(view).navigate(R.id.action_menu_to_editDish, args);
                },
                item -> {
                    // Delete
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Удалить блюдо?")
                            .setMessage(item.name + " будет удалено из меню.")
                            .setPositiveButton("Удалить", (d, w) -> viewModel.deleteMenuItem(item))
                            .setNegativeButton("Отмена", null)
                            .show();
                }
        );
        binding.rvMenuManage.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMenuManage.setAdapter(adapter);

        viewModel.allMenuItems.observe(getViewLifecycleOwner(), items -> adapter.submitList(items));

        binding.fabAddDish.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_menu_to_editDish));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
