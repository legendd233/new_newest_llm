package com.example.new_newest_llm.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.example.new_newest_llm.data.model.FeedItem;
import java.util.List;

@Entity(tableName = "item")
public class ItemEntity {

    @PrimaryKey
    public int id;

    public String source;
    public String url;
    public String title;

    @ColumnInfo(name = "summary_zh")
    public String summaryZh;

    @ColumnInfo(name = "summary_en")
    public String summaryEn;

    @ColumnInfo(name = "summary_short_en")
    public String summaryShortEn;

    @ColumnInfo(name = "summary_short_zh")
    public String summaryShortZh;

    /** Comma-separated tags from source */
    public String tags;

    @ColumnInfo(name = "semantic_tags")
    public String semanticTags;

    @ColumnInfo(name = "star_count")
    public int starCount;

    @ColumnInfo(name = "published_at")
    public long publishedAt;

    @ColumnInfo(name = "fetched_at")
    public long fetchedAt;

    @ColumnInfo(name = "is_favorited")
    public boolean isFavorited;

    @ColumnInfo(name = "is_read")
    public boolean isRead;

    public static ItemEntity fromFeedItem(FeedItem item) {
        ItemEntity entity = new ItemEntity();
        entity.id = item.id;
        entity.source = item.source;
        entity.url = item.url;
        entity.title = item.title;
        entity.summaryZh = item.summaryZh;
        entity.summaryEn = item.summaryEn;
        entity.summaryShortEn = item.summaryShortEn;
        entity.summaryShortZh = item.summaryShortZh;
        entity.tags = listToStr(item.tags);
        entity.semanticTags = listToStr(item.semanticTags);
        entity.starCount = item.starCount;
        entity.publishedAt = item.publishedAt;
        entity.fetchedAt = item.fetchedAt;
        entity.isFavorited = item.isFavorited;
        return entity;
    }

    private static String listToStr(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join(",", list);
    }
}