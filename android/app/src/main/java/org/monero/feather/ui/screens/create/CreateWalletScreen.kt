package org.monero.feather.ui.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.monero.feather.domain.model.NetworkType
import org.monero.feather.ui.viewmodel.CreateWalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWalletScreen(
    onNavigateBack: () -> Unit,
    onWalletCreated: () -> Unit,
    viewModel: CreateWalletViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val navigationEvent = remember { viewModel.navigationEvent }

    LaunchedEffect(navigationEvent) {
        navigationEvent.collect { event ->
            when (event) {
                is CreateWalletViewModel.NavigationEvent.ShowSeedPhrase -> {
                    // TODO: Navigate to seed phrase display screen
                }
                is CreateWalletViewModel.NavigationEvent.NavigateToMain -> {
                    onWalletCreated()
                }
            }
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            // Show error snackbar
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Wallet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Wallet Name
            OutlinedTextField(
                value = state.walletName,
                onValueChange = viewModel::updateWalletName,
                label = { Text("Wallet Name") },
                placeholder = { Text("My Wallet") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Password
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::updatePassword,
                label = { Text("Password") },
                placeholder = { Text("Enter password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = viewModel::togglePasswordVisibility) {
                        Text(if (state.isPasswordVisible) "👁" else "👁‍🗨")
                    }
                }
            )

            // Confirm Password
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::updateConfirmPassword,
                label = { Text("Confirm Password") },
                placeholder = { Text("Confirm password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true
            )

            // Network Type
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = state.networkType.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Network") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    NetworkType.values().forEach { network ->
                        DropdownMenuItem(
                            text = { Text(network.name) },
                            onClick = {
                                viewModel.updateNetworkType(network)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Info text
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.infoContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚠️ Important Security Notice",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "• Write down your seed phrase and store it in a safe place\n" +
                                "• Never share your seed phrase with anyone\n" +
                                "• Feather cannot recover your wallet if you lose the seed phrase",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create Button
            Button(
                onClick = viewModel::createWallet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Wallet", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        // Error Snackbar
        error?.let { errorMessage ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter),
                action = {
                    TextButton(onClick = viewModel::clearError) {
                        Text("DISMISS")
                    }
                }
            ) {
                Text(errorMessage)
            }
        }
    }
}
