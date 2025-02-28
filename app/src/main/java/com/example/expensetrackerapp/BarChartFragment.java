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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.SimpleFormatter;

public class BarChartFragment extends Fragment {
    private List<Expense> expenses;
    private BarChart barChart;
    private TimePeriod timePeriod;

    public BarChartFragment(List<Expense> expenses, TimePeriod timePeriod) {
        this.expenses = expenses;
        this.timePeriod = timePeriod;
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

        Map<String, Double> groupedExpenses = new LinkedHashMap<>();
        List<String> allKeys = new ArrayList<>();
        SimpleDateFormat formatter;
        Calendar calendar = Calendar.getInstance();

        switch (timePeriod) {
            case WEEKLY:
                allKeys = Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
                formatter = new SimpleDateFormat("EEE", Locale.getDefault());
                break;
            case MONTHLY:
                int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                for (int i = 1; i <= daysInMonth; i++) {
                    allKeys.add(String.format(Locale.getDefault(), "%d", i));
                }
                formatter = new SimpleDateFormat("dd", Locale.getDefault()); // Day of the month (01, 02, 03, ...)
                break;
            case YEARLY:
                allKeys = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
                formatter = new SimpleDateFormat("MMM", Locale.getDefault()); // Month name (Jan, Feb, etc.)
                break;
            default:
                formatter = null; // Error
                break;
        }

        // Initialize with 0 values to ensure all entries are displayed
        for (String key : allKeys) {
            groupedExpenses.put(key, 0.0);
        }

        for (Expense expense : expenses) {
            String key = formatter.format(expense.getTransactionDate());
            groupedExpenses.put(key, groupedExpenses.getOrDefault(key, 0.0) + expense.getAmount());
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Double> entry : groupedExpenses.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            index++;
        }

        // Create a BarDataSet from the entries
        BarDataSet barDataSet = new BarDataSet(entries, "Expenses");
        barDataSet.setColor(Color.BLUE);  // Set the color of the bars
        barDataSet.setBarBorderWidth(0.2f);

        barChart.getXAxis().setLabelCount(labels.size());
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        // Create BarData from the BarDataSet
        return new BarData(barDataSet);
    }

    public void updateData(List<Expense> expenses, TimePeriod timePeriod) {
        this.expenses = expenses;
        this.timePeriod = timePeriod;
        updateChart();
    }

    private void updateChart() {
        if (barChart != null) {
            BarData data = generateBarData(expenses);
            barChart.setData(data);
            // Disable the grid lines on the X-Axis and Y-Axis
            barChart.getXAxis().setDrawGridLines(false);  // X-Axis grid
            barChart.getXAxis().setGranularity(1f);
            barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            barChart.getXAxis().setLabelRotationAngle(45f);
            barChart.getAxisLeft().setDrawGridLines(false);  // Y-Axis grid (left side)
            barChart.getAxisRight().setDrawGridLines(false);  // Y-Axis grid (right side)
            barChart.getAxisRight().setEnabled(false);
            barChart.getDescription().setEnabled(false);  // Hide description
            // Disable touch interaction (no selection or dragging)
            barChart.setTouchEnabled(false);
            barChart.getLegend().setEnabled(false);
            barChart.invalidate();
        }
    }
}
