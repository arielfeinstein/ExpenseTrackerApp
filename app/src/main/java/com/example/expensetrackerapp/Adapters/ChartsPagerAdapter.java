package com.example.expensetrackerapp.Adapters;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.expensetrackerapp.Fragments.BarChartFragment;
import com.example.expensetrackerapp.Fragments.PieChartFragment;
import com.example.expensetrackerapp.Models.Expense;
import com.example.expensetrackerapp.TimePeriod;

import java.util.List;

public class ChartsPagerAdapter extends FragmentStateAdapter {
    private final List<Expense> expenses;
    private final PieChartFragment pieChartFragment;
    private final BarChartFragment barChartFragment;
    private TimePeriod timePeriod;

    public ChartsPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Expense> expenses, TimePeriod timePeriod) {
        super(fragmentActivity);
        this.expenses = expenses;
        this.timePeriod = timePeriod;
        pieChartFragment = new PieChartFragment(expenses);
        barChartFragment = new BarChartFragment(expenses, timePeriod);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return (position == 1) ? barChartFragment : pieChartFragment;
    }

    /**
     * ExpenseListAdapter is responsible for displaying a list of expenses in a RecyclerView.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Expense> expenses, TimePeriod timePeriod) {
        this.expenses.clear();
        this.expenses.addAll(expenses);
        this.timePeriod = timePeriod;
        notifyDataSetChanged();

        // notify charts about the changes
        pieChartFragment.updateData(expenses);
        barChartFragment.updateData(expenses, timePeriod);
    }

    @Override
    public int getItemCount() {
        return 2; // Number of pages
    }
}
