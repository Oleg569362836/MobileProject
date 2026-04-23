package com.example.newsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class NewsDetailActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private Button translateButton;
    private Button openBrowserButton;
    private String currentUrl;
    private String currentTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        currentUrl = getIntent().getStringExtra("news_url");
        currentTitle = getIntent().getStringExtra("news_title");

        toolbar = findViewById(R.id.toolbar);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        translateButton = findViewById(R.id.translateButton);
        openBrowserButton = findViewById(R.id.openBrowserButton);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(currentTitle != null ? currentTitle : "Новость");
        }

        // Настройки WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(ProgressBar.GONE);
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                progressBar.setVisibility(ProgressBar.GONE);
                String errorHtml = "<html><body style='text-align:center; padding:20px;'><h3>😞 Ошибка загрузки</h3>" +
                        "<p>" + description + "</p>" +
                        "<p>Попробуйте открыть в браузере</p>" +
                        "</body></html>";
                webView.loadDataWithBaseURL(null, errorHtml, "text/html", "UTF-8", null);
            }
        });

        // Кнопка перевода (открывает Google Translate)
        translateButton.setOnClickListener(v -> {
            String translateUrl = "https://translate.google.com/translate?sl=en&tl=ru&u=" + Uri.encode(currentUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(translateUrl));
            startActivity(intent);
        });

        // Кнопка открытия в браузере
        openBrowserButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
            startActivity(intent);
        });

        if (currentUrl != null && !currentUrl.isEmpty()) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            webView.loadUrl(currentUrl);
        } else {
            webView.loadData("<html><body><h3>Ссылка недоступна</h3></body></html>", "text/html", "UTF-8");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}