package com.example.mobileherald;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.SearchView;
import com.example.mobileherald.adapters.CategoryAdapter;
import com.example.mobileherald.adapters.NewsAdapter;
import com.example.mobileherald.models.Article;
import com.example.mobileherald.repository.NewsRepository;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private SearchView searchView;
    private Button themeButton;
    private RecyclerView categoriesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private NewsRepository repository;

    // Список категорий на русском и английском
    private List<String> categories = Arrays.asList("Все", "Бизнес", "Развлечения", "Здоровье", "Наука", "Спорт", "Технологии");
    private List<String> categoriesEn = Arrays.asList("", "business", "entertainment", "health", "science", "sports", "technology");
    private String currentCategory = "";
    private String currentQuery = "";
    private boolean isSearchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeMode = ThemeHelper.getThemeMode(this);
        ThemeHelper.applyTheme(themeMode);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupCategories();
        setupSearchView();
        setupThemeButton();
        setupSwipeRefresh();

        repository = new NewsRepository(this);

        // Загружаем новости США (на английском)
        loadNews("us", "");
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.searchView);
        themeButton = findViewById(R.id.themeButton);
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
    }

    private void setupRecyclerView() {
        newsAdapter = new NewsAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(newsAdapter);

        // Пагинация
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (layoutManager.findLastVisibleItemPosition() >= newsAdapter.getItemCount() - 1) {
                    repository.loadNextPage(new NewsRepository.NewsCallback() {
                        @Override
                        public void onSuccess(List<Article> articles) {
                            runOnUiThread(() -> newsAdapter.addArticles(articles));
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show());
                        }
                    });
                }
            }
        });

        // Клик по новости для открытия в WebView
        newsAdapter.setOnItemClickListener(article -> {
            Intent intent = new Intent(MainActivity.this, NewsDetailActivity.class);
            intent.putExtra("news_url", article.getUrl());
            intent.putExtra("news_title", article.getTitle());
            startActivity(intent);
        });
    }

    private void setupCategories() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        categoriesRecyclerView.setLayoutManager(layoutManager);

        categoryAdapter = new CategoryAdapter(categories, (category, position) -> {
            if (position == 0) {
                currentCategory = "";
                repository.clearCacheForNewCategory(); // Очищаем кэш
                loadNews("us", "");
            } else {
                currentCategory = categoriesEn.get(position);
                repository.clearCacheForNewCategory(); // Очищаем кэш
                loadNewsByCategory(currentCategory);
            }
            isSearchMode = false;
            searchView.setQuery("", false);
            searchView.clearFocus();
        });
        categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    currentQuery = query;
                    isSearchMode = true;
                    searchNews(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setupThemeButton() {
        themeButton.setOnClickListener(v -> showThemeDialog());
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (isSearchMode && !currentQuery.isEmpty()) {
                searchNews(currentQuery);
            } else if (!currentCategory.isEmpty()) {
                loadNewsByCategory(currentCategory);
            } else {
                loadNews("us", "");
            }
        });
    }

    private void loadNews(String country, String category) {
        progressBar.setVisibility(View.VISIBLE);
        repository.resetPagination();

        repository.fetchNewsByCategory(category.isEmpty() ? "general" : category, 1, new NewsRepository.NewsCallback() {
            @Override
            public void onSuccess(List<Article> articles) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    newsAdapter.setArticles(articles);
                    if (articles.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Новостей не найдено", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadNewsByCategory(String category) {
        progressBar.setVisibility(View.VISIBLE);
        repository.resetPagination();

        repository.fetchNewsByCategory(category, 1, new NewsRepository.NewsCallback() {
            @Override
            public void onSuccess(List<Article> articles) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    newsAdapter.setArticles(articles);
                    if (articles.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Новостей не найдено", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void searchNews(String query) {
        progressBar.setVisibility(View.VISIBLE);
        repository.resetPagination();

        repository.searchNews(query, 1, new NewsRepository.NewsCallback() {
            @Override
            public void onSuccess(List<Article> articles) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    newsAdapter.setArticles(articles);
                    if (articles.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Ничего не найдено", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showThemeDialog() {
        String[] themes = {"Светлая", "Тёмная", "Системная"};
        int currentTheme = ThemeHelper.getThemeMode(this);

        new AlertDialog.Builder(this)
                .setTitle("Выберите тему")
                .setSingleChoiceItems(themes, currentTheme, (dialog, which) -> {
                    ThemeHelper.saveThemeMode(this, which);
                    ThemeHelper.applyTheme(which);
                    recreate();
                    dialog.dismiss();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}