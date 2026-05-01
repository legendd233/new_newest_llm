package com.example.new_newest_llm.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.new_newest_llm.R
import com.example.new_newest_llm.data.repository.FavoriteRepository
import com.example.new_newest_llm.data.repository.FeedRepository
import com.example.new_newest_llm.databinding.FragmentFeedBinding
import com.example.new_newest_llm.ui.common.FavoriteSyncViewModel
import com.example.new_newest_llm.utils.TokenManager
import com.google.gson.Gson

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FeedViewModel
    private lateinit var adapter: FeedAdapter
    private val gson = Gson()
    private val syncViewModel: FavoriteSyncViewModel by activityViewModels()
    private var savedFirstVisiblePosition = RecyclerView.NO_POSITION
    private var savedFirstVisibleOffset = 0
    private var hasLoadedOnce = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tokenManager = TokenManager(requireContext().applicationContext)
        val repository = FeedRepository(tokenManager)
        val favoriteRepository = FavoriteRepository(tokenManager)
        viewModel = ViewModelProvider(
            this,
            FeedViewModelFactory(repository, favoriteRepository)
        )[FeedViewModel::class.java]

        setupRecyclerView()
        setupBottomNav()
        observeViewModel()

        binding.btnRetry.setOnClickListener {
            viewModel.loadFeed()
        }

        if (!hasLoadedOnce) {
            viewModel.loadFeed()
            hasLoadedOnce = true
        }
    }

    private fun setupRecyclerView() {
        adapter = FeedAdapter(
            onItemClick = { item -> viewModel.onItemClicked(item) },
            onFavoriteClick = { item -> viewModel.toggleFavorite(item) }
        )
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = adapter
    }

    private fun setupBottomNav() {
        binding.navFavorites.setOnClickListener {
            findNavController().navigate(R.id.action_feed_to_favorites)
        }

        binding.navMe.setOnClickListener {
            findNavController().navigate(R.id.action_feed_to_profile)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = View.GONE
            binding.layoutError.visibility = View.GONE
            binding.rvFeed.visibility = View.GONE
            binding.tvEmpty.visibility = View.GONE

            when (state) {
                is FeedUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is FeedUiState.Success -> {
                    binding.rvFeed.visibility = View.VISIBLE
                    adapter.submitList(state.items) {
                        if (savedFirstVisiblePosition != RecyclerView.NO_POSITION) {
                            (binding.rvFeed.layoutManager as? LinearLayoutManager)
                                ?.scrollToPositionWithOffset(
                                    savedFirstVisiblePosition,
                                    savedFirstVisibleOffset
                                )
                            savedFirstVisiblePosition = RecyclerView.NO_POSITION
                            savedFirstVisibleOffset = 0
                        }
                    }
                }
                is FeedUiState.Error -> {
                    binding.layoutError.visibility = View.VISIBLE
                    val errorCode = FeedViewModel.mapErrorCode(state.errorCode)
                    val resId = resources.getIdentifier(errorCode, "string", requireContext().packageName)
                    val message = if (resId != 0) getString(resId) else state.errorCode
                    binding.tvError.text = message
                }
                is FeedUiState.Empty -> {
                    binding.tvEmpty.visibility = View.VISIBLE
                }
            }
        }

        viewModel.navigateToDetail.observe(viewLifecycleOwner) { item ->
            item?.let {
                val json = gson.toJson(it)
                val bundle = Bundle().apply {
                    putString("feed_item_json", json)
                }
                findNavController().navigate(R.id.action_feed_to_detail, bundle)
                viewModel.onNavigationToDetailHandled()
            }
        }

        viewModel.navigateToLogin.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                Toast.makeText(requireContext(), R.string.err_token_revoked, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack(R.id.loginFragment, false)
                viewModel.onNavigationToLoginHandled()
            }
        }

        viewModel.toastError.observe(viewLifecycleOwner) { errorCode ->
            errorCode?.let {
                showError(it)
                viewModel.onToastShown()
            }
        }

        syncViewModel.changes.observe(viewLifecycleOwner) { changeMap ->
            changeMap.forEach { (id, fav) -> viewModel.applyFavoriteChange(id, fav) }
        }
    }

    private fun showError(errorCode: String) {
        val resId = resources.getIdentifier(errorCode, "string", requireContext().packageName)
        val message = if (resId != 0) getString(resId) else errorCode
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        val lm = binding.rvFeed.layoutManager as? LinearLayoutManager
        if (lm != null) {
            val firstPos = lm.findFirstVisibleItemPosition()
            if (firstPos != RecyclerView.NO_POSITION) {
                savedFirstVisiblePosition = firstPos
                savedFirstVisibleOffset = lm.findViewByPosition(firstPos)?.top ?: 0
            }
        }
        super.onDestroyView()
        _binding = null
    }
}
