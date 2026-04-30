package com.example.new_newest_llm.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.new_newest_llm.R
import com.example.new_newest_llm.data.model.FeedItem
import com.example.new_newest_llm.data.repository.FavoriteRepository
import com.example.new_newest_llm.databinding.FragmentDetailBinding
import com.example.new_newest_llm.ui.common.FavoriteSyncViewModel
import com.example.new_newest_llm.utils.TokenManager
import com.google.android.material.chip.Chip
import com.google.gson.Gson

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var feedItem: FeedItem
    private lateinit var viewModel: DetailViewModel
    private val syncViewModel: FavoriteSyncViewModel by activityViewModels()
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

        val tokenManager = TokenManager(requireContext().applicationContext)
        val favoriteRepository = FavoriteRepository(tokenManager)
        viewModel = ViewModelProvider(
            this,
            DetailViewModelFactory(favoriteRepository)
        )[DetailViewModel::class.java]
        viewModel.init(feedItem.id, feedItem.isFavorited)

        bindData()
        observeViewModel()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnFavorite.setOnClickListener {
            viewModel.toggleFavorite()
        }

        binding.btnViewOriginal.setOnClickListener {
            openInBrowser(feedItem.url)
        }
    }

    private fun observeViewModel() {
        viewModel.isFavorited.observe(viewLifecycleOwner) { fav ->
            binding.btnFavorite.setImageResource(
                if (fav) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_border
            )
        }

        viewModel.favoriteChanged.observe(viewLifecycleOwner) { change ->
            change?.let { (id, isFav) ->
                syncViewModel.publish(id, isFav)
                viewModel.onFavoriteChangePublished()
            }
        }

        viewModel.toastError.observe(viewLifecycleOwner) { code ->
            code?.let {
                showError(it)
                viewModel.onToastShown()
            }
        }

        viewModel.navigateToLogin.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                Toast.makeText(requireContext(), R.string.err_token_revoked, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack(R.id.loginFragment, false)
                viewModel.onNavigationToLoginHandled()
            }
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

    private fun showError(errorCode: String) {
        val resId = resources.getIdentifier(errorCode, "string", requireContext().packageName)
        val message = if (resId != 0) getString(resId) else errorCode
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
