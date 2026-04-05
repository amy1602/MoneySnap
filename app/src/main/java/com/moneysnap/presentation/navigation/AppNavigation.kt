package com.moneysnap.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moneysnap.presentation.auth.AuthViewModel
import com.moneysnap.presentation.auth.LoginScreen
import com.moneysnap.presentation.auth.RegisterScreen
import com.moneysnap.presentation.category.AddCategoryScreen
import com.moneysnap.presentation.category.CategoriesScreen
import com.moneysnap.presentation.home.HomeScreen
import com.moneysnap.presentation.splash.SplashScreen
import com.moneysnap.presentation.transaction.TransactionDetailScreen
import com.moneysnap.presentation.profile.SelectAvatarScreen
import com.moneysnap.presentation.profile.AccountSettingsScreen
import com.moneysnap.presentation.profile.ProfileViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.provideFactory(LocalContext.current)
    )

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onNavigateToNext = {
                val destination = if (authViewModel.isLoggedIn) "home" else "login"
                navController.navigate(destination) {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onRegisterClick = { navController.navigate("register") },
                onLoginSuccess = { 
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onLoginClick = { navController.popBackStack() },
                onRegisterSuccess = { 
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onNavigateToCategories = { navController.navigate("categories") },
                onNavigateToTransaction = { txId -> navController.navigate("transaction_detail?transactionId=$txId") },
                onNavigateToSelectAvatar = { navController.navigate("select_avatar") },
                onNavigateToAccountSettings = { navController.navigate("account_settings") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                profileViewModel = profileViewModel
            )
        }
        composable("categories") {
            CategoriesScreen(
                onBackClick = { navController.popBackStack() },
                onAddCategoryClick = { navController.navigate("add_category") },
                onEditCategoryClick = { categoryId -> 
                    navController.navigate("add_category?categoryId=$categoryId")
                }
            )
        }
        composable(
            route = "add_category?categoryId={categoryId}",
            arguments = listOf(
                navArgument("categoryId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            AddCategoryScreen(
                categoryId = categoryId,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        composable(
            route = "transaction_detail?transactionId={transactionId}",
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")
            if (transactionId != null) {
                TransactionDetailScreen(
                    transactionId = transactionId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        composable("select_avatar") {
            val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()

            SelectAvatarScreen(
                currentAvatarId = uiState.avatarId,
                onSaveAvatar = { avatarId -> profileViewModel.updateAvatar(avatarId) },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("account_settings") {
            AccountSettingsScreen(
                onBackClick = { navController.popBackStack() },
                onSignOut = {
                    profileViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
