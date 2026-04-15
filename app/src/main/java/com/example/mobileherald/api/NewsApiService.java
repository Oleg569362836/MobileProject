package com.example.mobileherald.api;

import com.example.mobileherald.models.NewsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {

    // Получение новостей по категории и стране
    @GET("top-headlines")
    Call<NewsResponse> getTopHeadlines(
            @Query("country") String country,
            @Query("category") String category,
            @Query("apiKey") String apiKey,
            @Query("page") int page,
            @Query("pageSize") int pageSize,
            @Query("language") String language
    );

    // Поиск новостей
    @GET("everything")
    Call<NewsResponse> searchNews(
            @Query("q") String query,
            @Query("apiKey") String apiKey,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    // Получение новостей по стране
    @GET("top-headlines")
    Call<NewsResponse> getTopHeadlinesByCountry(
            @Query("country") String country,
            @Query("apiKey") String apiKey,
            @Query("page") int page,
            @Query("pageSize") int pageSize,
            @Query("language") String language
    );
}