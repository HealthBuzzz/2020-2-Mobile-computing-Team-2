package com.healthbuzz.healthbuzz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class WelcomeActivity extends AppCompatActivity {
    int SPLASH_TIME = 2000; //This is 3 seconds

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPrefs =  PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstuse = sharedPrefs.getBoolean("firstuse", true);

        if(firstuse){
           sharedPrefs.edit().putBoolean("firstuse" , false).apply();
            //Timer: 3 seconds
            new Handler().postDelayed(() -> {
                startActivity(new Intent(WelcomeActivity.this, MainActivity3.class));
                finish();
            }, SPLASH_TIME);
        }else {
            //Timer: 3 seconds
            new Handler().postDelayed(() -> {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
            }, SPLASH_TIME);
        }
    }
}
