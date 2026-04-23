package com.example.newsapp.api;

import com.example.newsapp.models.NewsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {

    @GET("top-headlines")
    Call<NewsResponse> getTopHeadlines(
            @Query("country") String country,
            @Query("category") String category,
            @Query("apiKey") String apiKey,
            @Query("page") int page,
            @Query("pageSize") int pageSize,
            @Query("language") String language
    );

    @GET("everything")
    Call<NewsResponse> searchNews(
            @Query("q") String query,
            @Query("apiKey") String apiKey,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @GET("everything")
    Call<NewsResponse> getNewsByDate(
            @Query("q") String query,
            @Query("from") String from,
            @Query("to") String to,
            @Query("apiKey") String apiKey,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );
}