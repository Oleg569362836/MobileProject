package com.example.newsapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.newsapp.models.Article;
import java.util.List;

@Dao
public interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertArticle(Article article);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Article> articles);

    @Query("SELECT * FROM articles ORDER BY id DESC")
    List<Article> getAllArticles();

    @Query("SELECT * FROM articles WHERE category = :category ORDER BY id DESC")
    List<Article> getArticlesByCategory(String category);

    @Delete
    void deleteArticle(Article article);

    @Query("DELETE FROM articles")
    void deleteAllArticles();
}