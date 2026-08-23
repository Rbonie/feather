package org.monero.feather.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object CreateWallet : Screen("create_wallet")
    object RestoreWallet : Screen("restore_wallet")
    object Login : Screen("login/{walletId}") {
        fun createRoute(walletId: String) = "login/$walletId"
    }
    object Main : Screen("main")
    object Send : Screen("main/send")
    object Receive : Screen("main/receive")
    object History : Screen("main/history")
    object Settings : Screen("main/settings")
    object Coins : Screen("main/coins")
}
