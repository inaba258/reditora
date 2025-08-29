package com.redditviewer.presentation.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.redditviewer.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OAuthCallbackActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent.data
        if (uri != null && uri.scheme == "redditviewer" && uri.host == "auth") {
            val code = uri.getQueryParameter("code")
            val error = uri.getQueryParameter("error")
            val state = uri.getQueryParameter("state")

            when {
                error != null -> {
                    // エラーハンドリング
                    navigateToMainActivity()
                    finish()
                }
                code != null -> {
                    // 認証コードを処理
                    lifecycleScope.launch {
                        authViewModel.handleOAuthCallback(code)
                        navigateToMainActivity()
                        finish()
                    }
                }
                else -> {
                    // 不正なコールバック
                    navigateToMainActivity()
                    finish()
                }
            }
        } else {
            // 不正なURI
            navigateToMainActivity()
            finish()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
} 