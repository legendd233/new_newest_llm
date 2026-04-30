package com.example.new_newest_llm.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.new_newest_llm.MainActivity
import com.example.new_newest_llm.data.remote.RetrofitClient
import com.example.new_newest_llm.data.repository.AuthRepository
import com.example.new_newest_llm.data.repository.Result
import com.example.new_newest_llm.databinding.ActivityAuthBinding
import com.example.new_newest_llm.utils.TokenManager
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        RetrofitClient.init(tokenManager)

        val token = tokenManager.getToken()
        if (!token.isNullOrEmpty()) {
            checkTokenValidity(token)
        }
    }

    private fun checkTokenValidity(token: String) {
        lifecycleScope.launch {
            val repository = AuthRepository(tokenManager)
            when (repository.validateToken()) {
                is Result.Success -> {
                    startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                    finish()
                }
                is Result.Error -> {
                    // Token invalid, stay on login page
                    // Token already cleared by repository on 401
                }
            }
        }
    }
}
