package com.example.mobileherald.repository;

import android.content.Context;
import android.widget.Toast;
import com.example.mobileherald.api.NewsApiService;
import com.example.mobileherald.api.RetrofitClient;
import com.example.mobileherald.database.NewsDatabase;
import com.example.mobileherald.models.Article;
import com.example.mobileherald.models.NewsResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewsRepository {

    private NewsApiService apiService;
    private NewsDatabase database;
    private static final String API_KEY = "05b935b15b334577904ddf9c3e1aefe0";
    private Context context;

    // Параметры пагинации
    private int currentPage = 1;
    private int pageSize = 20;
    private String currentCategory = "";
    private String currentQuery = "";
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // Кэш для каждой категории отдельно
    private Map<String, Set<String>> categoryUrlsCache = new HashMap<>();

    public NewsRepository(Context context) {
        this.context = context;
        apiService = RetrofitClient.getNewsApiService();
        database = NewsDatabase.getInstance(context);
    }

    // Загрузка новостей по категории
    public void fetchNewsByCategory(String category, int page, NewsCallback callback) {
        if (isLoading) return;
        isLoading = true;

        // Сбрасываем кэш URL для новой категории
        if (!currentCategory.equals(category) || page == 1) {
            currentCategory = category;
            currentQuery = "";
            if (!categoryUrlsCache.containsKey(category)) {
                categoryUrlsCache.put(category, new HashSet<>());
            } else if (page == 1) {
                categoryUrlsCache.get(category).clear();
            }
        }

        String categoryForApi = category.isEmpty() ? "general" : category;
        Call<NewsResponse> apiCall = apiService.getTopHeadlines("us", categoryForApi, API_KEY, page, pageSize, null);
        apiCall.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    List<Article> articles = response.body().getArticles();

                    if (articles == null || articles.isEmpty()) {
                        isLastPage = true;
                        callback.onError("Новостей не найдено");
                        return;
                    }

                    // Фильтруем дубликаты и блокированные источники
                    List<Article> uniqueArticles = new ArrayList<>();
                    Set<String> urlCache = categoryUrlsCache.get(category);

                    for (Article article : articles) {
                        String url = article.getUrl();
                        if (url != null && !urlCache.contains(url)) {
                            // Пропускаем заблокированные источники
                            if (isBlockedSource(url)) {
                                continue;
                            }
                            urlCache.add(url);
                            uniqueArticles.add(article);
                        }
                    }

                    if (uniqueArticles.isEmpty() && page == 1) {
                        // Если первая страница пустая - пробуем другие источники
                        loadFromDatabase(callback);
                        return;
                    }

                    // Сохраняем в БД
                    new Thread(() -> {
                        if (page == 1) {
                            database.articleDao().deleteAllArticles();
                        }
                        for (Article article : uniqueArticles) {
                            database.articleDao().insertArticle(article);
                        }
                    }).start();

                    callback.onSuccess(uniqueArticles);
                    currentPage = page;
                } else {
                    handleApiError(response, callback);
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                isLoading = false;
                callback.onError("Ошибка сети: " + t.getMessage());
                loadFromDatabase(callback);
            }
        });
    }

    // Проверка заблокированных источников
    private boolean isBlockedSource(String url) {
        String[] blockedDomains = {
                "investors.com", "cloudflare", "wral.com"
        };
        for (String domain : blockedDomains) {
            if (url.contains(domain)) {
                return true;
            }
        }
        return false;
    }

    // Поиск новостей
    public void searchNews(String query, int page, NewsCallback callback) {
        if (isLoading) return;
        isLoading = true;
        currentQuery = query;

        Call<NewsResponse> call = apiService.searchNews(query, API_KEY, page, pageSize);
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    List<Article> articles = response.body().getArticles();
                    if (articles == null || articles.isEmpty()) {
                        isLastPage = true;
                        callback.onError("Ничего не найдено");
                        return;
                    }

                    // Фильтруем заблокированные источники
                    List<Article> filteredArticles = new ArrayList<>();
                    for (Article article : articles) {
                        if (!isBlockedSource(article.getUrl())) {
                            filteredArticles.add(article);
                        }
                    }

                    callback.onSuccess(filteredArticles);
                    currentPage = page;
                } else {
                    handleApiError(response, callback);
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                isLoading = false;
                callback.onError("Ошибка поиска: " + t.getMessage());
            }
        });
    }

    // Загрузка следующих новостей
    public void loadNextPage(NewsCallback callback) {
        if (!isLoading && !isLastPage) {
            int nextPage = currentPage + 1;
            if (!currentQuery.isEmpty()) {
                searchNews(currentQuery, nextPage, callback);
            } else {
                fetchNewsByCategory(currentCategory, nextPage, callback);
            }
        }
    }

    // Загрузка из БД
    private void loadFromDatabase(NewsCallback callback) {
        new Thread(() -> {
            List<Article> articles = database.articleDao().getAllArticles();
            if (articles != null && !articles.isEmpty()) {
                callback.onSuccess(articles);
            } else {
                callback.onError("Нет сохраненных новостей. Проверьте интернет.");
            }
        }).start();
    }

    // Обработка ошибок API
    private void handleApiError(Response<NewsResponse> response, NewsCallback callback) {
        String errorMsg;
        if (response.code() == 401) {
            errorMsg = "API ключ истёк. Получите новый на newsapi.org";
        } else if (response.code() == 429) {
            errorMsg = "Лимит запросов. Попробуйте через минуту";
        } else if (response.code() == 426) {
            errorMsg = "Обновите приложение";
        } else {
            errorMsg = "Ошибка загрузки. Код: " + response.code();
        }
        callback.onError(errorMsg);
        loadFromDatabase(callback);
    }

    // Сброс пагинации
    public void resetPagination() {
        currentPage = 1;
        isLastPage = false;
        isLoading = false;
        // Не очищаем кэш категории при сбросе
    }

    // Очистка кэша при смене категории
    public void clearCacheForNewCategory() {
        currentPage = 1;
        isLastPage = false;
        isLoading = false;
    }

    public interface NewsCallback {
        void onSuccess(List<Article> articles);
        void onError(String error);
    }
}