package org.monero.feather.ui.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.monero.feather.ui.theme.FeatherTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onCreateWallet: () -> Unit,
    onRestoreWallet: () -> Unit,
    onOpenWallet: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Logo and Title Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Placeholder for logo - replace with actual logo resource
                Text(
                    text = "Feather Wallet",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "A lightweight Monero wallet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Secure • Private • Decentralized",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            // Action Buttons Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onCreateWallet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Create New Wallet",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                OutlinedButton(
                    onClick = onRestoreWallet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Restore from Seed",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                TextButton(
                    onClick = onOpenWallet,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Open Existing Wallet",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            // Footer
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
