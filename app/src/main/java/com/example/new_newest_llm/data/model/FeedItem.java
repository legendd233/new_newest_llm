package com.example.new_newest_llm.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FeedItem {
    public int id;
    public String source;
    public String url;
    public String title;

    @SerializedName("summary_zh")
    public String summaryZh;

    @SerializedName("summary_en")
    public String summaryEn;

    @SerializedName("summary_short_en")
    public String summaryShortEn;

    @SerializedName("summary_short_zh")
    public String summaryShortZh;

    public List<String> tags;

    @SerializedName("semantic_tags")
    public List<String> semanticTags;

    @SerializedName("star_count")
    public int starCount;

    @SerializedName("published_at")
    public long publishedAt;

    @SerializedName("fetched_at")
    public long fetchedAt;

    @SerializedName("is_favorited")
    public boolean isFavorited;
}