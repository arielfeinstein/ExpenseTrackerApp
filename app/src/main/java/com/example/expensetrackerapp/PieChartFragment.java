package com.example.expensetrackerapp;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PieChartFragment extends Fragment {
    private final ArrayList<Expense> expenses;

    public PieChartFragment(ArrayList<Expense> expenses) {
        this.expenses = expenses;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pie_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PieChart pieChart = view.findViewById(R.id.pieChart);

        // group expenses by category
        Map<Category, List<Expense>> groupedExpenses = new HashMap<>();
        for (Expense expense : expenses) {
            groupedExpenses.computeIfAbsent(expense.getCategory(), k -> new ArrayList<>()).add(expense);
        }

        // create chart entries
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<Category, List<Expense>> entry : groupedExpenses.entrySet()) {
            double amount = 0;
            // sum all the amounts for specific category
            for (Expense expense : entry.getValue()) {
                amount += expense.getAmount();
            }

            entries.add(new PieEntry((float) amount, entry.getKey().getName()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");
        List<Integer> colors = generateColors(entries.size());
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.BLACK);

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setRotationEnabled(false);
        pieChart.setHoleRadius(10f);
        pieChart.setTransparentCircleRadius(2f);
        pieChart.setEntryLabelColor(Color.BLACK);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        pieChart.invalidate();
    }

    private List<Integer> generateColors(int count) {
        List<Integer> colors = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            int color = 0xff000000 | random.nextInt(0xffffff);
            colors.add(color);
        }
        return colors;
    }
}
