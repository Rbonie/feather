package org.monero.feather.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.monero.feather.ui.screens.welcome.WelcomeScreen
import org.monero.feather.ui.screens.create.CreateWalletScreen
import org.monero.feather.ui.screens.restore.RestoreWalletScreen
import org.monero.feather.ui.screens.login.LoginScreen
import org.monero.feather.ui.screens.main.MainScreen
import org.monero.feather.ui.screens.main.send.SendScreen
import org.monero.feather.ui.screens.main.receive.ReceiveScreen
import org.monero.feather.ui.screens.main.history.HistoryScreen
import org.monero.feather.ui.screens.main.settings.SettingsScreen
import org.monero.feather.ui.screens.main.coins.CoinsScreen
import org.monero.feather.presentation.settings.SettingsScreen as NewSettingsScreen

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onCreateWallet = {
                    navController.navigate(Screen.CreateWallet.route)
                },
                onRestoreWallet = {
                    navController.navigate(Screen.RestoreWallet.route)
                },
                onOpenWallet = {
                    // Navigate to login or wallet selection
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.CreateWallet.route) {
            CreateWalletScreen(
                onNavigateBack = { navController.popBackStack() },
                onWalletCreated = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.RestoreWallet.route) {
            RestoreWalletScreen(
                onNavigateBack = { navController.popBackStack() },
                onWalletRestored = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.Login.route,
            arguments = listOf(
                navArgument("walletId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
            LoginScreen(
                walletId = walletId,
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToSend = {
                    navController.navigate(Screen.Send.route)
                },
                onNavigateToReceive = {
                    navController.navigate(Screen.Receive.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToCoins = {
                    navController.navigate(Screen.Coins.route)
                },
                onLockWallet = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Send.route) {
            SendScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Receive.route) {
            ReceiveScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionClick = { txHash ->
                    // TODO: Navigate to transaction details
                }
            )
        }
        
        composable(Screen.Settings.route) {
            NewSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Coins.route) {
            CoinsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
