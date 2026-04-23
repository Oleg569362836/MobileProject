package com.example.newsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.newsapp.database.NewsDatabase;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private LinearLayout clearCacheLayout;
    private LinearLayout aboutLayout;
    private TextView fontSizeText;
    private LinearLayout fontSizeLayout;
    private Button btnLogout;
    private SharedPreferences sharedPreferences;
    private int currentFontSize = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        clearCacheLayout = findViewById(R.id.clearCacheLayout);
        aboutLayout = findViewById(R.id.aboutLayout);
        fontSizeText = findViewById(R.id.fontSizeText);
        fontSizeLayout = findViewById(R.id.fontSizeLayout);
        btnLogout = findViewById(R.id.btn_logout);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Настройки");
        }

        loadSettings();
        setupListeners();
    }

    private void loadSettings() {
        currentFontSize = sharedPreferences.getInt("font_size", 1);
        updateFontSizeText();
    }

    private void setupListeners() {
        fontSizeLayout.setOnClickListener(v -> showFontSizeDialog());
        clearCacheLayout.setOnClickListener(v -> showClearCacheDialog());
        aboutLayout.setOnClickListener(v -> showAboutDialog());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void showFontSizeDialog() {
        String[] sizes = {"Маленький", "Средний", "Большой"};
        new AlertDialog.Builder(this)
                .setTitle("Размер шрифта")
                .setSingleChoiceItems(sizes, currentFontSize, (dialog, which) -> {
                    currentFontSize = which;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("font_size", currentFontSize);
                    editor.apply();
                    updateFontSizeText();
                    Toast.makeText(this, "Размер шрифта изменён. Перезапустите приложение.", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void updateFontSizeText() {
        String[] sizes = {"Маленький", "Средний", "Большой"};
        fontSizeText.setText(sizes[currentFontSize]);
    }

    private void showClearCacheDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Очистить кэш")
                .setMessage("Вы уверены, что хотите удалить все сохраненные новости?")
                .setPositiveButton("Очистить", (dialog, which) -> {
                    new Thread(() -> {
                        NewsDatabase.getInstance(this).articleDao().deleteAllArticles();
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Кэш очищен", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("О приложении")
                .setMessage("📱 Mobile Herald\n\n" +
                        "Версия: 2.0\n\n" +
                        "Ваш ежедневный источник новостей\n\n" +
                        "© 2026 Mobile Herald")
                .setPositiveButton("OK", null)
                .show();
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                .setPositiveButton("Выйти", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}