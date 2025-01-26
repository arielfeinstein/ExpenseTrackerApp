package com.example.expensetrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if any user is authenticated
        String userID = FirebaseAuthManager.isLoggedIn();
        if (userID == null) {
            // no user authenticated - start GetStartedActivity
            Intent intent = new Intent(this, GetStartedActivity.class);
            startActivity(intent);
        }

        // set the test sign out button on click event
        Button btnSignOut = findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(v -> FirebaseAuthManager.signOut());
    }
}