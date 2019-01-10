package com.application.anant.smartattendancemanager.Presenter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.application.anant.smartattendancemanager.Activities.DetailActivity;
import com.application.anant.smartattendancemanager.Activities.MainActivity;
import com.application.anant.smartattendancemanager.Activities.SignUpActivity;
import com.application.anant.smartattendancemanager.Helper.DatabaseHelper;
import com.application.anant.smartattendancemanager.R;
import com.application.anant.smartattendancemanager.User;
import com.application.anant.smartattendancemanager.View.LoginView;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class LoginPresenter implements DatabaseHelper.OnDataFetchedListener {

    private FirebaseAuth mAuth;
    private Activity mActivity;
    private String email;
    private String password;
    private String name;
    private LoginView loginView;
    private DatabaseHelper databaseHelper;
    private DatabaseReference mDatabase;

    private static final String SUBJECTS_ADDED_PREF = "subPref";

    public LoginPresenter(FirebaseAuth auth, Activity activity, LoginView loginView, DatabaseReference reference) {
        mAuth = auth;
        mActivity = activity;
        this.loginView = loginView;
        mDatabase = reference;
    }

    public void signIn(String email, String password) {
        loginView.toggleProgressVisibility(true);
        this.email = email;
        this.password = password;
        checkValidationAndLogIn(false);
    }

    public void signUp(String email, String password, String name) {
        loginView.toggleProgressVisibility(true);
        this.email = email;
        this.password = password;
        this.name = name;
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

    public void writeNewUser(String userId, String email, String name) {
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
        if (map != null) {
            subjectsAdded(true);
            intent = new Intent(mActivity, DetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            intent = new Intent(mActivity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }

        mActivity.startActivity(intent);
    }

    public void subjectsAdded(boolean added) {
        SharedPreferences.Editor editor = mActivity.getSharedPreferences(SUBJECTS_ADDED_PREF, MODE_PRIVATE).edit();
        editor.putBoolean("subAdded", added);
        editor.apply();
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

    public GoogleSignInOptions configueGoogleClient() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(mActivity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        return gso;
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(mActivity, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkIfUserExists(user);
                    } else {
                        loginView.toggleProgressVisibility(false);
                        Toast.makeText(mActivity, R.string.login_failed_message,
                                Toast.LENGTH_SHORT).show();
                        // If sign in fails, display a message to the user.
                    }
                });
    }

    public void checkIfUserExists(FirebaseUser user) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ref.removeEventListener(this);
                    loginView.OnSuccess();
                } else {
                    writeNewUser(user.getUid(), user.getEmail(), user.getDisplayName());
                    ref.removeEventListener(this);
                    loginView.OnSuccess();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void hideSoftKeyboard(Activity activity) {
        if (null == activity) {
            return;
        }

        View view = activity.getCurrentFocus();
        if (null == view) {
            return;
        }

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
