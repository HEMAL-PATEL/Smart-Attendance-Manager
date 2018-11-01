package com.example.anant.smartattendancemanager.Presenter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.anant.smartattendancemanager.Activities.DetailActivity;
import com.example.anant.smartattendancemanager.Activities.MainActivity;
import com.example.anant.smartattendancemanager.Activities.SignUpActivity;
import com.example.anant.smartattendancemanager.Helper.DatabaseHelper;
import com.example.anant.smartattendancemanager.R;
import com.example.anant.smartattendancemanager.User;
import com.example.anant.smartattendancemanager.View.LoginView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.Map;

public class LoginPresenter implements DatabaseHelper.OnDataFetchedListener {

    private FirebaseAuth mAuth;
    private Activity mActivity;
    private String email;
    private String password;
    private String name;
    private LoginView loginView;
    private DatabaseHelper databaseHelper;
    private DatabaseReference mDatabase;

    public LoginPresenter(FirebaseAuth auth, Activity activity, LoginView loginView) {
        mAuth = auth;
        mActivity = activity;
        this.loginView = loginView;
    }

    public void signIn(String email, String password) {
        loginView.toggleProgressVisibility(true);
        this.email = email;
        this.password = password;
        checkValidationAndLogIn(false);
    }

    public void signUp(String email, String password, String name, DatabaseReference mDatabase) {
        loginView.toggleProgressVisibility(true);
        this.email = email;
        this.password = password;
        this.name = name;
        this.mDatabase = mDatabase;
        checkValidationAndLogIn(true);
    }

    private void checkValidationAndLogIn(boolean isSignup) {

        // Check for a valid password, if the user entered one.

        boolean cancel = false;

        if (isSignup) {
            if (TextUtils.isEmpty(name)) {
                loginView.showNameFieldRequired();
                cancel = true;
                loginView.toggleProgressVisibility(false);
            }
        }

        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            loginView.showInvalidPassword();
            cancel = true;
            loginView.toggleProgressVisibility(false);
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            loginView.showEmailFieldRequired();
            cancel = true;
            loginView.toggleProgressVisibility(false);
        } else if (!isEmailValid(email)) {
            loginView.showInvalidEmail();
            cancel = true;
            loginView.toggleProgressVisibility(false);
        }

        if (!cancel && !isSignup) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            login();
        } else if (!cancel && isSignup) {
            signInNewUser();
        }

    }

    private void signInNewUser() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(mActivity, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        writeNewUser(user.getUid(), email, name);
                        loginView.OnSuccess();

                    } else {
                        loginView.onFailed();
                    }

                    // ...
                });
    }

    private void login() {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(mActivity, task -> {
                    if (task.isSuccessful()) {
                        loginView.OnSuccess();
                    } else {
                        loginView.toggleProgressVisibility(false);
                        loginView.onFailed();
                    }
                });
    }

    private void writeNewUser(String userId, String email, String name) {
        User user = new User(email, name, "");
        mDatabase.child("users").child(userId).setValue(user);
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    public void setBackgroundResource() {
        RequestOptions options = new RequestOptions();
        options.centerCrop().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        Glide.with(mActivity)
                .load(R.drawable.backdrop_login_page)
                .apply(options)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        mActivity.getWindow().setBackgroundDrawable(resource);
                    }
                });
    }

    public void getSubjects() {
        FirebaseUser user = mAuth.getCurrentUser();
        String UID = user.getUid();
        databaseHelper = new DatabaseHelper(UID, this);
        databaseHelper.getSubjects();

    }

    @Override
    public void onDataFetched(Map<String, Object> map, boolean isSuccessful) {
        loginView.toggleProgressVisibility(false);
        Intent intent;
        if (map != null)
            intent = new Intent(mActivity, DetailActivity.class);
        else intent = new Intent(mActivity, MainActivity.class);

        mActivity.startActivity(intent);
    }

    public void sendResetPasswordEmail(String email) {
        loginView.toggleProgressVisibility(true);
        if (TextUtils.isEmpty(email)) {
            loginView.toggleProgressVisibility(false);
            loginView.showEmailFieldRequired();
        } else {
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            loginView.toggleProgressVisibility(false);
                            Toast.makeText(mActivity, R.string.password_reset_mail_sent,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void startSignUpActivity(ImageView mImageView) {
        Intent intent = new Intent(mActivity, SignUpActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(mActivity,
                        mImageView,
                        ViewCompat.getTransitionName(mImageView));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mActivity.startActivity(intent, options.toBundle());
        } else mActivity.startActivity(intent);
    }
}
