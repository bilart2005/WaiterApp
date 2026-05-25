package com.waiter.app.ui.info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.waiter.app.R;

public class InfoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPager2 pager = view.findViewById(R.id.info_pager);
        TabLayout tabs = view.findViewById(R.id.info_tabs);
        pager.setAdapter(new InfoPagerAdapter(this));
        new TabLayoutMediator(tabs, pager, (tab, pos) -> {
            switch (pos) {
                case 0: tab.setText("Предприятие"); break;
                case 1: tab.setText("Структура"); break;
                case 2: tab.setText("Данные"); break;
                case 3: tab.setText("Процессы"); break;
            }
        }).attach();
    }
}
