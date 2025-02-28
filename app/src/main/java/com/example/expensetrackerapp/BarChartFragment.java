package com.example.expensetrackerapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

public class BarChartFragment extends Fragment {
    private List<Expense> expenses;
    private BarChart barChart;

    public BarChartFragment(List<Expense> expenses) {
        this.expenses = expenses;
        updateChart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bar_chart, container, false);
        barChart = view.findViewById(R.id.barChart);
        updateChart();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private BarData generateBarData(List<Expense> expenses) {
        // TODO: get data to show from the constructor and display it in the BarChart view

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 4f));  // Bar at position 0 with a value of 4
        entries.add(new BarEntry(1f, 6f));  // Bar at position 1 with a value of 6
        entries.add(new BarEntry(2f, 2f));  // Bar at position 2 with a value of 2
        entries.add(new BarEntry(3f, 8f));  // Bar at position 3 with a value of 8

        // Create a BarDataSet from the entries
        BarDataSet barDataSet = new BarDataSet(entries, "Example Data");
        barDataSet.setColor(Color.BLUE);  // Set the color of the bars

        // Create BarData from the BarDataSet
        return new BarData(barDataSet);
    }

    public void updateData(List<Expense> expenses) {
        this.expenses = expenses;
        updateChart();
    }

    private void updateChart() {
        if (barChart != null) {
            BarData data = generateBarData(expenses);
            barChart.setData(data);
            // Disable the grid lines on the X-Axis and Y-Axis
            barChart.getXAxis().setDrawGridLines(false);  // X-Axis grid
            barChart.getAxisLeft().setDrawGridLines(false);  // Y-Axis grid (left side)
            barChart.getAxisRight().setDrawGridLines(false);  // Y-Axis grid (right side)
            barChart.getDescription().setEnabled(false);  // Hide description
            // Disable touch interaction (no selection or dragging)
            barChart.setTouchEnabled(false);
            barChart.invalidate();
        }
    }
}
