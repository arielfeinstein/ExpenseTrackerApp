package com.example.expensetrackerapp.Managers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthManager {
    private final static FirebaseAuth auth = FirebaseAuth.getInstance();

    public static void signIn(String email, String password, FirebaseAuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // sign in successfully
                            callback.onSuccess();
                        } else {
                            callback.onFailure("Failed to sign in");
                        }
                    } else {
                        callback.onFailure("Failed to sign in");
                    }
                });
    }

    public static void signUp(String email, String password, FirebaseAuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // sign up successfully
                            callback.onSuccess();
                        } else {
                            callback.onFailure("Failed to sign up");
                        }
                    } else {
                        // Check the exception
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            callback.onFailure("This email is already in use.");
                        } else {
                            callback.onFailure("Failed to sign up");
                        }
                    }
                });
    }

    public static void signOut() {
        auth.signOut();
    }

    public static String isLoggedIn() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    public static String getUserEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getEmail();
        }
        else {
            return null;
        }
    }

    public interface FirebaseAuthCallback {
        void onSuccess();
        void onFailure(String errorMsg);
    }
}
