package com.example.newsapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.newsapp.NewsDetailActivity;
import com.example.newsapp.R;
import com.example.newsapp.database.NewsDatabase;
import com.example.newsapp.models.Article;
import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;

    private List<Article> articles = new ArrayList<>();
    private Context context;
    private boolean isLoading = false;
    private NewsDatabase database;
    private SharedPreferences prefs;

    public NewsAdapter(Context context) {
        this.context = context;
        this.database = NewsDatabase.getInstance(context);
        this.prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoading && position == articles.size()) {
            return TYPE_LOADING;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_news, parent, false);
            return new NewsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NewsViewHolder) {
            Article article = articles.get(position);
            NewsViewHolder newsHolder = (NewsViewHolder) holder;

            // Размер шрифта из настроек
            int fontSizeSetting = prefs.getInt("font_size", 1);
            float titleSize = 16f, descSize = 13f, dateSize = 11f;
            switch (fontSizeSetting) {
                case 0: titleSize = 14f; descSize = 11f; dateSize = 9f; break;
                case 1: titleSize = 16f; descSize = 13f; dateSize = 11f; break;
                case 2: titleSize = 20f; descSize = 16f; dateSize = 14f; break;
            }
            newsHolder.titleTextView.setTextSize(titleSize);
            newsHolder.descriptionTextView.setTextSize(descSize);
            newsHolder.dateTextView.setTextSize(dateSize);

            newsHolder.titleTextView.setText(article.getTitle());
            newsHolder.descriptionTextView.setText(article.getDescription());
            newsHolder.dateTextView.setText(article.getPublishedAt());

            // Загрузка картинки
            if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(article.getUrlToImage())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(newsHolder.imageView);
            } else {
                newsHolder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Открытие новости
            newsHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, NewsDetailActivity.class);
                intent.putExtra("news_url", article.getUrl());
                intent.putExtra("news_title", article.getTitle());
                context.startActivity(intent);
            });

            // Кнопка поделиться
            newsHolder.shareButton.setOnClickListener(v -> shareNews(article));

            // Кнопка сохранить
            newsHolder.saveButton.setOnClickListener(v -> saveNews(article));
        }
    }

    private void shareNews(Article article) {
        String shareText = "📰 " + article.getTitle() + "\n\n" + article.getUrl();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        context.startActivity(Intent.createChooser(shareIntent, "Поделиться"));
    }

    private void saveNews(Article article) {
        new Thread(() -> {
            try {
                database.articleDao().insertArticle(article);
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "✅ Новость сохранена", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "❌ Ошибка", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return articles.size() + (isLoading ? 1 : 0);
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
        notifyDataSetChanged();
    }

    public void addArticles(List<Article> newArticles) {
        int startPosition = articles.size();
        articles.addAll(newArticles);
        notifyItemRangeInserted(startPosition, newArticles.size());
    }

    public void setLoading(boolean loading) {
        if (isLoading != loading) {
            isLoading = loading;
            if (loading) notifyItemInserted(articles.size());
            else notifyItemRemoved(articles.size());
        }
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, dateTextView;
        ImageView imageView;
        Button shareButton, saveButton;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_title);
            descriptionTextView = itemView.findViewById(R.id.tv_description);
            dateTextView = itemView.findViewById(R.id.tv_date);
            imageView = itemView.findViewById(R.id.iv_news_image);
            shareButton = itemView.findViewById(R.id.btn_share);
            saveButton = itemView.findViewById(R.id.btn_save);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}