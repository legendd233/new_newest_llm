package com.example.new_newest_llm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.new_newest_llm.data.local.ItemEntity;
import com.example.new_newest_llm.utils.LocaleHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<ItemEntity> newsList;
    private final OnFavoriteClickListener onFavoriteClick;
    private final OnTranslateListener onTranslateClick;
    private final Context appContext;
    private final Set<Integer> expandedPositions = new HashSet<>();
    private final Set<Integer> translatingPositions = new HashSet<>();
    private final Map<Integer, String> translatedCache = new HashMap<>();

    public interface OnFavoriteClickListener {
        void onClick(ItemEntity item, int position);
    }

    public interface OnTranslateListener {
        void onTranslate(ItemEntity item, int position);
    }

    public NewsAdapter(Context context, List<ItemEntity> newsList,
                       OnFavoriteClickListener favListener,
                       OnTranslateListener translateListener) {
        this.appContext = context.getApplicationContext();
        this.newsList = newsList;
        this.onFavoriteClick = favListener;
        this.onTranslateClick = translateListener;
    }

    public void setTranslating(int position, boolean translating) {
        if (translating) {
            translatingPositions.add(position);
        } else {
            translatingPositions.remove(position);
        }
        notifyItemChanged(position);
    }

    public void setTranslated(int position, String translatedText) {
        translatingPositions.remove(position);
        translatedCache.put(position, translatedText);
        notifyItemChanged(position);
    }

    public void clearTranslating(int position) {
        translatingPositions.remove(position);
        notifyItemChanged(position);
    }

    private boolean isChinese() {
        return LocaleHelper.isChinese(appContext);
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
        ItemEntity item = newsList.get(position);
        boolean expanded = expandedPositions.contains(position);

        // ---- 折叠态 ----
        holder.tvTitle.setText(item.title);
        String preview = pickPreviewText(item);
        holder.tvSummary.setText(preview != null ? preview : "");
        holder.tvTime.setText(formatRelativeTime(item.publishedAt));
        holder.tvSource.setText(formatSourceLabel(item));

        if (item.isFavorited) {
            holder.btnFavorite.setText(isChinese() ? "已收藏" : "Saved");
        } else {
            holder.btnFavorite.setText(isChinese() ? "收藏" : "Save");
        }
        holder.btnFavorite.setOnClickListener(v -> {
            if (onFavoriteClick != null) onFavoriteClick.onClick(item, position);
        });

        // ---- 展开区域 ----
        if (expanded) {
            holder.llDetail.setVisibility(View.VISIBLE);

            // 数据指标行
            boolean hasStats = false;
            if (item.starCount > 0) {
                holder.tvStar.setText(formatStarWithContext(item.source, item.starCount));
                holder.tvStar.setVisibility(View.VISIBLE);
                hasStats = true;
            } else {
                holder.tvStar.setVisibility(View.GONE);
            }

            String pubDate = formatAbsoluteDate(item.publishedAt);
            if (pubDate != null) {
                holder.tvDate.setText(pubDate + (isChinese() ? " 发布" : " published"));
                holder.tvDate.setVisibility(View.VISIBLE);
                hasStats = true;
            } else {
                holder.tvDate.setVisibility(View.GONE);
            }
            holder.llStats.setVisibility(hasStats ? View.VISIBLE : View.GONE);

            // 正文摘要
            String bodyText = pickBodyText(item);
            if (bodyText != null) {
                holder.tvBody.setText(bodyText);
                holder.tvBody.setVisibility(View.VISIBLE);
            } else {
                holder.tvBody.setVisibility(View.GONE);
            }

            // README
            String readme = pickReadme(item);
            if (readme != null) {
                holder.tvLabelReadme.setText("README");
                holder.tvLabelReadme.setVisibility(View.VISIBLE);
                holder.tvReadme.setText(readme);
                holder.tvReadme.setVisibility(View.VISIBLE);
            } else {
                holder.tvLabelReadme.setVisibility(View.GONE);
                holder.tvReadme.setVisibility(View.GONE);
            }

            // 中文翻译
            String zhText = translatedCache.get(position);
            if (zhText != null) {
                holder.tvLabelZh.setText(isChinese() ? "中文翻译" : "Chinese Translation");
                holder.tvLabelZh.setVisibility(View.VISIBLE);
                holder.tvSummaryZh.setText(zhText);
                holder.tvSummaryZh.setVisibility(View.VISIBLE);
            } else {
                holder.tvLabelZh.setVisibility(View.GONE);
                holder.tvSummaryZh.setVisibility(View.GONE);
            }

            // 源链接
            if (item.url != null && !item.url.isEmpty()) {
                holder.tvLabelUrl.setText(isChinese() ? "来源" : "Source");
                holder.tvLabelUrl.setVisibility(View.VISIBLE);
                holder.tvUrl.setText(item.url);
                holder.tvUrl.setVisibility(View.VISIBLE);
            } else {
                holder.tvLabelUrl.setVisibility(View.GONE);
                holder.tvUrl.setVisibility(View.GONE);
            }

            // 抓取时间
            String fetched = formatAbsoluteDate(item.fetchedAt);
            if (fetched != null) {
                holder.tvFetched.setText((isChinese() ? "抓取时间: " : "Fetched: ") + formatFullDatetime(item.fetchedAt));
                holder.tvFetched.setVisibility(View.VISIBLE);
            } else {
                holder.tvFetched.setVisibility(View.GONE);
            }

            // 原始标签
            String tagsStr = formatTags(item.tags);
            if (tagsStr != null) {
                holder.tvLabelTags.setText(isChinese() ? "标签" : "Tags");
                holder.tvLabelTags.setVisibility(View.VISIBLE);
                holder.tvTags.setText(tagsStr);
                holder.tvTags.setVisibility(View.VISIBLE);
            } else {
                holder.tvLabelTags.setVisibility(View.GONE);
                holder.tvTags.setVisibility(View.GONE);
            }

            // AI 语义标签
            String semTagsStr = formatTags(item.semanticTags);
            if (semTagsStr != null) {
                holder.tvLabelSemantic.setText(isChinese() ? "AI 标签" : "AI Tags");
                holder.tvLabelSemantic.setVisibility(View.VISIBLE);
                holder.tvSemanticTags.setText(semTagsStr);
                holder.tvSemanticTags.setVisibility(View.VISIBLE);
            } else {
                holder.tvLabelSemantic.setVisibility(View.GONE);
                holder.tvSemanticTags.setVisibility(View.GONE);
            }

            // 翻译按钮
            boolean isTranslating = translatingPositions.contains(position);
            if (isTranslating) {
                holder.tvTranslate.setText(isChinese() ? "翻译中…" : "Translating…");
            } else {
                holder.tvTranslate.setText(isChinese() ? "翻译为中文" : "Translate");
            }
            holder.tvTranslate.setOnClickListener(v -> {
                if (onTranslateClick != null && !translatingPositions.contains(position)) {
                    onTranslateClick.onTranslate(item, position);
                }
            });

            // 查看原文
            holder.tvOpenUrl.setText(isChinese() ? "查看原文" : "View Original");
            holder.tvOpenUrl.setOnClickListener(v -> {
                if (item.url != null && !item.url.isEmpty()) {
                    android.content.Intent intent = new android.content.Intent(
                            android.content.Intent.ACTION_VIEW, android.net.Uri.parse(item.url));
                    holder.itemView.getContext().startActivity(intent);
                }
            });
        } else {
            holder.llDetail.setVisibility(View.GONE);
        }

        holder.llContainer.setOnClickListener(v -> {
            if (expandedPositions.contains(position)) expandedPositions.remove(position);
            else expandedPositions.add(position);
            notifyItemChanged(position);
        });
    }

    private String pickPreviewText(ItemEntity item) {
        String shortText = isChinese() ? item.summaryShortZh : item.summaryShortEn;
        if (shortText != null && !shortText.isEmpty()) return shortText;
        String fullText = isChinese() ? item.summaryZh : item.summaryEn;
        String stripped = stripReadme(fullText);
        if (stripped != null && !stripped.isEmpty()) return stripped;
        String other = stripReadme(isChinese() ? item.summaryEn : item.summaryZh);
        if (other != null && !other.isEmpty()) return other;
        return null;
    }

    private String pickBodyText(ItemEntity item) {
        String fullText = isChinese() ? item.summaryZh : item.summaryEn;
        String stripped = stripReadme(fullText);
        if (stripped != null && !stripped.isEmpty()) return stripped;
        String other = stripReadme(isChinese() ? item.summaryEn : item.summaryZh);
        if (other != null && !other.isEmpty()) return other;
        String shortText = isChinese() ? item.summaryShortZh : item.summaryShortEn;
        if (shortText != null && !shortText.isEmpty()) return shortText;
        return null;
    }

    private String pickReadme(ItemEntity item) {
        String readme = extractReadme(item.summaryEn);
        if (readme != null && !readme.isEmpty()) return readme;
        readme = extractReadme(item.summaryZh);
        if (readme != null && !readme.isEmpty()) return readme;
        return null;
    }

    private static String stripReadme(String text) {
        if (text == null || text.isEmpty()) return null;
        int idx = text.indexOf("\n\n---\n\n");
        return (idx < 0) ? text : text.substring(0, idx);
    }

    private static String extractReadme(String text) {
        if (text == null || text.isEmpty()) return null;
        int idx = text.indexOf("\n\n---\n\n");
        if (idx < 0) return null;
        String readme = text.substring(idx + 8);
        return readme.isEmpty() ? null : readme;
    }

    private String formatTags(String tagsStr) {
        if (tagsStr == null || tagsStr.trim().isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (String tag : tagsStr.split(",")) {
            String t = tag.trim();
            if (!t.isEmpty()) sb.append("#").append(t).append("  ");
        }
        return sb.toString().trim();
    }

    private String formatRelativeTime(long unixSeconds) {
        if (unixSeconds <= 0) return "";
        long now = System.currentTimeMillis() / 1000;
        long diff = now - unixSeconds;
        if (diff < 3600) {
            long min = diff / 60;
            return isChinese() ? (min + "分钟前") : (min + "m ago");
        } else if (diff < 86400) {
            long hr = diff / 3600;
            return isChinese() ? (hr + "小时前") : (hr + "h ago");
        } else if (diff < 604800) {
            long day = diff / 86400;
            return isChinese() ? (day + "天前") : (day + "d ago");
        }
        return formatAbsoluteDate(unixSeconds);
    }

    private String formatAbsoluteDate(long unixSeconds) {
        if (unixSeconds <= 0) return null;
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(unixSeconds * 1000));
    }

    private String formatFullDatetime(long unixSeconds) {
        if (unixSeconds <= 0) return null;
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(unixSeconds * 1000));
    }

    private String formatSourceLabel(ItemEntity item) {
        String src = item.source;
        if (src == null) return "";
        switch (src) {
            case "github": return "GitHub";
            case "arxiv": return "arXiv";
            case "reddit": return "Reddit";
            case "rss": return "RSS";
            default: return src;
        }
    }

    private String formatStarWithContext(String source, int count) {
        if (count <= 0) return null;
        switch (source) {
            case "github": return "⭐ " + formatStarCount(count) + (isChinese() ? " stars" : " stars");
            case "arxiv": return (isChinese() ? "📄 引用 " : "📄 ") + formatStarCount(count) + (isChinese() ? " 次" : " citations");
            case "reddit": return (isChinese() ? "👍 " : "👍 ") + formatStarCount(count) + (isChinese() ? " 赞" : " upvotes");
            default: return "⭐ " + formatStarCount(count);
        }
    }

    private String formatStarCount(int count) {
        if (count >= 1000) return String.format(Locale.US, "%.1fk", count / 1000.0);
        return String.valueOf(count);
    }

    @Override
    public int getItemCount() { return newsList != null ? newsList.size() : 0; }

    public void updateList(List<ItemEntity> newList) {
        this.newsList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llContainer, llDetail, llStats;
        TextView tvTitle, tvSummary, tvTime, tvSource, tvStar, tvDate, tvBody;
        TextView tvLabelReadme, tvReadme, tvLabelZh, tvSummaryZh, tvLabelSemantic, tvSemanticTags;
        TextView tvLabelTags, tvTags, tvLabelUrl, tvUrl, tvFetched, tvTranslate, tvOpenUrl, btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            llContainer = itemView.findViewById(R.id.ll_container);
            llDetail = itemView.findViewById(R.id.ll_detail);
            llStats = itemView.findViewById(R.id.ll_stats);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvSource = itemView.findViewById(R.id.tv_source);
            tvStar = itemView.findViewById(R.id.tv_star);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvBody = itemView.findViewById(R.id.tv_body);
            tvLabelReadme = itemView.findViewById(R.id.tv_label_readme);
            tvReadme = itemView.findViewById(R.id.tv_readme);
            tvLabelZh = itemView.findViewById(R.id.tv_label_zh);
            tvSummaryZh = itemView.findViewById(R.id.tv_summary_zh);
            tvLabelSemantic = itemView.findViewById(R.id.tv_label_semantic);
            tvSemanticTags = itemView.findViewById(R.id.tv_semantic_tags);
            tvLabelTags = itemView.findViewById(R.id.tv_label_tags);
            tvTags = itemView.findViewById(R.id.tv_tags);
            tvLabelUrl = itemView.findViewById(R.id.tv_label_url);
            tvUrl = itemView.findViewById(R.id.tv_url);
            tvFetched = itemView.findViewById(R.id.tv_fetched);
            tvTranslate = itemView.findViewById(R.id.tv_translate);
            tvOpenUrl = itemView.findViewById(R.id.tv_open_url);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}
