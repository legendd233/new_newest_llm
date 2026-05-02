package com.example.new_newest_llm.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.new_newest_llm.MainActivity
import com.example.new_newest_llm.R
import com.example.new_newest_llm.data.repository.AuthRepository
import com.example.new_newest_llm.databinding.FragmentRegisterBinding
import com.example.new_newest_llm.utils.SecurityAnswerHelper
import com.example.new_newest_llm.utils.TokenManager
import java.util.Locale

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RegisterViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tokenManager = TokenManager(requireContext().applicationContext)
        val repository = AuthRepository(tokenManager)
        viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(repository)
        )[RegisterViewModel::class.java]

        viewModel.loadSecurityQuestion()

        binding.btnSubmit.setOnClickListener {
            val username = binding.etUsername.text?.toString() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""
            val confirmPassword = binding.etConfirmPassword.text?.toString() ?: ""
            val securityAnswer = binding.etSecurityAnswer.text?.toString() ?: ""
            val normalizedAnswer = SecurityAnswerHelper.normalize(securityAnswer)
            val error = viewModel.register(username, password, confirmPassword, normalizedAnswer)
            if (error != null) {
                showError(error)
            }
        }

        binding.tvToLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state.questionZh.isNotEmpty() || state.questionEn.isNotEmpty()) {
                val locale = Locale.getDefault()
                binding.tvSecurityQuestion.text = if (locale.language == Locale.CHINESE.language) {
                    state.questionZh
                } else {
                    state.questionEn
                }
            }

            if (state.isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnSubmit.isEnabled = false
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
            }

            if (state.registerSuccess) {
                Toast.makeText(requireContext(), R.string.register_success, Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireActivity(), MainActivity::class.java))
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
