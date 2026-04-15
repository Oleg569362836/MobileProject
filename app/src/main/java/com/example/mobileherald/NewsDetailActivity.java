package com.example.mobileherald;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class NewsDetailActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        String url = getIntent().getStringExtra("news_url");
        String title = getIntent().getStringExtra("news_title");

        toolbar = findViewById(R.id.toolbar);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title != null ? title : "Новость");
        }

        // Настройки WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(ProgressBar.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                progressBar.setVisibility(ProgressBar.GONE);
                view.loadData("<html><body style='text-align:center; padding:20px;'><h3>⚠️ Ошибка загрузки</h3><p>Не удалось загрузить страницу<br>Проверьте подключение к интернету</p></body></html>",
                        "text/html", "UTF-8");
            }
        });

        if (url != null && !url.isEmpty()) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            webView.loadUrl(url);
        } else {
            webView.loadData("<html><body style='text-align:center; padding:20px;'><h3>❌ Ошибка</h3><p>Ссылка на новость отсутствует</p></body></html>",
                    "text/html", "UTF-8");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}