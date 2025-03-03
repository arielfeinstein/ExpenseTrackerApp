package com.example.expensetrackerapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatisticsFragment extends Fragment {
    private ChartsPagerAdapter adapter;
    private LinearLayout chartsSection;
    private ProgressBar progressBar;

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
        chartsSection = view.findViewById(R.id.chartsSection);
        progressBar = view.findViewById(R.id.chartsProgressBar);

        // shows the progress bar and hide the pager
        chartsSection.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // fetch expenses from FireStore and update the pager adapter
        fetchExpenses(TimePeriod.WEEKLY, new FirestoreManager.FirestoreListCallback<Expense>() {
            @Override
            public void onComplete(List<Expense> items) {
                adapter = new ChartsPagerAdapter(getActivity(), items, TimePeriod.WEEKLY);
                viewPager.setAdapter(adapter);

                // connect ViewPager2 with TabLayout
                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    // Inflate custom dot layout for each tab
                    ViewGroup parent = view.findViewById(R.id.statisticsFrame);
                    View tabView = LayoutInflater.from(getContext()).inflate(R.layout.dot_tab, parent, false);
                    tab.setCustomView(tabView);
                }).attach();

                // shows the pager and hide the progress bar
                chartsSection.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                if (e.getMessage() != null)
                    Log.e("Error", e.getMessage());
            }
        });

        Button btnWeekly = view.findViewById(R.id.btn_weekly);
        Button btnMonthly = view.findViewById(R.id.btn_monthly);
        Button btnYearly = view.findViewById(R.id.btn_yearly);

        btnWeekly.setOnClickListener(v -> timePeriodButtonClicked(view, btnWeekly, TimePeriod.WEEKLY));
        btnMonthly.setOnClickListener(v -> timePeriodButtonClicked(view, btnMonthly, TimePeriod.MONTHLY));
        btnYearly.setOnClickListener(v -> timePeriodButtonClicked(view, btnYearly, TimePeriod.YEARLY));
    }

    private void timePeriodButtonClicked(View parentView, Button clickedButton, TimePeriod timePeriod) {
        unselectButtons(parentView);
        clickedButton.setBackgroundResource(R.drawable.button_selected);

        // shows the progress bar and hide the pager
        chartsSection.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // fetch expenses from FireStore and update the pager adapter
        fetchExpenses(timePeriod, new FirestoreManager.FirestoreListCallback<Expense>() {
            @Override
            public void onComplete(List<Expense> items) {
                adapter.updateData(items, timePeriod);
                // shows the pager and hide the progress bar
                chartsSection.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                if (e.getMessage() != null)
                    Log.e("Error", e.getMessage());
            }
        });
    }

    private void unselectButtons(View view) {
        Button btnWeekly = view.findViewById(R.id.btn_weekly);
        Button btnMonthly = view.findViewById(R.id.btn_monthly);
        Button btnYearly = view.findViewById(R.id.btn_yearly);

        btnWeekly.setBackgroundResource(R.drawable.button_unselected);
        btnMonthly.setBackgroundResource(R.drawable.button_unselected);
        btnYearly.setBackgroundResource(R.drawable.button_unselected);
    }

    private void fetchExpenses(TimePeriod timePeriod, FirestoreManager.FirestoreListCallback<Expense> callback) {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        Date startDate;
        switch (timePeriod) {
            case WEEKLY:
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                startDate = calendar.getTime();
                break;
            case MONTHLY:
                calendar.add(Calendar.MONTH, -1);
                startDate = calendar.getTime();
                break;
            case YEARLY:
                calendar.add(Calendar.YEAR, -1);
                startDate = calendar.getTime();
                break;
            default:
                startDate = calendar.getTime();
                break;
        }
        String email = FirebaseAuthManager.getUserEmail();
        FirestoreManager.getExpenses(email, startDate, currentDate, new FirestoreManager.FirestoreListCallback<Expense>() {
            @Override
            public void onComplete(List<Expense> items) {
                callback.onComplete(items);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
}