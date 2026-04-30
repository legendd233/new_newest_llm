package com.example.new_newest_llm.data.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FeedItem(
    val id: Int,
    val source: String,
    @SerializedName("source_id")
    val sourceId: String? = null,
    val url: String,
    val title: String,
    @SerializedName("summary_zh")
    val summaryZh: String?,
    @SerializedName("summary_en")
    val summaryEn: String?,
    val tags: List<String>,
    val score: Float,
    @SerializedName("published_at")
    val publishedAt: Long,
    @SerializedName("fetched_at")
    val fetchedAt: Long,
    @SerializedName("is_favorited")
    val isFavorited: Boolean
) {
    val displaySummary: String
        get() = summaryZh ?: summaryEn ?: ""

    val formattedPublishedAt: String
        get() {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                sdf.format(Date(publishedAt * 1000L))
            } catch (e: Exception) {
                publishedAt.toString()
            }
        }
}

data class FeedResponse(
    val items: List<FeedItem>,
    @SerializedName("has_more")
    val hasMore: Boolean = false
)
