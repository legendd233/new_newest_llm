package com.example.new_newest_llm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<NewsItem> newsList;
    private OnFavoriteClickListener onFavoriteClick;

    // 点击监听接口
    public interface OnFavoriteClickListener {
        void onClick(NewsItem news, int position);
    }

    public NewsAdapter(List<NewsItem> newsList, OnFavoriteClickListener listener) {
        this.newsList = newsList;
        this.onFavoriteClick = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsItem news = newsList.get(position);
        holder.tvTitle.setText(news.getTitle());
        holder.tvSummary.setText(news.getSummary());
        holder.tvTime.setText(news.getTime());
        holder.btnFavorite.setText(news.isFavorited() ? "已收藏" : "收藏");

        holder.btnFavorite.setOnClickListener(v -> {
            if (onFavoriteClick != null) {
                onFavoriteClick.onClick(news, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public void updateList(List<NewsItem> newList) {
        this.newsList = newList;
        notifyDataSetChanged();
    }

    // ViewHolder 内部类
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSummary, tvTime;
        Button btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            tvTime = itemView.findViewById(R.id.tv_time);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}
