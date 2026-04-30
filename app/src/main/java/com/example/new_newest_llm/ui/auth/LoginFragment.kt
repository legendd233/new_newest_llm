package com.example.new_newest_llm.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.new_newest_llm.R
import com.example.new_newest_llm.data.repository.AuthRepository
import com.example.new_newest_llm.databinding.FragmentLoginBinding
import com.example.new_newest_llm.utils.TokenManager

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tokenManager = TokenManager(requireContext().applicationContext)
        val repository = AuthRepository(tokenManager)
        viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(repository)
        )[LoginViewModel::class.java]

        binding.btnSubmit.setOnClickListener {
            val username = binding.etUsername.text?.toString() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""
            val error = viewModel.login(username, password)
            if (error != null) {
                showError(error)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgot_password)
        }

        binding.tvToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state.isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnSubmit.isEnabled = false
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
            }

            if (state.loginSuccess) {
                Toast.makeText(requireContext(), R.string.login_success, Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_login_to_feed)
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
