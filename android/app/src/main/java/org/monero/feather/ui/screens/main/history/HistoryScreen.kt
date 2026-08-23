package org.monero.feather.ui.screens.main.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.monero.feather.domain.model.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onTransactionClick: (String) -> Unit = {}
) {
    // Пример данных (в реальности из ViewModel)
    val transactions = remember {
        listOf(
            Transaction(
                hash = "a1b2c3d4e5f6...",
                amount = 1.5,
                timestamp = System.currentTimeMillis() - 3600000,
                isOutgoing = false,
                confirmations = 12,
                blockHeight = 2850000
            ),
            Transaction(
                hash = "f6e5d4c3b2a1...",
                amount = 0.75,
                timestamp = System.currentTimeMillis() - 86400000,
                isOutgoing = true,
                confirmations = 150,
                blockHeight = 2849500
            ),
            Transaction(
                hash = "9876543210ab...",
                amount = 2.0,
                timestamp = System.currentTimeMillis() - 172800000,
                isOutgoing = false,
                confirmations = 300,
                blockHeight = 2849000
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История транзакций") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Нет транзакций",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Когда вы получите или отправите XMR,\nони появятся здесь",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction.hash) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.isOutgoing) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (transaction.isOutgoing) {
                        Icons.Default.ArrowUpward
                    } else {
                        Icons.Default.ArrowDownward
                    },
                    contentDescription = null,
                    tint = if (transaction.isOutgoing) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (transaction.isOutgoing) "Исходящая" else "Входящая",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (transaction.isOutgoing) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    
                    Text(
                        text = formatTimestamp(transaction.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "${transaction.confirmations} подтверждений",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${if (transaction.isOutgoing) "-" else "+"}${String.format("%.6f", transaction.amount)} XMR",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (transaction.isOutgoing) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                
                Text(
                    text = "~$${String.format("%.2f", transaction.amount * 150)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Только что"
        diff < 3600000 -> "${diff / 60000} мин назад"
        diff < 86400000 -> "${diff / 3600000} ч назад"
        diff < 604800000 -> "${diff / 86400000} дн назад"
        else -> android.text.format.DateFormat.format("dd MMM yyyy", timestamp).toString()
    }
}
