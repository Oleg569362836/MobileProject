package com.example.mobileherald.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mobileherald.R;
import com.example.mobileherald.models.Article;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<Article> articles = new ArrayList<>();
    private OnItemClickListener listener;
    private Random random = new Random();

    // Массив цветов для карточек без картинок
    private int[] colors = {
            Color.parseColor("#FF5722"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#9C27B0"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#00BCD4"),
            Color.parseColor("#E91E63"),
            Color.parseColor("#3F51B5")
    };

    public interface OnItemClickListener {
        void onItemClick(Article article);
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        Article article = articles.get(position);

        String title = article.getTitle();
        String description = article.getDescription();

        holder.titleTextView.setText(title != null && !title.isEmpty() ? title : "Без названия");
        holder.descriptionTextView.setText(description != null && !description.isEmpty() ? description : "Описание отсутствует");

        // Форматируем дату
        String date = article.getPublishedAt();
        if (date != null && date.length() > 10) {
            date = date.substring(0, 10);
        }
        holder.dateTextView.setText(date != null ? date : "");

        // Загрузка изображения или установка цветного фона
        String imageUrl = article.getUrlToImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .timeout(15000)
                    .centerCrop()
                    .into(holder.imageView);
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            // Если нет картинки - ставим заглушку
            holder.imageView.setImageResource(R.drawable.ic_placeholder);
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                String url = article.getUrl();
                if (url != null && !url.isEmpty()) {
                    listener.onItemClick(article);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        TextView dateTextView;
        ImageView imageView;
        CardView cardView;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_title);
            descriptionTextView = itemView.findViewById(R.id.tv_description);
            dateTextView = itemView.findViewById(R.id.tv_date);
            imageView = itemView.findViewById(R.id.iv_news_image);
            cardView = (CardView) itemView;
        }
    }
}