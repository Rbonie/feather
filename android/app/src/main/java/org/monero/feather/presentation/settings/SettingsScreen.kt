package org.monero.feather.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.monero.feather.presentation.sync.SyncViewModel

/**
 * Экран настроек с опциями синхронизации и безопасности
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SyncViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Security Section
        SettingsSection(title = "Security") {
            SettingItem(
                title = "Biometric Authentication",
                subtitle = "Use fingerprint or face to unlock wallet",
                isChecked = true,
                onCheckedChange = { /* TODO: Implement */ }
            )
            
            SettingItem(
                title = "Auto-lock Wallet",
                subtitle = "Lock wallet after 5 minutes of inactivity",
                isChecked = true,
                onCheckedChange = { /* TODO: Implement */ }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sync Section
        SettingsSection(title = "Desktop Sync") {
            SettingItem(
                title = "Sync with Desktop",
                subtitle = if (uiState.isSyncInitialized) 
                    "Connected to: ${uiState.desktopDeviceId ?: "Unknown"}" 
                    else "Not connected",
                isChecked = uiState.isSyncEnabled,
                enabled = uiState.isSyncInitialized,
                onCheckedChange = { viewModel.toggleSync(it) }
            )
            
            if (uiState.isSyncInitialized) {
                LastSyncInfo(lastSyncTimestamp = uiState.lastSyncTimestamp)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* TODO: Implement force sync */ },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSyncing
                    ) {
                        Text("Sync Now")
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.clearSessionData { /* TODO: Show confirmation */ } },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Disconnect")
                    }
                }
                
                if (uiState.isSyncing) {
                    LinearProgressIndicator(
                        progress = uiState.syncProgress / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Syncing... ${uiState.syncProgress}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                uiState.syncError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                Button(
                    onClick = { /* TODO: Navigate to QR scan screen */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Connect to Desktop")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Network Section
        SettingsSection(title = "Network") {
            SettingItem(
                title = "Use Testnet",
                subtitle = "Connect to Monero testnet instead of mainnet",
                isChecked = false,
                onCheckedChange = { /* TODO: Implement */ }
            )
            
            SettingItem(
                title = "Remote Node",
                subtitle = "node.moneroworld.com:18089",
                onClick = { /* TODO: Show node selection dialog */ }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // About Section
        SettingsSection(title = "About") {
            ListItem(
                headlineContent = { Text("Version") },
                supportingContent = { Text("1.0.0-alpha") }
            )
            
            ListItem(
                headlineContent = { Text("License") },
                supportingContent = { Text("BSD-3-Clause") }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String? = null,
    isChecked: Boolean = false,
    enabled: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    if (onCheckedChange != null) {
        SwitchItem(
            title = title,
            subtitle = subtitle,
            isChecked = isChecked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    } else if (onClick != null) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { subtitle?.let { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun SwitchItem(
    title: String,
    subtitle: String?,
    isChecked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) 
                    MaterialTheme.colorScheme.onSurface 
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun LastSyncInfo(lastSyncTimestamp: Long?) {
    val lastSyncText = remember(lastSyncTimestamp) {
        when {
            lastSyncTimestamp == null -> "Never synced"
            System.currentTimeMillis() - lastSyncTimestamp < 60000 -> "Just now"
            System.currentTimeMillis() - lastSyncTimestamp < 3600000 -> 
                "${(System.currentTimeMillis() - lastSyncTimestamp) / 60000} minutes ago"
            System.currentTimeMillis() - lastSyncTimestamp < 86400000 -> 
                "${(System.currentTimeMillis() - lastSyncTimestamp) / 3600000} hours ago"
            else -> "${(System.currentTimeMillis() - lastSyncTimestamp) / 86400000} days ago"
        }
    }
    
    Text(
        text = "Last sync: $lastSyncText",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
