package com.aregyan.compose.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adamglin.phosphoricons.regular.BookOpen
import com.adamglin.phosphoricons.regular.House
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.User
import com.aregyan.compose.ui.auth.AuthViewModel
import com.aregyan.compose.ui.auth.ForgotPasswordScreen
import com.aregyan.compose.ui.auth.LoginScreen
import com.aregyan.compose.ui.auth.ProfileScreen
import com.aregyan.compose.ui.auth.RegisterScreen
import com.aregyan.compose.ui.auth.model.AuthState
import com.aregyan.compose.ui.details.DetailsScreen
import com.aregyan.compose.ui.home.HomeScreen
import com.aregyan.compose.ui.search.SearchScreen
import com.aregyan.compose.ui.users.UsersScreen
import com.aregyan.compose.ui.vocabulary.AddWordScreen
import com.aregyan.compose.ui.vocabulary.FlashcardScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.bold.House

@Composable
fun ComposeApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Define bottom navigation items with Phosphor icons
    val bottomNavItems = listOf(
        BottomNavItem("Home", Route.HOME, PhosphorIcons.Bold.House),
        BottomNavItem("Flashcards", Route.FLASHCARDS_HOME,  PhosphorIcons.Regular.BookOpen),
        BottomNavItem("Add", Route.ADD_WORD, PhosphorIcons.Regular.Plus),
        BottomNavItem("Search", Route.SEARCH, PhosphorIcons.Regular.MagnifyingGlass),
        BottomNavItem("Profile", Route.PROFILE, PhosphorIcons.Regular.User)
    )
    
    // Check if current route is in the authentication flow
    val isAuthScreen = currentDestination?.route in listOf(Route.LOGIN, Route.REGISTER, Route.FORGOT_PASSWORD)
    
    Scaffold(
        bottomBar = {
            // Only show bottom navigation when authenticated and not in auth screens
            if (authState is AuthState.Authenticated && !isAuthScreen) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = when (authState) {
                is AuthState.Authenticated -> Route.HOME
                is AuthState.Unauthenticated -> Route.LOGIN
                else -> Route.LOGIN
            },
            modifier = Modifier.padding(innerPadding)
        ) {
            // Authentication screens
            composable(Route.LOGIN) {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Route.REGISTER) },
                    onNavigateToForgotPassword = { navController.navigate(Route.FORGOT_PASSWORD) },
                    onLoginSuccess = { navController.navigate(Route.HOME) { popUpTo(Route.LOGIN) { inclusive = true } } }
                )
            }
            
            composable(Route.REGISTER) {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = { navController.navigate(Route.HOME) { popUpTo(Route.LOGIN) { inclusive = true } } }
                )
            }
            
            composable(Route.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Route.PROFILE) {
                ProfileScreen(
                    onSignOut = { navController.navigate(Route.LOGIN) { popUpTo(0) { inclusive = true } } }
                )
            }
            
            // Main app screens
            composable(Route.HOME) {
                HomeScreen(
                    navigateToFlashcards = { categoryId -> 
                        navController.navigate("${Route.FLASHCARDS}/$categoryId")
                    },
                    navigateToAddWord = { navController.navigate(Route.ADD_WORD) },
                    navigateToProfile = { navController.navigate(Route.PROFILE) },
                    navigateToReview = { navController.navigate(Route.REVIEW) },
                    navigateToSearch = { navController.navigate(Route.SEARCH) }
                )
            }
            
            // Legacy screens - will be removed later
            composable(Route.USER) { backStackEntry ->
                UsersScreen(
                    onUserClick = { username ->
                        // In order to discard duplicated navigation events, we check the Lifecycle
                        if (backStackEntry.lifecycle.currentState == Lifecycle.State.RESUMED) {
                            navController.navigate("${Route.DETAIL}/$username")
                        }
                    },
                    onProfileClick = { navController.navigate(Route.PROFILE) }
                )
            }
            
            composable(
                route = "${Route.DETAIL}/{${Argument.USERNAME}}",
                arguments = listOf(
                    navArgument(Argument.USERNAME) {
                        type = NavType.StringType
                    }
                ),
            ) {
                DetailsScreen()
            }
            
            // Vocabulary learning screens
            composable(Route.FLASHCARDS_HOME) {
                // Flashcards home screen - shows all categories
                FlashcardScreen(
                    categoryId = null,
                    navigateToAddWord = { navController.navigate(Route.ADD_WORD) },
                    navigateToHome = { navController.navigate(Route.HOME) }
                )
            }
            
            composable(
                route = "${Route.FLASHCARDS}/{${Argument.CATEGORY_ID}}",
                arguments = listOf(
                    navArgument(Argument.CATEGORY_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString(Argument.CATEGORY_ID)
                FlashcardScreen(
                    categoryId = categoryId,
                    navigateToAddWord = { navController.navigate(Route.ADD_WORD) },
                    navigateToHome = { navController.navigate(Route.HOME) }
                )
            }
            
            composable(Route.ADD_WORD) {
                AddWordScreen(
                    navigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Route.REVIEW) {
                // Temporary placeholder - will be implemented later
                FlashcardScreen(
                    navigateToAddWord = { navController.navigate(Route.ADD_WORD) },
                    navigateToHome = { navController.navigate(Route.HOME) }
                )
            }
            
            composable(Route.SEARCH) {
                SearchScreen(
                    onNavigateToWordPackDetail = { wordPackId ->
                        // Navigate to flashcards with the word pack ID
                        navController.navigate("${Route.FLASHCARDS}/$wordPackId")
                    }
                )
            }
        }
    }
}

// Bottom navigation item data class
data class BottomNavItem(val title: String, val route: String, val icon: ImageVector)

object Route {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val PROFILE = "profile"
    const val USER = "user"
    const val DETAIL = "detail"
    
    // New vocabulary routes
    const val HOME = "home"
    const val FLASHCARDS = "flashcards"
    const val FLASHCARDS_HOME = "flashcards_home"
    const val ADD_WORD = "add_word"
    const val REVIEW = "review"
    const val SEARCH = "search"
}

object Argument {
    const val USERNAME = "username"
    const val CATEGORY_ID = "categoryId"
}