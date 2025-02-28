package com.example.expensetrackerapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Date;

public class StatisticsFragment extends Fragment {

    public StatisticsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPager2 viewPager = view.findViewById(R.id.chartsPager);
        TabLayout tabLayout = view.findViewById(R.id.chartsTabs);
        ArrayList<Expense> expenses = new ArrayList<>();
        expenses.add(new Expense(10, new Category("Food", R.drawable.food_category), new Date(), "some description"));
        expenses.add(new Expense(20, new Category("Food", R.drawable.food_category), new Date(), "some description 2"));
        expenses.add(new Expense(10, new Category("Communications", R.drawable.communications_category), new Date(), "some description 3"));
        expenses.add(new Expense(5, new Category("Utilities", R.drawable.utilities_category), new Date(), "some description 4"));
        ChartsPagerAdapter adapter = new ChartsPagerAdapter(getActivity(), expenses);
        viewPager.setAdapter(adapter);

        // connect ViewPager2 with TabLayout
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Inflate custom dot layout for each tab
            ViewGroup parent = view.findViewById(R.id.statisticsFrame);
            View tabView = LayoutInflater.from(getContext()).inflate(R.layout.dot_tab, parent, false);
            tab.setCustomView(tabView);
        }).attach();
    }
}