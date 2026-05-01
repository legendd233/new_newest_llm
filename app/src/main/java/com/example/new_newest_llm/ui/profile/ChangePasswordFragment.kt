package com.example.new_newest_llm.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.new_newest_llm.R
import com.example.new_newest_llm.data.repository.AuthRepository
import com.example.new_newest_llm.data.repository.Result
import com.example.new_newest_llm.databinding.FragmentChangePasswordBinding
import com.example.new_newest_llm.ui.auth.AuthActivity
import com.example.new_newest_llm.utils.TokenManager
import kotlinx.coroutines.launch

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ChangePasswordViewModel
    private lateinit var repository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tokenManager = TokenManager(requireContext().applicationContext)
        repository = AuthRepository(tokenManager)
        viewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(repository)
        )[ChangePasswordViewModel::class.java]

        loadUsername()

        binding.btnSubmit.setOnClickListener {
            val username = binding.etUsername.text?.toString() ?: ""
            val securityAnswer = binding.etSecurityAnswer.text?.toString() ?: ""
            val newPassword = binding.etNewPassword.text?.toString() ?: ""
            val confirmPassword = binding.etConfirmPassword.text?.toString() ?: ""
            val error = viewModel.changePassword(username, securityAnswer, newPassword, confirmPassword)
            if (error != null) {
                showError(error)
            }
        }

        binding.tvBack.setOnClickListener {
            findNavController().navigateUp()
        }

        observeViewModel()
    }

    private fun loadUsername() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = repository.validateToken()) {
                is Result.Success -> {
                    binding.etUsername.setText(result.data.username)
                }
                is Result.Error -> {
                    val resId = resources.getIdentifier("err_token_revoked", "string", requireContext().packageName)
                    val message = if (resId != 0) getString(resId) else "err_token_revoked"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state.isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnSubmit.isEnabled = false
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
            }

            if (state.success) {
                Toast.makeText(requireContext(), R.string.change_password_success, Toast.LENGTH_SHORT).show()
                val intent = Intent(requireActivity(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }

            state.errorMessage?.let { showError(it) }
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
