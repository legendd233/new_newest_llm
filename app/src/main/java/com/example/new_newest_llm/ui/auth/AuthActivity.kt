package com.example.new_newest_llm.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.new_newest_llm.R
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
                    navigateToFeed()
                }
                is Result.Error -> {
                    // Token invalid, stay on login page
                    // Token already cleared by repository on 401
                }
            }
        }
    }

    private fun navigateToFeed() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        // Clear the auth back stack, then navigate to feed so user can't go back to login
        navController.navigate(R.id.feedFragment, null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build())
    }
}
