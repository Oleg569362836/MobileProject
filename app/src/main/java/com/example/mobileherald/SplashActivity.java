package com.example.mobileherald;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 секунды

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Применяем тему перед отображением
        int themeMode = ThemeHelper.getThemeMode(this);
        ThemeHelper.applyTheme(themeMode);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}