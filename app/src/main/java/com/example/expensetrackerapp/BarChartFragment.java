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
        Map<String, Double> groupedExpenses = new LinkedHashMap<>();
        List<String> allKeys = new ArrayList<>();
        SimpleDateFormat formatter;
        Calendar calendar = Calendar.getInstance();

        switch (timePeriod) {
            case WEEKLY:
                // Use day of the week (Sun, Mon, Tue, Wed, Thu, Fri, Sat)
                allKeys = Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
                formatter = new SimpleDateFormat("EEE", Locale.ENGLISH);
                break;
            case MONTHLY:
                // Use day of the month (1, 2, 3, ...)
                int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                formatter = new SimpleDateFormat("d", Locale.ENGLISH); // Day of the month (01, 02, 03, ...)
                for (int i = 1; i <= daysInMonth; i++) {
                    allKeys.add(String.valueOf(i)); // Day numbers (1, 2, 3, ...)
                }
                break;
            case YEARLY:
                // Use months of the year (Jan, Feb, Mar, ...)
                allKeys = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
                formatter = new SimpleDateFormat("MMM", Locale.ENGLISH); // Month abbreviation (Jan, Feb, ...)
                break;
            default:
                formatter = null; // Error handling
                break;
        }

        // Initialize the groupedExpenses map with 0 values
        for (String key : allKeys) {
            groupedExpenses.put(key, 0.0);
        }

        // Group expenses by time period (week, month, or year)
        for (Expense expense : expenses) {
            String key = formatter.format(expense.getTransactionDate());

            // Add the expense amount to the appropriate key in the groupedExpenses map
            if (groupedExpenses.containsKey(key)) {
                groupedExpenses.put(key, groupedExpenses.get(key) + expense.getAmount());
            } else {
                groupedExpenses.put(key, expense.getAmount());
            }
        }

        // Create BarEntries and labels
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;

        for (String key : allKeys) {
            // Ensure we are adding values in the correct order
            Double amount = groupedExpenses.get(key);
            entries.add(new BarEntry(index, amount.floatValue()));
            labels.add(key);
            index++;
        }

        // Create a BarDataSet with the entries and set the color
        BarDataSet barDataSet = new BarDataSet(entries, "Expenses");
        barDataSet.setColor(Color.BLUE);  // Set the color of the bars
        barDataSet.setBarBorderWidth(0.2f);

        // Set X-axis labels
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        // Create and return BarData
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
            barChart.getAxisLeft().setAxisMinimum(0f);
            barChart.getDescription().setEnabled(false);  // Hide description
            // Disable touch interaction (no selection or dragging)
            barChart.setTouchEnabled(false);
            barChart.getLegend().setEnabled(false);
            barChart.invalidate();
        }
    }
}
