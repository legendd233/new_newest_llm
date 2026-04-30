package com.example.new_newest_llm.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.new_newest_llm.R
import com.example.new_newest_llm.data.model.FeedItem
import com.example.new_newest_llm.databinding.ItemFeedCardBinding

class FeedAdapter(
    private val onItemClick: (FeedItem) -> Unit,
    private val onFavoriteClick: (FeedItem) -> Unit
) : ListAdapter<FeedItem, FeedAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFeedCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemFeedCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FeedItem) {
            binding.tvPlatform.text = item.source
            binding.tvTitle.text = item.title
            binding.tvSummary.text = item.displaySummary
            binding.tvAuthor.text = String.format("%.2f", item.score)
            binding.tvPublishedAt.text = item.formattedPublishedAt

            binding.btnFavorite.setImageResource(
                if (item.isFavorited) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_border
            )

            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnFavorite.setOnClickListener { onFavoriteClick(item) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FeedItem>() {
        override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            return oldItem == newItem
        }
    }
}
