package com.example.mobileherald.repository;

import android.content.Context;
import com.example.mobileherald.api.NewsApiService;
import com.example.mobileherald.api.RetrofitClient;
import com.example.mobileherald.database.NewsDatabase;
import com.example.mobileherald.models.Article;
import com.example.mobileherald.models.NewsResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class NewsRepository {
    
    private NewsApiService apiService;
    private NewsDatabase database;
    private static final String API_KEY = "05b935b15b334577904ddf9c3e1aefe0";
    
    public NewsRepository(Context context) {
        apiService = RetrofitClient.getNewsApiService();
        database = NewsDatabase.getInstance(context);
    }
    
    public void fetchTopHeadlines(final NewsCallback callback) {
        Call<NewsResponse> call = apiService.getTopHeadlines("us", API_KEY);
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Article> articles = response.body().getArticles();
                    new Thread(() -> {
                        database.articleDao().deleteAllArticles();
                        database.articleDao().insertAll(articles);
                    }).start();
                    callback.onSuccess(articles);
                } else {
                    loadFromDatabase(callback);
                }
            }
            
            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                loadFromDatabase(callback);
            }
        });
    }
    
    private void loadFromDatabase(NewsCallback callback) {
        new Thread(() -> {
            List<Article> articles = database.articleDao().getAllArticles();
            if (articles != null && !articles.isEmpty()) {
                callback.onSuccess(articles);
            } else {
                callback.onError("Нет сохраненных новостей");
            }
        }).start();
    }
    
    public void searchNews(String query, final NewsCallback callback) {
        Call<NewsResponse> call = apiService.searchNews(query, API_KEY, 20, 1);
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getArticles());
                } else {
                    callback.onError("Ничего не найдено");
                }
            }
            
            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                callback.onError("Ошибка: " + t.getMessage());
            }
        });
    }
    
    public interface NewsCallback {
        void onSuccess(List<Article> articles);
        void onError(String error);
    }
}