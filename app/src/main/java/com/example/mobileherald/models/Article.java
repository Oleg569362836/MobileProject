package com.example.mobileherald.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

@Entity(tableName = "articles")
public class Article implements Serializable {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @SerializedName("author")
    private String author;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("url")
    private String url;
    
    @SerializedName("urlToImage")
    private String urlToImage;
    
    @SerializedName("publishedAt")
    private String publishedAt;
    
    @SerializedName("content")
    private String content;
    
    public Article() {}
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getUrlToImage() { return urlToImage; }
    public void setUrlToImage(String urlToImage) { this.urlToImage = urlToImage; }
    
    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}