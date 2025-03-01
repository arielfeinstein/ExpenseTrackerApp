package com.example.expensetrackerapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SettingsFragment extends Fragment {
    private Button btnSignOut;
    private Button btnClearExpenses;
    private TextView txtUserEmail;

    public SettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        btnSignOut = view.findViewById(R.id.btn_sign_out);
        btnClearExpenses = view.findViewById(R.id.btnClearExpenses);
        txtUserEmail = view.findViewById(R.id.txtUserEmail);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // set the user email text
        txtUserEmail.setText(FirebaseAuthManager.getUserEmail());

        // handle clear expenses
        btnClearExpenses.setOnClickListener(v -> {
            // TODO: call the method that clears all the expenses in FireStoreManager
            // TODO: consider to show a popup window that ask the user are you sure?
        });

        // Handle signOut
        btnSignOut.setOnClickListener(v -> {
            FirebaseAuthManager.signOut();
            Context context = getContext();
            if (context != null) {
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}