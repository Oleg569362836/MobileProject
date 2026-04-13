package com.example.mobileherald;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.mobileherald.adapters.NewsAdapter;
import com.example.mobileherald.models.Article;
import com.example.mobileherald.repository.NewsRepository;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private NewsRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);

        newsAdapter = new NewsAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(newsAdapter);

        repository = new NewsRepository(this);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadNews();
        });

        loadNews();
    }

    private void loadNews() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        repository.fetchTopHeadlines(new NewsRepository.NewsCallback() {
            @Override
            public void onSuccess(List<Article> articles) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    newsAdapter.setArticles(articles);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}