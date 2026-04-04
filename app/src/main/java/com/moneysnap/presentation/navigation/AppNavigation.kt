package com.moneysnap.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moneysnap.presentation.auth.AuthViewModel
import com.moneysnap.presentation.auth.LoginScreen
import com.moneysnap.presentation.auth.RegisterScreen
import com.moneysnap.presentation.category.AddCategoryScreen
import com.moneysnap.presentation.category.CategoriesScreen
import com.moneysnap.presentation.home.HomeScreen
import com.moneysnap.presentation.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

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
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("categories") {
            CategoriesScreen(
                onBackClick = { navController.popBackStack() },
                onAddCategoryClick = { navController.navigate("add_category") }
            )
        }
        composable("add_category") {
            AddCategoryScreen(
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
    }
}
