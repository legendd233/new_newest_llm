package com.example.new_newest_llm.ui.favorites

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
import com.example.new_newest_llm.R
import com.example.new_newest_llm.data.repository.FavoriteRepository
import com.example.new_newest_llm.databinding.FragmentFavoritesBinding
import com.example.new_newest_llm.ui.common.FavoriteSyncViewModel
import com.example.new_newest_llm.ui.feed.FeedAdapter
import com.example.new_newest_llm.utils.TokenManager
import com.google.gson.Gson

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FavoritesViewModel
    private lateinit var adapter: FeedAdapter
    private val syncViewModel: FavoriteSyncViewModel by activityViewModels()
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tokenManager = TokenManager(requireContext().applicationContext)
        val favoriteRepository = FavoriteRepository(tokenManager)
        viewModel = ViewModelProvider(
            this,
            FavoritesViewModelFactory(favoriteRepository)
        )[FavoritesViewModel::class.java]

        setupRecyclerView()
        observeViewModel()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.loadFavorites()
        }

        viewModel.loadFavorites()
    }

    private fun setupRecyclerView() {
        adapter = FeedAdapter(
            onItemClick = { item -> viewModel.onItemClicked(item) },
            onFavoriteClick = { item -> viewModel.unfavorite(item) }
        )
        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorites.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = View.GONE
            binding.layoutError.visibility = View.GONE
            binding.rvFavorites.visibility = View.GONE
            binding.tvEmpty.visibility = View.GONE

            when (state) {
                is FavoritesUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is FavoritesUiState.Success -> {
                    binding.rvFavorites.visibility = View.VISIBLE
                    adapter.submitList(state.items)
                }
                is FavoritesUiState.Error -> {
                    binding.layoutError.visibility = View.VISIBLE
                    val errorCode = FavoritesViewModel.mapErrorCode(state.errorCode)
                    val resId = resources.getIdentifier(errorCode, "string", requireContext().packageName)
                    val message = if (resId != 0) getString(resId) else state.errorCode
                    binding.tvError.text = message
                }
                is FavoritesUiState.Empty -> {
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
                findNavController().navigate(R.id.action_favorites_to_detail, bundle)
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

        viewModel.toastError.observe(viewLifecycleOwner) { code ->
            code?.let {
                showError(it)
                viewModel.onToastShown()
            }
        }

        viewModel.favoriteChanged.observe(viewLifecycleOwner) { change ->
            change?.let { (id, isFav) ->
                syncViewModel.publish(id, isFav)
                viewModel.onFavoriteChangePublished()
            }
        }

        syncViewModel.changes.observe(viewLifecycleOwner) { changeMap ->
            changeMap.forEach { (id, fav) -> viewModel.applyExternalChange(id, fav) }
        }
    }

    private fun showError(errorCode: String) {
        val resId = resources.getIdentifier(errorCode, "string", requireContext().packageName)
        val message = if (resId != 0) getString(resId) else errorCode
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
