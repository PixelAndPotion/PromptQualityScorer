package com.eduvos.promptqualityscorer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.eduvos.promptqualityscorer.model.HistoryEntity;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistoryEntity> historyList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HistoryEntity history);
    }

    public HistoryAdapter(List<HistoryEntity> historyList, OnItemClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    public void setHistory(List<HistoryEntity> history) {
        historyList = history;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryEntity item = historyList.get(position);
        int score = item.getScore();
        holder.prompt.setText(item.getOriginalPrompt());
        holder.scoreText.setText(holder.itemView.getContext().getString(R.string.percent_score, score));
        holder.progress.setProgress(score);

        int colour;
        if (score >= 80) {
            colour = R.color.green;
        } else if (score >= 40) {
            colour = R.color.orange;
        } else {
            colour = R.color.red;
        }
        holder.progress.setIndicatorColor(ContextCompat.getColor(holder.itemView.getContext(), colour));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView prompt;
        TextView scoreText;
        CircularProgressIndicator progress;

        ViewHolder(View view) {
            super(view);
            prompt = view.findViewById(R.id.textPromptSnippet);
            scoreText = view.findViewById(R.id.historyScore);
            progress = view.findViewById(R.id.historyProgress);
        }
    }
}
