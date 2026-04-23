package com.example.newsapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newsapp.R;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<String> categories;
    private OnCategoryClickListener listener;
    private int selectedPosition = 0;
    private Context context;
    private SharedPreferences prefs;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category, int position);
    }

    public CategoryAdapter(Context context, List<String> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
        this.prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.categoryText.setText(category);
        
        // Применяем размер шрифта
        int fontSizeSetting = prefs.getInt("font_size", 1);
        float textSize = 14f;
        switch (fontSizeSetting) {
            case 0: textSize = 12f; break;
            case 1: textSize = 14f; break;
            case 2: textSize = 18f; break;
        }
        holder.categoryText.setTextSize(textSize);

        if (selectedPosition == position) {
            holder.categoryText.setBackgroundResource(R.drawable.category_selected);
            holder.categoryText.setTextColor(holder.itemView.getContext().getColor(android.R.color.white));
        } else {
            holder.categoryText.setBackgroundResource(R.drawable.category_normal);
            holder.categoryText.setTextColor(holder.itemView.getContext().getColor(android.R.color.black));
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                int oldPos = selectedPosition;
                selectedPosition = currentPosition;
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);
                listener.onCategoryClick(category, currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.categoryText);
        }
    }
}