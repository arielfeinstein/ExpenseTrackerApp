package com.example.expensetrackerapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

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

            // show a popup window to ask the user if he is sure
            LayoutInflater inflater = LayoutInflater.from(getContext());
            @SuppressLint("InflateParams")
            View popupView = inflater.inflate(R.layout.clear_all_expenses_popup_window, null);
            LinearLayout outerFrame = popupView.findViewById(R.id.popup_clear_all_expenses_outer_frame);
            ConstraintLayout content = popupView.findViewById(R.id.popup_clear_all_expenses_content);

            content.setScaleX(0.8f);
            content.setScaleY(0.8f);
            content.setAlpha(0f);
            content.animate()
                    .scaleX(1f).scaleY(1f)  // Scale to normal
                    .alpha(1f)               // Fade in
                    .setDuration(400)        // Duration 300ms
                    .setInterpolator(new DecelerateInterpolator()) // Smooth effect
                    .start();

            PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

            // Make outside touchable to dismiss
            outerFrame.setOnClickListener(v1 ->
                content.animate()
                        .scaleX(0.8f).scaleY(0.8f)  // Shrink
                        .alpha(0f)                  // Fade out
                        .setDuration(300)           // Duration 200ms
                        .setInterpolator(new AccelerateInterpolator()) // Smooth exit
                        .withEndAction(popupWindow::dismiss) // Dismiss after animation
                        .start());
            content.setOnClickListener(v1 -> {}); // prevent dismiss when clicking inside the content

            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
            popupView.findViewById(R.id.btnYes).setOnClickListener(v1 -> {
                FirestoreManager.deleteAllExpenses(FirebaseAuthManager.getUserEmail());
                content.animate()
                        .scaleX(0.8f).scaleY(0.8f)  // Shrink
                        .alpha(0f)                  // Fade out
                        .setDuration(300)           // Duration 200ms
                        .setInterpolator(new AccelerateInterpolator()) // Smooth exit
                        .withEndAction(popupWindow::dismiss) // Dismiss after animation
                        .start();
            });
            popupView.findViewById(R.id.btnClose).setOnClickListener(v2 ->
                content.animate()
                        .scaleX(0.8f).scaleY(0.8f)  // Shrink
                        .alpha(0f)                  // Fade out
                        .setDuration(300)           // Duration 200ms
                        .setInterpolator(new AccelerateInterpolator()) // Smooth exit
                        .withEndAction(popupWindow::dismiss) // Dismiss after animation
                        .start());
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