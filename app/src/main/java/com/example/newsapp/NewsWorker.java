package com.example.newsapp;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.newsapp.api.NewsApiService;
import com.example.newsapp.api.RetrofitClient;
import com.example.newsapp.models.NewsResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsWorker extends Worker {

    private static final String API_KEY = "05b935b15b334577904ddf9c3e1aefe0";
    private static int lastNewsCount = 0;

    public NewsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        checkForNewNews();
        return Result.success();
    }

    private void checkForNewNews() {
        NewsApiService apiService = RetrofitClient.getNewsApiService();
        Call<NewsResponse> call = apiService.getTopHeadlines("us", "general", API_KEY, 1, 10, null);

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int currentCount = response.body().getArticles() != null ?
                            response.body().getArticles().size() : 0;

                    // Если появились новые новости
                    if (lastNewsCount > 0 && currentCount > lastNewsCount) {
                        int newCount = currentCount - lastNewsCount;
                        NotificationHelper.showNewsNotification(getApplicationContext(), newCount);
                    }
                    lastNewsCount = currentCount;
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                // Ошибка - ничего не делаем
            }
        });
    }
}