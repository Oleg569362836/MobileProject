package com.example.newsapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class DateFilterDialog extends DialogFragment {

    public interface DateFilterListener {
        void onDateSelected(String filter);
    }

    private DateFilterListener listener;

    public void setListener(DateFilterListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] filters = {"Сегодня", "За неделю", "За месяц", "За год", "Все время"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Фильтр по дате")
                .setItems(filters, (dialog, which) -> {
                    String selected = filters[which];
                    if (listener != null) {
                        listener.onDateSelected(selected);
                    }
                })
                .setNegativeButton("Отмена", null);
        
        return builder.create();
    }
}