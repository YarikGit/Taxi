package com.example.taxi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private TextView checkRed, checkGreen;

    private TextInputLayout emailTextInput;
    private TextInputLayout nameTextInput;
    private TextInputLayout passwordTextInput;
    private TextInputLayout passwordConfirmTextInput;

    private Button signUpButton;
    private Button changeInButton;

    private boolean isSignUpMode = true;
    private boolean isPassengerMode;
    private CardView chooseModeCardView, signCardView;
    private Button choosePassengerModeButton, chooseDriverModeButton;
    private TextView userModePreview;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        userModePreview = findViewById(R.id.userModePreviewTextView);
        chooseModeCardView = findViewById(R.id.chooseModeCardView);
        signCardView = findViewById(R.id.signCardView);
        chooseDriverModeButton = findViewById(R.id.driverModeButton);
        choosePassengerModeButton = findViewById(R.id.passengerModeButton);

        checkRed = findViewById(R.id.checkFontRed);
        checkGreen = findViewById(R.id.checkFontGreen);
        emailTextInput = findViewById(R.id.textInputEmail);
        nameTextInput = findViewById(R.id.textInputName);
        passwordTextInput = findViewById(R.id.textInputPassword);
        passwordConfirmTextInput = findViewById(R.id.textInputConfirmPassword);

        signUpButton = findViewById(R.id.signUpButton);
        changeInButton = findViewById(R.id.changeInButton);

        chooseModeCardView.setVisibility(View.VISIBLE);
        chooseModeCardView.animate().alpha(1).setDuration(800);
        choosePassengerModeButton.setOnClickListener(v -> {
            isPassengerMode = true;
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser != null){
                startMapActivity();
                return;
            }
            runSignView();
        });
        chooseDriverModeButton.setOnClickListener(v -> {
            isPassengerMode = false;
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser != null){
                startMapActivity();
                return;
            }
            runSignView();
        });

        signUpButton.setOnClickListener(v -> signUp());
        changeInButton.setOnClickListener(v -> changeSignMode());
        mAuth = FirebaseAuth.getInstance();

    }

    private void startMapActivity(){
        Intent intent = new Intent(SignInActivity.this, MapsActivity.class);
        if(!isPassengerMode) {
            intent.putExtra("userMode", "driver");
        }else {
            intent.putExtra("userMode", "passenger");
        }
        startActivity(intent);
    }

    private void runSignView(){
        chooseModeCardView.animate().alpha(0).setDuration(500);

        if(isPassengerMode){
            userModePreview.setText("Passenger Sign Up");
        }else {
            userModePreview.setText("Driver Sign Up");
        }

        new Handler().postDelayed(() -> {
            chooseModeCardView.setVisibility(View.GONE);
            signCardView.setVisibility(View.VISIBLE);
            signCardView.animate().alpha(1).setDuration(500);
        },600);



    }

    private boolean validateInputData(){
        boolean b = true;
        String email = emailTextInput.getEditText().getText().toString().trim();
        String name = nameTextInput.getEditText().getText().toString().trim();
        String password1 = passwordTextInput.getEditText().getText().toString().trim();
        String password2 = passwordConfirmTextInput.getEditText().getText().toString().trim();

        if(email.isEmpty()) {
            emailTextInput.setError("Please input your email");
            b = false;
        }else {
            emailTextInput.setError("");
        }
        
        if(isSignUpMode && name.isEmpty()){
            nameTextInput.setError("Please input your name");
            b = false;
        }else if(isSignUpMode && name.length() > 15){
            nameTextInput.setError("Name length have to be lass than 15");
            b = false;
        }else {
            nameTextInput.setError("");
        }

        if(password1.isEmpty()){
            passwordTextInput.setError("Please input your password");
            passwordConfirmTextInput.setError("");
            b = false;
        }else if(password1.length() > 15){
            passwordTextInput.setError("Password length have to be lass than 15");
            passwordConfirmTextInput.setError("");
            b = false;
        }else if(password1.length() < 7){
            passwordTextInput.setError("Password length have to be more than 6");
            passwordConfirmTextInput.setError("");
            b = false;
        }else if(isSignUpMode && !password1.equals(password2)){
            passwordTextInput.setError("");
            passwordConfirmTextInput.setError("Password have to match");
            b = false;
        }else {
            passwordTextInput.setError("");
            passwordConfirmTextInput.setError("");
        }

        if(b){
            checkGreen.setAlpha(1);
            checkGreen.animate().alpha(0).setDuration(800);
        }else {
            checkRed.setAlpha(1);
            checkRed.animate().alpha(0).setDuration(800);
        }

        return b;

    }

    private void signUp(){

        if(isSignUpMode && validateInputData()){
            mAuth.createUserWithEmailAndPassword(emailTextInput.getEditText().getText().toString().trim(),
                            passwordTextInput.getEditText().getText().toString().trim())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            startMapActivity();

                        } else {
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    });
        }else if(!isSignUpMode && validateInputData()){
            mAuth.signInWithEmailAndPassword(emailTextInput.getEditText().getText().toString().trim(),
                            passwordTextInput.getEditText().getText().toString().trim())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();

                                startMapActivity();

                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(SignInActivity.this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void changeSignMode(){
        if(isSignUpMode){
            changeInButton.setText("Sign Up");
            signUpButton.setText("Sign In");
            passwordConfirmTextInput.setVisibility(View.GONE);
            nameTextInput.setVisibility(View.GONE);
            isSignUpMode = false;
            if(isPassengerMode){
                userModePreview.setText("Passenger Sign In");
            }else {
                userModePreview.setText("Driver Sign In");
            }
        }else{
            changeInButton.setText("Sign In");
            signUpButton.setText("Sign Up");
            passwordConfirmTextInput.setVisibility(View.VISIBLE);
            nameTextInput.setVisibility(View.VISIBLE);
            isSignUpMode = true;
            if(isPassengerMode){
                userModePreview.setText("Passenger Sign Up");
            }else {
                userModePreview.setText("Driver Sign Up");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

}