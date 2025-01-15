package com.example.expensetrackerapp;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String FIREBASE_AUTHENTICATION_LOG_TAG = "Firebase Authentication";
    private EditText emailET, passwordET, confirmPasswordET;
    private Button loginBtn;
    private TextView signupInviteTV;
    private boolean inRegisterMode;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initViews();

        // Set register mode to false
        inRegisterMode = false;

        // Get instance of firebase authentication
        mAuth = FirebaseAuth.getInstance();

        // Listener for login button
        loginBtn.setOnClickListener(v -> {
            String[] userInput = getUserInput();

            // Fields entered correctly
            if (userInput != null) {
                if (inRegisterMode) {
                    // attempt to register
                    registerUser(userInput[0], userInput[1]);
                } else {
                    // attempt to login
                    logInUser(userInput[0], userInput[1]);
                }
            }
        });

        // Listener for signup TextView
        signupInviteTV.setOnClickListener(v -> {
            loadRegistrationLayout();
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
    }

    private void logInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(FIREBASE_AUTHENTICATION_LOG_TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, handle specific exceptions
                            Exception e = task.getException();

                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                // Invalid credentials (email or password)
                                Toast.makeText(LoginActivity.this, "Authentication failed. Incorrect email or password.",
                                        Toast.LENGTH_SHORT).show();
                            } else if (e instanceof FirebaseNetworkException) {
                                // Network error
                                Toast.makeText(LoginActivity.this, "Authentication failed. Network error.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Other errors
                                Log.w(FIREBASE_AUTHENTICATION_LOG_TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed. Try again later.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            //updateUI(null);
                        }
                    }
                });
    }


    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(FIREBASE_AUTHENTICATION_LOG_TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthWeakPasswordException) {
                                // weak password
                                Toast.makeText(LoginActivity.this, "Authentication failed. Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                            }
                            else if (e instanceof FirebaseAuthUserCollisionException) {
                                // email already in use
                                Toast.makeText(LoginActivity.this, "Authentication failed. Email already in use", Toast.LENGTH_SHORT).show();
                            }
                            else if (e instanceof FirebaseNetworkException) {
                                // network error
                                Toast.makeText(LoginActivity.this, "Authentication failed. Network error", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                // other errors when registering
                                Log.w(FIREBASE_AUTHENTICATION_LOG_TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed. Try again later",
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }
                        }
                    }
                });
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

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
            return null;
        }

        // Check if the email structure is valid
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (inRegisterMode) {
            String confirmPassword = confirmPasswordET.getText().toString();
            if (confirmPassword.isEmpty()) {
                Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
                return null;
            }

            if (!password.equals(confirmPassword)) {
                Log.d("password_match", "password = " + password + " confirm password = " + confirmPassword);
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        return new String[] {email, password};
    }


    // assigns views from activity_login.xml
    private void initViews() {
        // EditTexts
        emailET= findViewById(R.id.activity_login_et_email);
        passwordET = findViewById(R.id.activity_login_et_password);
        confirmPasswordET = findViewById(R.id.activity_login_et_confirm_password);

        // Login Button
        loginBtn = findViewById(R.id.activity_login_btn_login);

        // signup invite TextView
        signupInviteTV = findViewById(R.id.activity_login_tv_signup);

    }
}