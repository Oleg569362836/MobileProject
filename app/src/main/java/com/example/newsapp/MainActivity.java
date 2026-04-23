package com.example.newsapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.newsapp.adapters.CategoryAdapter;
import com.example.newsapp.adapters.NewsAdapter;
import com.example.newsapp.models.Article;
import com.example.newsapp.repository.NewsRepository;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private RecyclerView categoriesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private NewsRepository repository;
    private TextView offlineIndicator;
    private TextView tvDateFilter;
    private Toolbar toolbar;
    private LinearLayoutManager layoutManager;
    private boolean isLoadingMore = false;
    private Button btnSaved, btnSettings, btnFilterDate;

    private List<String> categories = Arrays.asList("Все", "Бизнес", "Развлечения", "Здоровье", "Наука", "Спорт", "Технологии");
    private List<String> categoriesEn = Arrays.asList("general", "business", "entertainment", "health", "science", "sports", "technology");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        offlineIndicator = findViewById(R.id.offlineIndicator);
        btnSaved = findViewById(R.id.btn_saved);
        btnSettings = findViewById(R.id.btn_settings);
        btnFilterDate = findViewById(R.id.btn_filter_date);
        tvDateFilter = findViewById(R.id.tv_date_filter);

        newsAdapter = new NewsAdapter(this);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(newsAdapter);

        setupCategories();
        setupScrollListener();

        repository = new NewsRepository(this);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            repository.resetPagination();
            offlineIndicator.setVisibility(TextView.GONE);
            loadNews("general");
        });

        btnSaved.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavedActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        btnFilterDate.setOnClickListener(v -> showDateFilterDialog());

        loadNews("general");
    }

    private void showDateFilterDialog() {
        DateFilterDialog dialog = new DateFilterDialog();
        dialog.setListener(filter -> {
            tvDateFilter.setText("Фильтр: " + filter);
            repository.setDateFilter(filter);
            loadNews("general");
        });
        dialog.show(getSupportFragmentManager(), "date_filter");
    }

    private void setupCategories() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        categoriesRecyclerView.setLayoutManager(layoutManager);

        categoryAdapter = new CategoryAdapter(this, categories, (category, position) -> {
            repository.resetPagination();
            String categoryEn = categoriesEn.get(position);
            loadNews(categoryEn);
            offlineIndicator.setVisibility(TextView.GONE);
        });
        categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = newsAdapter.getItemCount();

                if (!isLoadingMore && repository.hasMorePages() &&
                        lastVisiblePosition >= totalItemCount - 3) {
                    loadMoreNews();
                }
            }
        });
    }

    private void loadNews(String category) {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        repository.fetchNewsByCategory(category, new NewsRepository.NewsCallback() {
            @Override
            public void onSuccess(List<Article> articles) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    newsAdapter.setArticles(articles);
                    offlineIndicator.setVisibility(TextView.GONE);
                    isLoadingMore = false;
                    if (articles.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Новостей не найдено", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    offlineIndicator.setVisibility(TextView.VISIBLE);
                    isLoadingMore = false;
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadMoreNews() {
        if (isLoadingMore) return;
        isLoadingMore = true;

        newsAdapter.setLoading(true);

        repository.loadNextPage(new NewsRepository.NewsCallback() {
            @Override
            public void onSuccess(List<Article> articles) {
                runOnUiThread(() -> {
                    newsAdapter.addArticles(articles);
                    newsAdapter.setLoading(false);
                    isLoadingMore = false;
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    newsAdapter.setLoading(false);
                    isLoadingMore = false;
                    if (!error.equals("Больше новостей нет")) {
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}