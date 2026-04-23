package com.example.newsapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.newsapp.NewsDetailActivity;
import com.example.newsapp.R;
import com.example.newsapp.models.Article;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SavedAdapter extends RecyclerView.Adapter<SavedAdapter.SavedViewHolder> {

    private List<Article> articles;
    private Context context;
    private boolean isSelectionMode = false;
    private Set<Integer> selectedPositions = new HashSet<>();
    private OnSelectionChangeListener selectionChangeListener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int selectedCount);
        void onDeleteSelected(List<Article> selectedArticles);
    }

    public SavedAdapter(Context context, List<Article> articles, OnSelectionChangeListener listener) {
        this.context = context;
        this.articles = articles;
        this.selectionChangeListener = listener;
    }

    public void setSelectionMode(boolean enabled) {
        isSelectionMode = enabled;
        if (!enabled) {
            selectedPositions.clear();
            selectionChangeListener.onSelectionChanged(0);
        }
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public List<Article> getSelectedArticles() {
        List<Article> selected = new ArrayList<>();
        for (int position : selectedPositions) {
            if (position < articles.size()) {
                selected.add(articles.get(position));
            }
        }
        return selected;
    }

    public void clearSelection() {
        selectedPositions.clear();
        notifyDataSetChanged();
        selectionChangeListener.onSelectionChanged(0);
    }

    @NonNull
    @Override
    public SavedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved, parent, false);
        return new SavedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedViewHolder holder, int position) {
        Article article = articles.get(position);

        holder.titleTextView.setText(article.getTitle());
        holder.descriptionTextView.setText(article.getDescription());
        holder.dateTextView.setText(article.getPublishedAt());

        if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(article.getUrlToImage())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.checkbox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        holder.checkbox.setChecked(selectedPositions.contains(position));

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedPositions.add(position);
            } else {
                selectedPositions.remove(position);
            }
            selectionChangeListener.onSelectionChanged(selectedPositions.size());
        });

        holder.cardView.setOnClickListener(v -> {
            if (isSelectionMode) {
                holder.checkbox.setChecked(!holder.checkbox.isChecked());
            } else {
                Intent intent = new Intent(context, NewsDetailActivity.class);
                intent.putExtra("news_url", article.getUrl());
                intent.putExtra("news_title", article.getTitle());
                context.startActivity(intent);
            }
        });

        holder.cardView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                setSelectionMode(true);
                holder.checkbox.setChecked(true);
                selectedPositions.add(position);
                selectionChangeListener.onSelectionChanged(selectedPositions.size());
            }
            return true;
        });
    }

    public void removeArticles(List<Article> articlesToRemove) {
        articles.removeAll(articlesToRemove);
        selectedPositions.clear();
        notifyDataSetChanged();
        selectionChangeListener.onSelectionChanged(0);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    static class SavedViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, dateTextView;
        ImageView imageView;
        CheckBox checkbox;
        CardView cardView;

        SavedViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_title);
            descriptionTextView = itemView.findViewById(R.id.tv_description);
            dateTextView = itemView.findViewById(R.id.tv_date);
            imageView = itemView.findViewById(R.id.iv_news_image);
            checkbox = itemView.findViewById(R.id.checkbox);
            cardView = (CardView) itemView;
        }
    }
}