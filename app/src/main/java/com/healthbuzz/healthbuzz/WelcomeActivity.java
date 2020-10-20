package com.healthbuzz.healthbuzz;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import android.os.Handler;

public class WelcomeActivity extends AppCompatActivity {
    int SPLASH_TIME = 3000; //This is 3 seconds

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Timer: 3 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
            }
        },SPLASH_TIME);


    }
}
