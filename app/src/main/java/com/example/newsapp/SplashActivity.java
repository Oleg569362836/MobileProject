package com.example.newsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500;
    private ImageView logo;
    private TextView loadingText;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        logo = findViewById(R.id.logo);
        loadingText = findViewById(R.id.loadingText);

        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale);
        logo.startAnimation(scaleAnimation);

        startLoadingAnimation();

        // Проверяем, залогинен ли пользователь
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

        new Handler().postDelayed(() -> {
            if (isLoggedIn) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, SPLASH_DELAY);
    }

    private void startLoadingAnimation() {
        new Handler().postDelayed(new Runnable() {
            int dots = 0;
            @Override
            public void run() {
                dots++;
                if (dots > 3) dots = 1;
                String text = "Загрузка новостей";
                for (int i = 0; i < dots; i++) {
                    text += ".";
                }
                loadingText.setText(text);
                new Handler().postDelayed(this, 500);
            }
        }, 500);
    }
}