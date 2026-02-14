package com.example.eventfinder.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eventfinder.ui.screens.SplashScreen
import com.example.eventfinder.ui.screens.HomeScreen
import com.example.eventfinder.ui.screens.SearchScreen
import com.example.eventfinder.ui.screens.DetailsScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventfinder.viewmodel.SharedViewModel

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val sharedViewModel: SharedViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onNavigateToHome = {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeScreen(
                viewModel = sharedViewModel,
                onEventClick = { eventId -> navController.navigate("details/$eventId") },
                onSearchClick = { navController.navigate("search") }
            )
        }
        composable("search") {
            SearchScreen(
                viewModel = sharedViewModel,
                onBackClick = { navController.popBackStack() },
                onEventClick = { eventId -> navController.navigate("details/$eventId") }
            )
        }
        composable(
            route = "details/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            DetailsScreen(
                eventId = eventId ?: "",
                viewModel = sharedViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
