package com.example.expensetrackerapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ChartsPagerAdapter extends FragmentStateAdapter {
    public ChartsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new BarChartFragment();
        }
        return new PieChartFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // Number of pages
    }
}
