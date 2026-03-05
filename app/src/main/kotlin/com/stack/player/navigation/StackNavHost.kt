package com.stack.player.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stack.feature.gate.GateScreen

@Composable
fun StackNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoute.Gate.route
    ) {
        composable(NavRoute.Gate.route) {
            GateScreen(
                onGateCompleted = {
                    navController.navigate(NavRoute.Main.route) {
                        popUpTo(NavRoute.Gate.route) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoute.Main.route) {
            // TODO: LibraryShellScreen
        }
    }
}
