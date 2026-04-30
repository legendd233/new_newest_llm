package com.example.new_newest_llm.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.new_newest_llm.R
import com.example.new_newest_llm.data.model.FeedItem
import com.example.new_newest_llm.databinding.FragmentDetailBinding
import com.google.android.material.chip.Chip
import com.google.gson.Gson

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var feedItem: FeedItem
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val json = arguments?.getString("feed_item_json")
        if (json == null) {
            Toast.makeText(requireContext(), R.string.err_network, Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        feedItem = gson.fromJson(json, FeedItem::class.java)
        bindData()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnFavorite.setOnClickListener {
            Toast.makeText(requireContext(), R.string.favorite_coming_soon, Toast.LENGTH_SHORT).show()
        }

        binding.btnViewOriginal.setOnClickListener {
            openInBrowser(feedItem.url)
        }
    }

    private fun bindData() {
        binding.tvPlatform.text = feedItem.source
        binding.tvTitle.text = feedItem.title
        binding.tvAuthor.text = getString(R.string.score_format, String.format("%.2f", feedItem.score))
        binding.tvPublishedAt.text = getString(R.string.published_format, feedItem.formattedPublishedAt)
        binding.tvSummary.text = feedItem.displaySummary

        binding.chipGroupTags.removeAllViews()
        for (tag in feedItem.tags) {
            val chip = Chip(requireContext()).apply {
                text = tag
                isClickable = false
                isCheckable = false
            }
            binding.chipGroupTags.addView(chip)
        }
    }

    private fun openInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.err_network, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
