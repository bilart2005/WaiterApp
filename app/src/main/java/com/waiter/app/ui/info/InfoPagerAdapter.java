package com.waiter.app.ui.info;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class InfoPagerAdapter extends FragmentStateAdapter {
    public InfoPagerAdapter(@NonNull Fragment fragment) { super(fragment); }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new InfoEnterpriseFragment();
            case 1: return new InfoStructureFragment();
            case 2: return new InfoDataModelFragment();
            case 3: return new InfoProcessFragment();
            default: return new InfoEnterpriseFragment();
        }
    }

    @Override
    public int getItemCount() { return 4; }
}
