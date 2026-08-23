package org.monero.feather.ui.screens.main.send

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.monero.feather.ui.viewmodel.SendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    onNavigateBack: () -> Unit,
    viewModel: SendViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Отправить XMR") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
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
            // Поле адреса получателя
            OutlinedTextField(
                value = uiState.address,
                onValueChange = { viewModel.onAddressChange(it) },
                label = { Text("Адрес получателя") },
                placeholder = { Text("Введите адрес Monero") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3,
                trailingIcon = {
                    IconButton(onClick = { /* Scan QR code */ }) {
                        Icon(Icons.Default.QrCode, contentDescription = "Сканировать QR")
                    }
                }
            )
            
            // Поле суммы
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = { viewModel.onAmountChange(it) },
                label = { Text("Сумма (XMR)") },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            
            // Кнопка максимальной суммы
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.setMaxAmount() }) {
                    Text("MAX")
                }
            }
            
            // Поле платежа (опционально)
            OutlinedTextField(
                value = uiState.paymentId,
                onValueChange = { viewModel.onPaymentIdChange(it) },
                label = { Text("ID платежа (опционально)") },
                placeholder = { Text("16-значный hex или 64-значный интегрированный") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Информация о комиссии
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Комиссия сети",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "≈ ${uiState.estimatedFee} XMR",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Время подтверждения: ~20 минут",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Итоговая сумма
            if (uiState.amount.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Итого:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${uiState.totalAmount} XMR",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопка отправки
            Button(
                onClick = { viewModel.sendTransaction() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.isValid && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Отправить", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            // Сообщение об ошибке
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Сообщение об успехе
            if (uiState.isSuccess) {
                Text(
                    text = "Транзакция успешно отправлена!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
