package com.example.expensetrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class GetStartedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        // get a reference to get started button
        Button btnGetStarted = findViewById(R.id.btnGetStarted);

        // set the get started button click listener
        btnGetStarted.setOnClickListener(v -> getStarted());
    }

    /**
     * Helper function - handle the click event of btnGetStarted
     */
    private void getStarted() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}