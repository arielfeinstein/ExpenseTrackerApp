package com.example.expensetrackerapp;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChartsPagerAdapter extends FragmentStateAdapter {
    private final List<Expense> expenses;
    private final PieChartFragment pieChartFragment;
    private final BarChartFragment barChartFragment;

    public ChartsPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Expense> expenses) {
        super(fragmentActivity);
        this.expenses = expenses;
        pieChartFragment = new PieChartFragment(expenses);
        barChartFragment = new BarChartFragment(expenses);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return (position == 1) ? barChartFragment : pieChartFragment;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Expense> expenses) {
        this.expenses.clear();
        this.expenses.addAll(expenses);
        notifyDataSetChanged();

        // notify charts about the changes
        pieChartFragment.updateData(expenses);
        barChartFragment.updateData(expenses);
    }

    @Override
    public int getItemCount() {
        return 2; // Number of pages
    }
}
