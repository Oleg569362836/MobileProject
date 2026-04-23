package com.example.newsapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newsapp.adapters.SavedAdapter;
import com.example.newsapp.database.NewsDatabase;
import com.example.newsapp.models.Article;
import java.util.ArrayList;
import java.util.List;

public class SavedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SavedAdapter savedAdapter;
    private ProgressBar progressBar;
    private TextView emptyText;
    private Toolbar toolbar;
    private NewsDatabase database;
    private List<Article> savedArticles = new ArrayList<>();
    private LinearLayout selectionBar;
    private TextView selectionCount;
    private MenuItem deleteMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
        selectionBar = findViewById(R.id.selectionBar);
        selectionCount = findViewById(R.id.selectionCount);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("💾 Сохраненные");
        }

        database = NewsDatabase.getInstance(this);

        findViewById(R.id.btn_delete_selected).setOnClickListener(v -> deleteSelected());
        findViewById(R.id.btn_cancel_selection).setOnClickListener(v -> exitSelectionMode());

        loadSavedNews();
    }

    private void loadSavedNews() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        new Thread(() -> {
            List<Article> articles = database.articleDao().getAllArticles();
            runOnUiThread(() -> {
                progressBar.setVisibility(ProgressBar.GONE);
                if (articles != null && !articles.isEmpty()) {
                    savedArticles.clear();
                    savedArticles.addAll(articles);
                    setupAdapter();
                    emptyText.setVisibility(TextView.GONE);
                } else {
                    emptyText.setVisibility(TextView.VISIBLE);
                }
            });
        }).start();
    }

    private void setupAdapter() {
        savedAdapter = new SavedAdapter(this, savedArticles, new SavedAdapter.OnSelectionChangeListener() {
            @Override
            public void onSelectionChanged(int selectedCount) {
                if (selectedCount > 0) {
                    selectionBar.setVisibility(View.VISIBLE);
                    selectionCount.setText(selectedCount + " выбрано");
                    if (deleteMenuItem != null) {
                        deleteMenuItem.setVisible(false);
                    }
                } else {
                    selectionBar.setVisibility(View.GONE);
                    if (deleteMenuItem != null) {
                        deleteMenuItem.setVisible(true);
                    }
                }
            }

            @Override
            public void onDeleteSelected(List<Article> selectedArticles) {
                deleteArticles(selectedArticles);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(savedAdapter);
    }

    private void deleteSelected() {
        List<Article> selected = savedAdapter.getSelectedArticles();
        if (selected.isEmpty()) return;

        new AlertDialog.Builder(this)
                .setTitle("Удалить новости")
                .setMessage("Вы уверены, что хотите удалить " + selected.size() + " новость(и)?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    deleteArticles(selected);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteArticles(List<Article> articlesToDelete) {
        new Thread(() -> {
            for (Article article : articlesToDelete) {
                database.articleDao().deleteArticle(article);
            }
            runOnUiThread(() -> {
                savedArticles.removeAll(articlesToDelete);
                savedAdapter.removeArticles(articlesToDelete);
                Toast.makeText(this, "Удалено " + articlesToDelete.size() + " новостей", Toast.LENGTH_SHORT).show();
                exitSelectionMode();

                if (savedArticles.isEmpty()) {
                    emptyText.setVisibility(TextView.VISIBLE);
                }
            });
        }).start();
    }

    private void exitSelectionMode() {
        if (savedAdapter != null) {
            savedAdapter.setSelectionMode(false);
            savedAdapter.clearSelection();
        }
        selectionBar.setVisibility(View.GONE);
        if (deleteMenuItem != null) {
            deleteMenuItem.setVisible(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.saved_menu, menu);
        deleteMenuItem = menu.findItem(R.id.action_delete_all);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_all) {
            deleteAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAll() {
        if (savedArticles.isEmpty()) return;

        new AlertDialog.Builder(this)
                .setTitle("Удалить всё")
                .setMessage("Вы уверены, что хотите удалить все сохраненные новости?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    new Thread(() -> {
                        database.articleDao().deleteAllArticles();
                        runOnUiThread(() -> {
                            savedArticles.clear();
                            savedAdapter.removeArticles(savedArticles);
                            emptyText.setVisibility(TextView.VISIBLE);
                            Toast.makeText(this, "Все новости удалены", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}