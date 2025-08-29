package com.redditviewer.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.redditviewer.domain.model.Post
import com.redditviewer.presentation.auth.AuthViewModel
import com.redditviewer.presentation.auth.LoginScreen
import com.redditviewer.presentation.home.HomeScreen
import com.redditviewer.presentation.postdetail.PostDetailScreen
import com.redditviewer.presentation.theme.RedditTranslatorViewerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            RedditTranslatorViewerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RedditViewerApp()
                }
            }
        }
    }
}

@Composable
fun RedditViewerApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    // 投稿データを一時的に保存する状態
    var currentPost by remember { mutableStateOf<Post?>(null) }

    NavHost(
        navController = navController,
        startDestination = if (authState.isAuthenticated) "home" else "login"
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToPost = { post ->
                    currentPost = post
                    navController.navigate("post_detail")
                }
            )
        }
        
        composable("post_detail") {
            currentPost?.let { post ->
                PostDetailScreen(
                    post = post,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
} 