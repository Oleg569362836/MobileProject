package com.example.mobileherald.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.mobileherald.models.Article;
import java.util.List;

@Dao
public interface ArticleDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertArticle(Article article);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Article> articles);
    
    @Query("SELECT * FROM articles ORDER BY id DESC")
    List<Article> getAllArticles();
    
    @Query("SELECT * FROM articles WHERE title LIKE '%' || :query || '%'")
    List<Article> searchArticles(String query);
    
    @Query("DELETE FROM articles")
    void deleteAllArticles();
    
    @Query("SELECT COUNT(*) FROM articles")
    int getCount();
}