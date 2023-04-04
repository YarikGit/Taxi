package com.example.taxi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreenActivity extends AppCompatActivity {

    private TextView splashText;
    private Handler handler;
    private ImageView red, yellow, green;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        handler = new Handler();
        splashText = findViewById(R.id.splashTextView);
        red = findViewById(R.id.splashRedLight);
        yellow = findViewById(R.id.splashYellowLight);
        green = findViewById(R.id.splashGreenLight);

        startAnimation();

        new Thread(){
            @Override
            public void run(){
                try{
                    sleep(1667);
                    red.animate().alpha(0.2f).setDuration(300);
                    yellow.animate().alpha(1f).setDuration(300);
                    sleep(1667);
                    yellow.animate().alpha(0.2f).setDuration(300);
                    green.animate().alpha(1f).setDuration(200);
                    sleep(1666);
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    handler.removeCallbacksAndMessages(null);
                    startActivity(new Intent(SplashScreenActivity.this, SignInActivity.class));
                }
            }
        }.start();
    }

    private void startAnimation(){
        Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if(splashText.getAlpha() < 1){
                    splashText.animate().alpha(1).setDuration(700);
                } else if (splashText.getAlpha() > 0) {
                    splashText.animate().alpha(0.1f).setDuration(700);
                }

                handler.postDelayed(this, 800);
            }
        };
        handler.post(runnable);

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}