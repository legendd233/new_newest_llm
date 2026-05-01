package com.example.new_newest_llm.ui.profile

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.new_newest_llm.R
import com.example.new_newest_llm.data.repository.AuthRepository
import com.example.new_newest_llm.databinding.FragmentProfileBinding
import com.example.new_newest_llm.ui.auth.AuthActivity
import com.example.new_newest_llm.utils.TokenManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext().applicationContext)
        val repository = AuthRepository(tokenManager)
        viewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(repository)
        )[ProfileViewModel::class.java]

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.itemFavorites.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_favorites)
        }

        binding.itemChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_change_password)
        }

        binding.itemLanguage.setOnClickListener {
            showLanguageDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        observeViewModel()
        viewModel.loadUser()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.progressUsername.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            if (state.username.isNotEmpty()) {
                binding.tvUsername.text = state.username
            }

            state.errorMessage?.let { code ->
                val resId = resources.getIdentifier(code, "string", requireContext().packageName)
                val message = if (resId != 0) getString(resId) else code
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLanguageDialog() {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val currentTag = if (currentLocales.isEmpty) "" else currentLocales[0]?.language ?: ""

        val options = arrayOf(
            getString(R.string.language_follow_system),
            getString(R.string.language_chinese),
            getString(R.string.language_english)
        )
        val tags = arrayOf("", "zh", "en")
        val checkedIndex = tags.indexOfFirst { it == currentTag }.coerceAtLeast(0)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.menu_language)
            .setSingleChoiceItems(options, checkedIndex) { dialog: DialogInterface, which: Int ->
                val tag = tags[which]
                val localeList = if (tag.isEmpty()) {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(tag)
                }
                AppCompatDelegate.setApplicationLocales(localeList)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirm)
            .setPositiveButton(R.string.logout) { _: DialogInterface, _: Int ->
                tokenManager.clearToken()
                Toast.makeText(requireContext(), R.string.logout_success, Toast.LENGTH_SHORT).show()
                val intent = Intent(requireActivity(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
