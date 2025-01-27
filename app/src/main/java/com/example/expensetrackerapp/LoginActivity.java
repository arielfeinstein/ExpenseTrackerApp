package com.example.expensetrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailET, passwordET, confirmPasswordET;
    private Button loginBtn;
    private TextView signupInviteTV;
    private boolean inRegisterMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        initViews();

        // Set register mode to false
        inRegisterMode = false;

        // Listener for login button
        loginBtn.setOnClickListener(v -> {
            String[] userInput = getUserInput();

            // Fields entered correctly
            if (userInput != null) {
                if (inRegisterMode) {
                    // attempt to register
                    signUp(userInput[0], userInput[1]);
                } else {
                    // attempt to login
                    signIn(userInput[0], userInput[1]);
                }
            }
        });

        // Listener for signup TextView
        signupInviteTV.setOnClickListener(v -> {
            loadRegistrationLayout();
        });

        // Listener for EditText text change event
        emailET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailET.setBackgroundResource(R.drawable.login_input_background);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        passwordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordET.setBackgroundResource(R.drawable.login_input_background);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (inRegisterMode) {
                    resetToLoginMode();
                } else {
                    finish(); // Default back press behavior
                }
            }
        });
    }

    private void signIn(String email, String password) {
        Intent intent = new Intent(this, MainActivity.class);
        FirebaseAuthManager.signIn(email, password, new FirebaseAuthManager.FirebaseAuthCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Welcome " + email + "!", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }

            @Override
            public void onFailure(String errorMsg) {
                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void signUp(String email, String password) {
        Intent intent = new Intent(this, MainActivity.class);
        FirebaseAuthManager.signUp(email, password, new FirebaseAuthManager.FirebaseAuthCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Welcome " + email + "!", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }

            @Override
            public void onFailure(String errorMsg) {
                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resetToLoginMode() {
        confirmPasswordET.setVisibility(View.GONE);
        loginBtn.setText(getString(R.string.login));
        signupInviteTV.setVisibility(View.VISIBLE);
        inRegisterMode = false;
    }

    private void loadRegistrationLayout() {
        confirmPasswordET.setVisibility(View.VISIBLE);
        loginBtn.setText(getString(R.string.signup));
        signupInviteTV.setVisibility(View.GONE);
        inRegisterMode = true;

        // make sure that inputs background is the normal
        emailET.setBackgroundResource(R.drawable.login_input_background);
        passwordET.setBackgroundResource(R.drawable.login_input_background);
    }

    /*
    return string array [email,password] if successfully read from EditTexts. else return null
    reasons to fail:
        1. one or more EditTexts are empty - a Toast message will be displayed
        2. if in registration mode and password and confirm password don't match - a Toast message will be displayed
     */
    private String[] getUserInput() {
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();

        if (inRegisterMode) {
            String confirmPassword = confirmPasswordET.getText().toString();
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
                emailET.setBackgroundResource(R.drawable.login_input_error_background);
                passwordET.setBackgroundResource(R.drawable.login_input_error_background);
                confirmPasswordET.setBackgroundResource(R.drawable.login_input_error_background);
                return null;
            }

            if (!password.equals(confirmPassword)) {
                Log.d("password_match", "password = " + password + " confirm password = " + confirmPassword);
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                emailET.setBackgroundResource(R.drawable.login_input_background);
                passwordET.setBackgroundResource(R.drawable.login_input_error_background);
                confirmPasswordET.setBackgroundResource(R.drawable.login_input_error_background);
                return null;
            }
        } else {
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
                emailET.setBackgroundResource(R.drawable.login_input_error_background);
                passwordET.setBackgroundResource(R.drawable.login_input_error_background);
                return null;
            }
        }

        // Check if the email structure is valid
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            emailET.setBackgroundResource(R.drawable.login_input_error_background);
            passwordET.setBackgroundResource(R.drawable.login_input_background);
            confirmPasswordET.setBackgroundResource(R.drawable.login_input_background);
            return null;
        }

        return new String[] {email, password};
    }


    // assigns views from activity_login.xml
    private void initViews() {
        // EditTexts
        emailET= findViewById(R.id.inputEmail);
        passwordET = findViewById(R.id.inputPassword);
        confirmPasswordET = findViewById(R.id.inputConfirmPassword);

        // Login Button
        loginBtn = findViewById(R.id.btnLogin);

        // signup invite TextView
        signupInviteTV = findViewById(R.id.txtSignUp);
    }
}