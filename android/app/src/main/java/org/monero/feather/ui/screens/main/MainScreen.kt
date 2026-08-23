package org.monero.feather.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.monero.feather.ui.navigation.Screen

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val screen: Screen
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateTo: (Screen) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val bottomNavItems = listOf(
        BottomNavItem("Send", Icons.Filled.Send, Screen.Send),
        BottomNavItem("Receive", Icons.Filled.QrCode, Screen.Receive),
        BottomNavItem("History", Icons.Filled.History, Screen.History),
        BottomNavItem("Coins", Icons.Filled.AccountBalanceWallet, Screen.Coins),
        BottomNavItem("Settings", Icons.Filled.Settings, Screen.Settings)
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feather Wallet") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            onNavigateTo(item.screen)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Content based on selected tab
            when (selectedTab) {
                0 -> SendTabContent()
                1 -> ReceiveTabContent()
                2 -> HistoryTabContent()
                3 -> CoinsTabContent()
                4 -> SettingsTabContent()
            }
        }
    }
}

@Composable
private fun SendTabContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Send XMR - Coming Soon")
    }
}

@Composable
private fun ReceiveTabContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Receive XMR - Coming Soon")
    }
}

@Composable
private fun HistoryTabContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Transaction History - Coming Soon")
    }
}

@Composable
private fun CoinsTabContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Unspent Outputs - Coming Soon")
    }
}

@Composable
private fun SettingsTabContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Settings - Coming Soon")
    }
}
