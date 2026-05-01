package com.example.new_newest_llm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.new_newest_llm.data.local.ItemEntity;
import com.example.new_newest_llm.utils.LocaleHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<ItemEntity> newsList;
    private final OnFavoriteClickListener onFavoriteClick;
    private final Context appContext;
    private final Set<Integer> expandedPositions = new HashSet<>();

    // 点击监听接口
    public interface OnFavoriteClickListener {
        void onClick(ItemEntity item, int position);
    }

    public NewsAdapter(Context context, List<ItemEntity> newsList, OnFavoriteClickListener listener) {
        this.appContext = context.getApplicationContext();
        this.newsList = newsList;
        this.onFavoriteClick = listener;
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

        // 收藏按钮
        if (item.isFavorited) {
            holder.btnFavorite.setText(isChinese() ? "已收藏" : "Saved");
        } else {
            holder.btnFavorite.setText(isChinese() ? "收藏" : "Save");
        }
        holder.btnFavorite.setOnClickListener(v -> {
            if (onFavoriteClick != null) {
                onFavoriteClick.onClick(item, position);
            }
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

            // 正文：优先当前语言的完整摘要
            String bodyText = pickBodyText(item);
            if (bodyText != null) {
                holder.tvBody.setText(bodyText);
                holder.tvBody.setVisibility(View.VISIBLE);
            } else {
                holder.tvBody.setVisibility(View.GONE);
            }

            // 中文翻译（仅当正文是英文且有中文翻译时才显示）
            String zhText = pickZhOnly(item);
            if (zhText != null) {
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
                holder.tvFetched.setText(
                        (isChinese() ? "抓取时间: " : "Fetched: ") + formatFullDatetime(item.fetchedAt));
                holder.tvFetched.setVisibility(View.VISIBLE);
            } else {
                holder.tvFetched.setVisibility(View.GONE);
            }

            // 原始标签
            String tagsStr = formatTags(item.tags);
            if (tagsStr != null) {
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

            // 查看原文
            holder.tvOpenUrl.setText(isChinese() ? "查看原文 →" : "View Original →");
            holder.tvOpenUrl.setOnClickListener(v -> {
                if (item.url != null && !item.url.isEmpty()) {
                    android.content.Intent intent = new android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(item.url));
                    holder.itemView.getContext().startActivity(intent);
                }
            });

        } else {
            holder.llDetail.setVisibility(View.GONE);
        }

        // 点击卡片主体展开/收起
        holder.llContainer.setOnClickListener(v -> {
            if (expandedPositions.contains(position)) {
                expandedPositions.remove(position);
            } else {
                expandedPositions.add(position);
            }
            notifyItemChanged(position);
        });
    }

    // ---- 摘要选择 ----

    /** 折叠态预览：短摘要 > 当前语言完整摘要 > 另一种语言 */
    private String pickPreviewText(ItemEntity item) {
        String shortText = isChinese() ? item.summaryShortZh : item.summaryShortEn;
        if (shortText != null && !shortText.isEmpty()) return shortText;
        String fullText = isChinese() ? item.summaryZh : item.summaryEn;
        if (fullText != null && !fullText.isEmpty()) return fullText;
        String other = isChinese() ? item.summaryEn : item.summaryZh;
        if (other != null && !other.isEmpty()) return other;
        return null;
    }

    /** 展开态正文：当前语言完整摘要 > 另一种语言 > 短摘要 */
    private String pickBodyText(ItemEntity item) {
        String fullText = isChinese() ? item.summaryZh : item.summaryEn;
        if (fullText != null && !fullText.isEmpty()) return fullText;
        String other = isChinese() ? item.summaryEn : item.summaryZh;
        if (other != null && !other.isEmpty()) return other;
        String shortText = isChinese() ? item.summaryShortZh : item.summaryShortEn;
        if (shortText != null && !shortText.isEmpty()) return shortText;
        return null;
    }

    /** 仅当正文是英文且中文翻译存在时才返回中文文本 */
    private String pickZhOnly(ItemEntity item) {
        // 如果正文已经是中文，不重复显示
        String bodyText = pickBodyText(item);
        if (bodyText != null && bodyText.equals(item.summaryZh)) return null;
        // 有中文内容且与正文不同
        if (item.summaryZh != null && !item.summaryZh.isEmpty()) return item.summaryZh;
        if (item.summaryShortZh != null && !item.summaryShortZh.isEmpty()
                && (bodyText == null || !item.summaryShortZh.equals(bodyText)))
            return item.summaryShortZh;
        return null;
    }

    // ---- 标签 ----

    private String formatTags(String tagsStr) {
        if (tagsStr == null || tagsStr.trim().isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (String tag : tagsStr.split(",")) {
            String t = tag.trim();
            if (!t.isEmpty()) {
                sb.append("#").append(t).append("  ");
            }
        }
        String result = sb.toString().trim();
        return result.isEmpty() ? null : result;
    }

    // ---- 时间格式化 ----

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(unixSeconds * 1000));
    }

    private String formatFullDatetime(long unixSeconds) {
        if (unixSeconds <= 0) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(unixSeconds * 1000));
    }

    // ---- 来源 & star ----

    private String formatSourceLabel(ItemEntity item) {
        String src = item.source;
        if (src == null) return "";
        switch (src) {
            case "github": return "GitHub";
            case "arxiv":  return "arXiv";
            case "reddit": return "Reddit";
            case "rss":    return "RSS";
            default:       return src;
        }
    }

    private String formatStarWithContext(String source, int count) {
        if (count <= 0) return null;
        switch (source) {
            case "github":
                return "⭐ " + formatStarCount(count) + (isChinese() ? " stars" : " stars");
            case "arxiv":
                return (isChinese() ? "📄 引用 " : "📄 ") + formatStarCount(count) + (isChinese() ? " 次" : " citations");
            case "reddit":
                return (isChinese() ? "👍 " : "👍 ") + formatStarCount(count) + (isChinese() ? " 赞" : " upvotes");
            default:
                return "⭐ " + formatStarCount(count);
        }
    }

    private String formatStarCount(int count) {
        if (count >= 1000) {
            return String.format(Locale.US, "%.1fk", count / 1000.0);
        }
        return String.valueOf(count);
    }

    // ---- RecyclerView ----

    @Override
    public int getItemCount() {
        return newsList != null ? newsList.size() : 0;
    }

    public void updateList(List<ItemEntity> newList) {
        this.newsList = newList;
        notifyDataSetChanged();
    }

    // ViewHolder 内部类
    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llContainer, llDetail, llStats;
        TextView tvTitle, tvSummary, tvTime, tvSource;
        TextView tvStar, tvDate;
        TextView tvBody;
        TextView tvLabelZh, tvSummaryZh;
        TextView tvLabelUrl, tvUrl;
        TextView tvFetched;
        TextView tvLabelTags, tvTags;
        TextView tvLabelSemantic, tvSemanticTags;
        TextView tvOpenUrl;
        Button btnFavorite;

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
            tvLabelZh = itemView.findViewById(R.id.tv_label_zh);
            tvSummaryZh = itemView.findViewById(R.id.tv_summary_zh);
            tvLabelUrl = itemView.findViewById(R.id.tv_label_url);
            tvUrl = itemView.findViewById(R.id.tv_url);
            tvFetched = itemView.findViewById(R.id.tv_fetched);
            tvLabelTags = itemView.findViewById(R.id.tv_label_tags);
            tvTags = itemView.findViewById(R.id.tv_tags);
            tvLabelSemantic = itemView.findViewById(R.id.tv_label_semantic);
            tvSemanticTags = itemView.findViewById(R.id.tv_semantic_tags);
            tvOpenUrl = itemView.findViewById(R.id.tv_open_url);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}
