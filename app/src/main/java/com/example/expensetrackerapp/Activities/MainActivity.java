package com.example.expensetrackerapp.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.example.expensetrackerapp.Fragments.CategoriesFragment;
import com.example.expensetrackerapp.Fragments.HomeFragment;
import com.example.expensetrackerapp.Managers.FirebaseAuthManager;
import com.example.expensetrackerapp.R;
import com.example.expensetrackerapp.Fragments.SettingsFragment;
import com.example.expensetrackerapp.Fragments.StatisticsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if any user is authenticated
        String userID = FirebaseAuthManager.getUserId();
        if (userID == null) {
            // no user authenticated - start GetStartedActivity
            Intent intent = new Intent(this, GetStartedActivity.class);
            startActivity(intent);
            return;
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        // Get toolbar views
        TextView toolBarTitle = findViewById(R.id.toolbar_title_tv);
        Toolbar toolbar = findViewById(R.id.activity_main_regular_toolbar); // needs to be gone when home fragment is selected

        // set toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() == null ) {
            Log.d("MainActivity", "onCreate: Failed to set action bar ");
            throw new NullPointerException(); // Critical error! crash app
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false); // toolbar will not show the name of the app as the title
        actionBar.hide(); // hide by default because home fragment is default and has its own collapsible toolbar - hide to avoid conflict

        // Set default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        // Set up the listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
                actionBar.show();
                toolBarTitle.setText("Settings");
            } else if (item.getItemId() == R.id.nav_home) {
                actionBar.hide();
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_statistics) {
                selectedFragment = new StatisticsFragment();
                actionBar.show();
                toolBarTitle.setText("Statistics");
            } else if (item.getItemId() == R.id.nav_categories) {
                selectedFragment = new CategoriesFragment();
                actionBar.show();
                toolBarTitle.setText("Categories");
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });
    }
}