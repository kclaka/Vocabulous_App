package com.aregyan.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aregyan.compose.ui.auth.AuthViewModel
import com.aregyan.compose.ui.auth.ForgotPasswordScreen
import com.aregyan.compose.ui.auth.LoginScreen
import com.aregyan.compose.ui.auth.ProfileScreen
import com.aregyan.compose.ui.auth.RegisterScreen
import com.aregyan.compose.ui.auth.model.AuthState
import com.aregyan.compose.ui.home.HomeScreen
import com.aregyan.compose.ui.vocabulary.AddWordScreen
import com.aregyan.compose.ui.vocabulary.FlashcardScreen

@Composable
fun Navigation(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = when (authState) {
            is AuthState.Authenticated -> Screen.Home.route
            else -> Screen.Login.route
        }
    ) {
        // Auth screens
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Home screen
        composable(Screen.Home.route) {
            HomeScreen(
                navigateToFlashcards = { navController.navigate(Screen.Flashcards.route) },
                navigateToAddWord = { navController.navigate(Screen.AddWord.route) },
                navigateToProfile = { navController.navigate(Screen.Profile.route) },
                navigateToReview = { navController.navigate(Screen.Review.route) },
                navigateToSearch = { navController.navigate(Screen.Search.route) }
            )
        }
        
        // Vocabulary screens
        composable(Screen.Flashcards.route) {
            FlashcardScreen(
                navigateToAddWord = { navController.navigate(Screen.AddWord.route) }
            )
        }
        
        composable(Screen.AddWord.route) {
            AddWordScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        
        // These screens will be implemented later
        composable(Screen.Review.route) {
            // Temporary placeholder
            FlashcardScreen(
                navigateToAddWord = { navController.navigate(Screen.AddWord.route) }
            )
        }
        
        composable(Screen.Search.route) {
            // Temporary placeholder
            FlashcardScreen(
                navigateToAddWord = { navController.navigate(Screen.AddWord.route) }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Flashcards : Screen("flashcards")
    object AddWord : Screen("add_word")
    object Review : Screen("review")
    object Search : Screen("search")
}
