package org.monero.feather.ui.screens.main.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLockWallet: () -> Unit = {},
    onBackupWallet: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onNodesSettings: () -> Unit = {},
    onAboutApp: () -> Unit = {}
) {
    var isDarkMode by remember { mutableStateOf(false) }
    var isBiometricEnabled by remember { mutableStateOf(false) }
    var autoLockTime by remember { mutableStateOf("5 минут") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
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
        ) {
            // Безопасность
            SettingsSection(title = "Безопасность") {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Заблокировать кошелек",
                    subtitle = "Требуется пароль для доступа",
                    onClick = onLockWallet
                )
                
                SettingsItem(
                    icon = Icons.Default.Password,
                    title = "Изменить пароль",
                    subtitle = "Обновить пароль кошелька",
                    onClick = onChangePassword
                )
                
                SwitchSettingItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Биометрия",
                    subtitle = "Использовать отпечаток пальца",
                    checked = isBiometricEnabled,
                    onCheckedChange = { isBiometricEnabled = it }
                )
                
                DropdownSettingItem(
                    icon = Icons.Default.Timer,
                    title = "Автоблокировка",
                    subtitle = "Через $autoLockTime бездействия",
                    value = autoLockTime,
                    options = listOf("1 минута", "5 минут", "15 минут", "30 минут", "Никогда"),
                    onValueSelected = { autoLockTime = it }
                )
            }
            
            // Резервное копирование
            SettingsSection(title = "Резервное копирование") {
                SettingsItem(
                    icon = Icons.Default.Backup,
                    title = "Создать резервную копию",
                    subtitle = "Сохранить мнемоническую фразу",
                    onClick = onBackupWallet
                )
                
                SettingsItem(
                    icon = Icons.Default.FileDownload,
                    title = "Экспорт кошелька",
                    subtitle = "Экспортировать файл кошелька",
                    onClick = { /* Export wallet file */ }
                )
            }
            
            // Сеть
            SettingsSection(title = "Сеть") {
                SettingsItem(
                    icon = Icons.Default.Dns,
                    title = "Узлы (Nodes)",
                    subtitle = "Настройка подключения к сети Monero",
                    onClick = onNodesSettings
                )
                
                SettingsItem(
                    icon = Icons.Default.Wifi,
                    title = "Режим синхронизации",
                    subtitle = "Автоматический",
                    onClick = { /* Sync mode settings */ }
                )
            }
            
            // Приложение
            SettingsSection(title = "Приложение") {
                SwitchSettingItem(
                    icon = Icons.Default.DarkMode,
                    title = "Тёмная тема",
                    subtitle = "Использовать тёмную тему оформления",
                    checked = isDarkMode,
                    onCheckedChange = { isDarkMode = it }
                )
                
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "Язык",
                    subtitle = "Русский",
                    onClick = { /* Language settings */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "О приложении",
                    subtitle = "Версия 1.0.0",
                    onClick = onAboutApp
                )
                
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Лицензия",
                    subtitle = "Open Source License",
                    onClick = { /* Show license */ }
                )
            }
            
            // Опасная зона
            SettingsSection(title = "Опасная зона") {
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Удалить кошелек",
                    subtitle = "Безвозвратно удалить все данные",
                    onClick = { /* Delete wallet with confirmation */ },
                    textColor = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                content = content
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Перейти",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SwitchSettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun <T> DropdownSettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    value: T,
    options: List<T>,
    onValueSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.toString()) },
                            onClick = {
                                onValueSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
