package com.example.journaly.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.journaly.R;
import com.example.journaly.databinding.JournalEntryItemBinding;
import com.example.journaly.model.JournalEntry;
import com.example.journaly.model.User;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.ViewHolder> {

    interface OnEntryClickListener {
        void onEntryClick(int position);
    }

    private Map<String, JournalEntry> entries;
    private Context context;
    private OnEntryClickListener clickListener;


    public JournalEntryAdapter(Map<String, JournalEntry> entries, OnEntryClickListener clickListener, Context context) {
        this.entries = entries;
        this.context = context;
        this.clickListener = clickListener;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.journal_entry_item, parent, false);
        return new JournalEntryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.bind(entries.get(position));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        JournalEntryItemBinding binding;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = JournalEntryItemBinding.bind(itemView);
        }

        private void bind(JournalEntry entry) {

        }
    }
}

