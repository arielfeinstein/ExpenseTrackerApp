package com.example.expensetrackerapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class ChartsPagerAdapter extends FragmentStateAdapter {
    private final ArrayList<Expense> expenses;

    public ChartsPagerAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Expense> expenses) {
        super(fragmentActivity);
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new BarChartFragment(expenses);
        }
        return new PieChartFragment(expenses);
    }

    @Override
    public int getItemCount() {
        return 2; // Number of pages
    }
}
