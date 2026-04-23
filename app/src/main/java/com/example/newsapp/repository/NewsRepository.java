package com.example.newsapp.repository;

import android.content.Context;
import com.example.newsapp.api.NewsApiService;
import com.example.newsapp.api.RetrofitClient;
import com.example.newsapp.database.NewsDatabase;
import com.example.newsapp.models.Article;
import com.example.newsapp.models.NewsResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NewsRepository {
    
    private NewsApiService apiService;
    private NewsDatabase database;
    private static final String API_KEY = "05b935b15b334577904ddf9c3e1aefe0";
    private Context context;
    
    private int currentPage = 1;
    private int pageSize = 20;
    private String currentCategory = "general";
    private String currentDateFilter = "Все время";
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private Set<String> loadedUrls = new HashSet<>();
    
    public NewsRepository(Context context) {
        this.context = context;
        apiService = RetrofitClient.getNewsApiService();
        database = NewsDatabase.getInstance(context);
    }
    
    public void setDateFilter(String filter) {
        this.currentDateFilter = filter;
        resetPagination();
    }
    
    public void fetchNewsByCategory(String category, NewsCallback callback) {
        if (!currentCategory.equals(category)) {
            currentCategory = category;
            currentPage = 1;
            isLastPage = false;
            loadedUrls.clear();
        }
        loadPage(currentPage, callback);
    }
    
    private void loadPage(int page, NewsCallback callback) {
        if (isLoading) return;
        isLoading = true;
        
        String fromDate = getFromDate(currentDateFilter);
        String toDate = getCurrentDate();
        
        if (!currentDateFilter.equals("Все время")) {
            String query = currentCategory.equals("general") ? "news" : currentCategory;
            Call<NewsResponse> call = apiService.getNewsByDate(query, fromDate, toDate, API_KEY, page, pageSize);
            
            call.enqueue(new Callback<NewsResponse>() {
                @Override
                public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                    isLoading = false;
                    
                    if (response.isSuccessful() && response.body() != null) {
                        List<Article> articles = response.body().getArticles();
                        processArticles(articles, page, callback);
                    } else {
                        loadFallback(page, callback);
                    }
                }
                
                @Override
                public void onFailure(Call<NewsResponse> call, Throwable t) {
                    isLoading = false;
                    loadFallback(page, callback);
                }
            });
        } else {
            Call<NewsResponse> call = apiService.getTopHeadlines("us", currentCategory, API_KEY, page, pageSize, null);
            
            call.enqueue(new Callback<NewsResponse>() {
                @Override
                public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                    isLoading = false;
                    
                    if (response.isSuccessful() && response.body() != null) {
                        List<Article> articles = response.body().getArticles();
                        processArticles(articles, page, callback);
                    } else {
                        loadFallback(page, callback);
                    }
                }
                
                @Override
                public void onFailure(Call<NewsResponse> call, Throwable t) {
                    isLoading = false;
                    loadFallback(page, callback);
                }
            });
        }
    }
    
    private void processArticles(List<Article> articles, int page, NewsCallback callback) {
        if (articles == null || articles.isEmpty()) {
            isLastPage = true;
            callback.onError("Новостей не найдено");
            return;
        }
        
        List<Article> uniqueArticles = new ArrayList<>();
        for (Article article : articles) {
            if (article.getUrl() != null && !loadedUrls.contains(article.getUrl())) {
                loadedUrls.add(article.getUrl());
                uniqueArticles.add(article);
            }
        }
        
        if (uniqueArticles.isEmpty()) {
            isLastPage = true;
            callback.onError("Новостей не найдено");
            return;
        }
        
        new Thread(() -> {
            if (page == 1) {
                database.articleDao().deleteAllArticles();
            }
            for (Article article : uniqueArticles) {
                article.setCategory(currentCategory);
                database.articleDao().insertArticle(article);
            }
        }).start();
        
        callback.onSuccess(uniqueArticles);
        currentPage = page;
    }
    
    private void loadFallback(int page, NewsCallback callback) {
        Call<NewsResponse> fallbackCall = apiService.getTopHeadlines("us", currentCategory, API_KEY, page, pageSize, null);
        
        fallbackCall.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                isLoading = false;
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Article> articles = response.body().getArticles();
                    processArticles(articles, page, callback);
                } else {
                    loadFromDatabase(callback);
                }
            }
            
            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                isLoading = false;
                loadFromDatabase(callback);
            }
        });
    }
    
    private String getFromDate(String filter) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        switch (filter) {
            case "Сегодня":
                return sdf.format(calendar.getTime());
            case "За неделю":
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                return sdf.format(calendar.getTime());
            case "За месяц":
                calendar.add(Calendar.MONTH, -1);
                return sdf.format(calendar.getTime());
            case "За год":
                calendar.add(Calendar.YEAR, -1);
                return sdf.format(calendar.getTime());
            default:
                return "2020-01-01";
        }
    }
    
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }
    
    public void loadNextPage(NewsCallback callback) {
        if (!isLoading && !isLastPage) {
            loadPage(currentPage + 1, callback);
        }
    }
    
    public void resetPagination() {
        currentPage = 1;
        isLastPage = false;
        isLoading = false;
        loadedUrls.clear();
    }
    
    public boolean hasMorePages() {
        return !isLastPage;
    }
    
    private void loadFromDatabase(NewsCallback callback) {
        new Thread(() -> {
            List<Article> articles = database.articleDao().getAllArticles();
            if (articles != null && !articles.isEmpty()) {
                callback.onSuccess(articles);
            } else {
                callback.onError("Нет сохраненных новостей. Подключитесь к интернету.");
            }
        }).start();
    }
    
    public interface NewsCallback {
        void onSuccess(List<Article> articles);
        void onError(String error);
    }
}