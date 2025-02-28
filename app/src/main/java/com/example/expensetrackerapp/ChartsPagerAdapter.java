package com.example.expensetrackerapp;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChartsPagerAdapter extends FragmentStateAdapter {
    private final List<Expense> expenses;

    public ChartsPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Expense> expenses) {
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

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Expense> expenses) {
        this.expenses.clear();
        this.expenses.addAll(expenses);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return 2; // Number of pages
    }
}
