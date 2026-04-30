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
import com.example.new_newest_llm.databinding.FragmentForgotPasswordBinding
import com.example.new_newest_llm.utils.TokenManager
import java.util.Locale

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ForgotPasswordViewModel
    private var questionLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tokenManager = TokenManager(requireContext().applicationContext)
        val repository = AuthRepository(tokenManager)
        viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(repository)
        )[ForgotPasswordViewModel::class.java]

        // Step 1: Load question
        binding.btnLoadQuestion.setOnClickListener {
            viewModel.loadSecurityQuestion()
        }

        // Step 2: Reset password
        binding.btnSubmit.setOnClickListener {
            val username = binding.etUsername.text?.toString() ?: ""
            val securityAnswer = binding.etSecurityAnswer.text?.toString() ?: ""
            val newPassword = binding.etNewPassword.text?.toString() ?: ""
            val error = viewModel.resetPassword(username, securityAnswer, newPassword)
            if (error != null) {
                showError(error)
            }
        }

        binding.tvToLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state.isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnLoadQuestion.isEnabled = false
                binding.btnSubmit.isEnabled = false
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnLoadQuestion.isEnabled = true
                binding.btnSubmit.isEnabled = true
            }

            if (state.questionLoaded) {
                questionLoaded = true
                showStep2(state)
            }

            if (state.resetSuccess) {
                Toast.makeText(requireContext(), R.string.password_reset_success, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }

            state.errorMessage?.let { showError(it) }
        }
    }

    private fun showStep2(state: ForgotPasswordUiState) {
        val locale = Locale.getDefault()
        val questionText = if (locale.language == Locale.CHINESE.language) {
            state.questionZh
        } else {
            state.questionEn
        }
        binding.tvSecurityQuestion.text = questionText
        binding.tvSecurityQuestion.visibility = View.VISIBLE
        binding.tilSecurityAnswer.visibility = View.VISIBLE
        binding.tilNewPassword.visibility = View.VISIBLE
        binding.btnSubmit.visibility = View.VISIBLE
        binding.btnLoadQuestion.visibility = View.GONE
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
