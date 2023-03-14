package com.example.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    private EditText userMail, userName, userPassword, userPasswordRepeat;
    private TextView buttonSignUp, buttonSignIn;
    private CardView signUpCardView, signInCard;
    private boolean loginModeActive;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, UserListActivity.class));
        }

        database = FirebaseDatabase.getInstance();

        userMail = findViewById(R.id.emailEditText);
        userName = findViewById(R.id.nameEditText);
        userPassword = findViewById(R.id.passwordEditText);
        userPasswordRepeat = findViewById(R.id.passwordRepeatEditText);
        buttonSignUp = findViewById(R.id.signUpButton);
        buttonSignIn = findViewById(R.id.signInButton);
        signUpCardView = findViewById(R.id.signUpButtonCardView);
        signInCard = findViewById(R.id.signInCard);

        signInCard.animate().alpha(1).setDuration(1000);

        buttonSignUp.setOnClickListener(v -> {
            buttonAnimation(signUpCardView);
            loginSignUpUser(userMail.getText().toString().trim(), userPassword.getText().toString().trim());
        });

        buttonSignIn.setOnClickListener(v -> {
            buttonAnimation(buttonSignIn);
            toggleLoginMode();
        });
    }

    private void loginSignUpUser(String email, String password) {

        if (loginModeActive) {

            if (userMail.getText().toString().trim().equals("")) {
                Toast.makeText(SignInActivity.this, "Please input your email", Toast.LENGTH_SHORT).show();
            } else if (userPassword.getText().toString().trim().length() < 6) {
                Toast.makeText(SignInActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = auth.getCurrentUser();

                                Intent intent = new Intent(SignInActivity.this, UserListActivity.class);
                                intent.putExtra("userName", userName.getText().toString().trim());
                                Log.e("UserName", "SignIn " + userName.getText().toString().trim());
                                startActivity(intent);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(SignInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                //                  updateUI(null);
                            }
                        });
            }
        } else {
            if (userMail.getText().toString().trim().equals("")) {
                Toast.makeText(SignInActivity.this, "Please input your email", Toast.LENGTH_SHORT).show();
            } else if (userPassword.getText().toString().trim().length() < 6) {
                Toast.makeText(SignInActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else if (!userPassword.getText().toString().trim().equals(userPasswordRepeat.getText().toString().trim())) {
                Toast.makeText(SignInActivity.this, "Passwords are different", Toast.LENGTH_SHORT).show();
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = auth.getCurrentUser();

                                createUser(user);

                                Intent intent = new Intent(SignInActivity.this, UserListActivity.class);
                                intent.putExtra("userName", userName.getText().toString().trim());
                                startActivity(intent);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private void createUser(FirebaseUser firebaseUser) {
        String key = database.getReference().push().getKey();

        User user = new User();
        user.setAvatar("https://firebasestorage.googleapis.com/v0/b/chat-bc095.appspot.com/o/avatar_images%2Fimage%3A68439?alt=media&token=db43ca45-6f99-446d-99fa-4750c473c92a");
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setName(userName.getText().toString().trim());
        user.setKey(key);

        HashMap<String, Object> postValues = new HashMap<>();
        postValues.put("avatar", user.getEmail());
        postValues.put("email", user.getEmail());
        postValues.put("id", user.getId());
        postValues.put("name", user.getName());
        postValues.put("key", key);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + key, postValues);

        database.getReference().updateChildren(childUpdates);
    }

    private void buttonAnimation(View view) {
        Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            int i = 0;

            @Override
            public void run() {
                if (i > 0) {
                    view.animate().scaleX(1).scaleY(1).setDuration(100);
                    return;
                }
                view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100);
                i++;
                handler.postDelayed(this, 100);
            }
        };
        handler.post(runnable);
    }

    private void toggleLoginMode() {

        if (loginModeActive) {
            loginModeActive = false;
            userName.setVisibility(View.VISIBLE);
            userPasswordRepeat.setVisibility(View.VISIBLE);
            buttonSignUp.setText("Sign Up");
            buttonSignIn.setText("Tap to log in");
            Log.e("Login mode active", loginModeActive + "");

        } else {
            loginModeActive = true;
            userName.setVisibility(View.GONE);
            userPasswordRepeat.setVisibility(View.GONE);
            buttonSignUp.setText("Log in");
            buttonSignIn.setText("Tap to sign up");
            Log.e("Login mode active", loginModeActive + "");
        }
    }

}