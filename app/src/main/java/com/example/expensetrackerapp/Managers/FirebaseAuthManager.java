package com.example.expensetrackerapp.Managers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthManager {
    private final static FirebaseAuth auth = FirebaseAuth.getInstance();


    /**
     * Signs in a user with the provided email and password.
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @param callback The callback to be invoked upon completion of the sign-in operation.
     *                 This callback must implement the {@link FirebaseAuthCallback} interface.
     */
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


    /**
     * Signs up a user with the provided email and password.
     * @param email    The email address of the user.
     * @param password The password of the user.
     * @param callback The callback to be invoked upon completion of the sign-up operation.
     *                 This callback must implement the {@link FirebaseAuthCallback} interface.
     */
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

    /**
     * sign out a user
     */
    public static void signOut() {
        auth.signOut();
    }

    /**
     * @return the current authenticated user ID or null if no user is authenticated
     */
    public static String getUserId() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    /**
     * @return the current authenticated user email or null if no user is authenticated
     */
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
