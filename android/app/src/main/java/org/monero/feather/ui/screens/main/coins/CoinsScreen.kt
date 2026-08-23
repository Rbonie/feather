package org.monero.feather.ui.screens.main.coins

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.monero.feather.domain.model.Output

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinsScreen(
    onNavigateBack: () -> Unit
) {
    // Пример данных (в реальности из ViewModel)
    val outputs = remember {
        listOf(
            Output(
                keyImage = "a1b2c3d4...",
                amount = 1.5,
                blockHeight = 2850000,
                confirmations = 12,
                isSpent = false
            ),
            Output(
                keyImage = "e5f6g7h8...",
                amount = 0.75,
                blockHeight = 2849500,
                confirmations = 150,
                isSpent = false
            ),
            Output(
                keyImage = "i9j0k1l2...",
                amount = 2.0,
                blockHeight = 2849000,
                confirmations = 300,
                isSpent = true
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Монеты (Outputs)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (outputs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет доступных выходов",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(outputs.size) { index ->
                    OutputItem(output = outputs[index])
                }
            }
        }
    }
}

@Composable
fun OutputItem(output: Output) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (output.isSpent) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%.6f XMR", output.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (output.isSpent) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                
                AssistChip(
                    onClick = { },
                    label = { 
                        Text(
                            text = if (output.isSpent) "Потрачен" else "Доступен",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    enabled = false,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (output.isSpent) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                )
            }
            
            HorizontalDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Блок: ${output.blockHeight}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${output.confirmations} подтверждений",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "Key: ${output.keyImage.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
