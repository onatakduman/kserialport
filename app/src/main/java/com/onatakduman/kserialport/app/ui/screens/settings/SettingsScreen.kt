package com.onatakduman.kserialport.app.ui.screens.settings

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import com.onatakduman.kserialport.app.ui.designsystem.KspSwitch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.sp
import com.onatakduman.kserialport.app.BuildConfig
import com.onatakduman.kserialport.app.data.model.AutoReplyRule
import com.onatakduman.kserialport.app.data.model.ConnectionProfile
import com.onatakduman.kserialport.app.data.model.ConnectionState
import com.onatakduman.kserialport.app.data.model.LineEnding
import com.onatakduman.kserialport.app.ui.components.ConnectionBannerWithDialog
import com.onatakduman.kserialport.app.ui.components.DropdownSelector
import com.onatakduman.kserialport.app.ui.components.LabeledDropdownSelector
import com.onatakduman.kserialport.app.ui.components.ProBadge
import com.onatakduman.kserialport.app.ui.theme.LocalExtendedColors
import com.onatakduman.kserialport.app.viewmodel.SerialViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    serialViewModel: SerialViewModel,
    onNavigateToAbout: () -> Unit = {}
) {

    val config by serialViewModel.serialConfig.collectAsState()
    val themeMode by serialViewModel.preferencesRepo.themeMode.collectAsState(initial = "dark")
    val dynamicColors by serialViewModel.preferencesRepo.dynamicColors.collectAsState(initial = false)
    val autoScroll by serialViewModel.preferencesRepo.autoScroll.collectAsState(initial = true)
    val showTimestamps by serialViewModel.preferencesRepo.showTimestamps.collectAsState(initial = true)
    val connectionState by serialViewModel.connectionState.collectAsState()
    val isConnected = connectionState is ConnectionState.Connected
    val isProUser by serialViewModel.isProUser.collectAsState()
    val macros by serialViewModel.preferencesRepo.macros.collectAsState(initial = listOf("AT", "AT+RST", "PING", "TEST"))
    val profiles by serialViewModel.preferencesRepo.profiles.collectAsState(initial = emptyList())
    val autoReplyRules by serialViewModel.preferencesRepo.autoReplyRules.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    // Dialog states
    var showMacroDialog by remember { mutableStateOf(false) }
    var editingMacroIndex by remember { mutableStateOf(-1) }
    var macroDialogText by remember { mutableStateOf("") }

    var showProfileDialog by remember { mutableStateOf(false) }
    var profileDialogName by remember { mutableStateOf("") }

    var showAutoReplyDialog by remember { mutableStateOf(false) }
    var editingRuleIndex by remember { mutableStateOf(-1) }
    var ruleTrigger by remember { mutableStateOf("") }
    var ruleResponse by remember { mutableStateOf("") }

    // Macro add/edit dialog
    if (showMacroDialog) {
        AlertDialog(
            onDismissRequest = { showMacroDialog = false },
            shape = RectangleShape,
            title = { Text(if (editingMacroIndex >= 0) "Edit Macro" else "Add Macro") },
            text = {
                OutlinedTextField(
                    value = macroDialogText,
                    onValueChange = { macroDialogText = it },
                    label = { Text("Macro command") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (macroDialogText.isNotBlank()) {
                            val updated = macros.toMutableList()
                            if (editingMacroIndex >= 0) {
                                updated[editingMacroIndex] = macroDialogText
                            } else {
                                updated.add(macroDialogText)
                            }
                            scope.launch { serialViewModel.preferencesRepo.saveMacros(updated) }
                        }
                        showMacroDialog = false
                    },
                    shape = RectangleShape
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showMacroDialog = false }, shape = RectangleShape) { Text("Cancel") }
            }
        )
    }

    // Profile save dialog
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            shape = RectangleShape,
            title = { Text("Save Profile") },
            text = {
                OutlinedTextField(
                    value = profileDialogName,
                    onValueChange = { profileDialogName = it },
                    label = { Text("Profile name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (profileDialogName.isNotBlank()) {
                            val profile = ConnectionProfile(
                                name = profileDialogName,
                                config = config,
                                macros = macros
                            )
                            scope.launch { serialViewModel.preferencesRepo.saveProfile(profile) }
                        }
                        showProfileDialog = false
                        profileDialogName = ""
                    },
                    shape = RectangleShape
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }, shape = RectangleShape) { Text("Cancel") }
            }
        )
    }

    // Auto-reply rule dialog
    if (showAutoReplyDialog) {
        AlertDialog(
            onDismissRequest = { showAutoReplyDialog = false },
            shape = RectangleShape,
            title = { Text(if (editingRuleIndex >= 0) "Edit Rule" else "Add Auto-Reply Rule") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ruleTrigger,
                        onValueChange = { ruleTrigger = it },
                        label = { Text("Trigger (incoming text)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ruleResponse,
                        onValueChange = { ruleResponse = it },
                        label = { Text("Response (auto-send)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (ruleTrigger.isNotBlank() && ruleResponse.isNotBlank()) {
                            val updated = autoReplyRules.toMutableList()
                            val rule = AutoReplyRule(ruleTrigger, ruleResponse)
                            if (editingRuleIndex >= 0) {
                                updated[editingRuleIndex] = rule
                            } else {
                                updated.add(rule)
                            }
                            scope.launch { serialViewModel.preferencesRepo.saveAutoReplyRules(updated) }
                        }
                        showAutoReplyDialog = false
                    },
                    shape = RectangleShape
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAutoReplyDialog = false }, shape = RectangleShape) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LocalExtendedColors.current.headerBg
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ConnectionBannerWithDialog(serialViewModel = serialViewModel)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Account
            SectionTitle("// account")
            Card(modifier = Modifier.fillMaxWidth(), shape = RectangleShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column {
                    if (!isProUser) {
                        // Upgrade to Pro
                        val activity = LocalContext.current as? Activity
                        val price = serialViewModel.billingManager.getFormattedPrice()
                        ListItem(
                            modifier = Modifier
                                .clickable {
                                    activity?.let {
                                        val launched = serialViewModel.billingManager.launchPurchase(it)
                                        if (!launched) {
                                            android.widget.Toast.makeText(it, "Unable to start purchase. Please try again.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                .background(MaterialTheme.colorScheme.tertiary),
                            colors = androidx.compose.material3.ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            ),
                            leadingContent = {
                                Icon(Icons.Default.WorkspacePremium, null, tint = MaterialTheme.colorScheme.onTertiary)
                            },
                            headlineContent = {
                                Text("Upgrade to Pro", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary)
                            },
                            supportingContent = {
                                Text(
                                    if (price != null) "Remove ads & unlock all features · $price"
                                    else "Remove ads, edit macros, export logs & more",
                                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f)
                                )
                            },
                            trailingContent = {
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onTertiary)
                            }
                        )
                        HorizontalDivider()
                    }
                    // Pro Status / Restore Purchases
                    if (isProUser) {
                        val purchaseTime by serialViewModel.billingManager.purchaseTime.collectAsState()
                        val orderId by serialViewModel.billingManager.orderId.collectAsState()
                        val purchaseDate = purchaseTime?.let {
                            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(it))
                        }
                        ListItem(
                            leadingContent = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
                            headlineContent = { Text("Pro Active", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                            supportingContent = {
                                Column {
                                    if (purchaseDate != null) Text("Purchased: $purchaseDate")
                                    if (orderId != null) Text("Order: $orderId", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        )
                    } else {
                        ListItem(
                            modifier = Modifier.clickable { serialViewModel.billingManager.restorePurchases() },
                            leadingContent = { Icon(Icons.Default.Refresh, null) },
                            headlineContent = { Text("Restore Purchases") },
                            supportingContent = { Text("Restore previously purchased pro features") },
                            trailingContent = { Icon(Icons.Default.ChevronRight, null) }
                        )
                    }
                }
            }

            // Serial Port Configuration
            SectionTitle("// serial_port")
            Card(modifier = Modifier.fillMaxWidth(), shape = RectangleShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DropdownSelector(
                        options = listOf(9600, 19200, 38400, 57600, 115200, 230400, 460800, 921600),
                        selected = config.baudRate,
                        onSelect = { serialViewModel.updateConfig(config.copy(baudRate = it)) },
                        enabled = !isConnected,
                        label = "Baud Rate"
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DropdownSelector(
                            options = listOf(5, 6, 7, 8),
                            selected = config.dataBits,
                            onSelect = { serialViewModel.updateConfig(config.copy(dataBits = it)) },
                            enabled = !isConnected,
                            label = "Data Bits",
                            modifier = Modifier.weight(1f)
                        )
                        DropdownSelector(
                            options = listOf(1, 2),
                            selected = config.stopBits,
                            onSelect = { serialViewModel.updateConfig(config.copy(stopBits = it)) },
                            enabled = !isConnected,
                            label = "Stop Bits",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    LabeledDropdownSelector(
                        options = listOf("None" to 0, "Odd" to 1, "Even" to 2),
                        selected = config.parity,
                        onSelect = { serialViewModel.updateConfig(config.copy(parity = it)) },
                        enabled = !isConnected,
                        label = "Parity"
                    )

                    LabeledDropdownSelector(
                        options = LineEnding.entries.map { it.displayName to it.ordinal },
                        selected = config.lineEnding.ordinal,
                        onSelect = {
                            val lineEnding = LineEnding.entries[it]
                            serialViewModel.updateConfig(config.copy(lineEnding = lineEnding))
                        },
                        enabled = !isConnected,
                        label = "Line Ending"
                    )
                }
            }

            // Macros (Pro to edit)
            SectionTitleWithAction(
                text = "// macros",
                actionIcon = Icons.Default.Add,
                actionLabel = "Add",
                enabled = isProUser,
                showProBadge = !isProUser,
                onAction = {
                    editingMacroIndex = -1
                    macroDialogText = ""
                    showMacroDialog = true
                }
            )
            Card(modifier = Modifier.fillMaxWidth(), shape = RectangleShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column {
                    macros.forEachIndexed { index, macro ->
                        if (index > 0) HorizontalDivider()
                        ListItem(
                            headlineContent = { Text(macro) },
                            trailingContent = {
                                if (isProUser) {
                                    Row {
                                        IconButton(onClick = {
                                            editingMacroIndex = index
                                            macroDialogText = macro
                                            showMacroDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(20.dp))
                                        }
                                        IconButton(onClick = {
                                            val updated = macros.toMutableList().also { it.removeAt(index) }
                                            scope.launch { serialViewModel.preferencesRepo.saveMacros(updated) }
                                        }) {
                                            Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Connection Profiles (Pro)
            SectionTitleWithAction(
                text = "// profiles",
                actionIcon = Icons.Default.SaveAs,
                actionLabel = "Save Current",
                enabled = isProUser,
                showProBadge = !isProUser,
                onAction = {
                    profileDialogName = ""
                    showProfileDialog = true
                }
            )
            if (profiles.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RectangleShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    ListItem(
                        headlineContent = {
                            Text(
                                if (isProUser) "No saved profiles" else "Upgrade to Pro to save profiles",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth(), shape = RectangleShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column {
                        profiles.forEachIndexed { index, profile ->
                            if (index > 0) HorizontalDivider()
                            ListItem(
                                headlineContent = { Text(profile.name) },
                                supportingContent = {
                                    Text("${profile.config.path} @ ${profile.config.baudRate}")
                                },
                                trailingContent = {
                                    Row {
                                        TextButton(
                                            onClick = {
                                                serialViewModel.updateConfig(profile.config)
                                                scope.launch { serialViewModel.preferencesRepo.saveMacros(profile.macros) }
                                            },
                                            shape = RectangleShape
                                        ) {
                                            Text("Load")
                                        }
                                        IconButton(onClick = {
                                            scope.launch { serialViewModel.preferencesRepo.deleteProfile(profile.name) }
                                        }) {
                                            Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Auto-Reply Rules (Pro)
            SectionTitleWithAction(
                text = "// auto_reply",
                actionIcon = Icons.Default.Add,
                actionLabel = "Add Rule",
                enabled = isProUser,
                showProBadge = !isProUser,
                onAction = {
                    editingRuleIndex = -1
                    ruleTrigger = ""
                    ruleResponse = ""
                    showAutoReplyDialog = true
                }
            )
            if (autoReplyRules.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RectangleShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    ListItem(
                        headlineContent = {
                            Text(
                                if (isProUser) "No auto-reply rules" else "Upgrade to Pro for auto-reply",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth(), shape = RectangleShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column {
                        autoReplyRules.forEachIndexed { index, rule ->
                            if (index > 0) HorizontalDivider()
                            ListItem(
                                headlineContent = { Text("\"${rule.trigger}\" → \"${rule.response}\"") },
                                trailingContent = {
                                    Row {
                                        KspSwitch(
                                            checked = rule.enabled,
                                            onCheckedChange = { enabled ->
                                                val updated = autoReplyRules.toMutableList()
                                                updated[index] = rule.copy(enabled = enabled)
                                                scope.launch { serialViewModel.preferencesRepo.saveAutoReplyRules(updated) }
                                            }
                                        )
                                        IconButton(onClick = {
                                            val updated = autoReplyRules.toMutableList().also { it.removeAt(index) }
                                            scope.launch { serialViewModel.preferencesRepo.saveAutoReplyRules(updated) }
                                        }) {
                                            Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Display Preferences
            SectionTitle("// display")
            Card(modifier = Modifier.fillMaxWidth(), shape = RectangleShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column {
                    ListItem(
                        headlineContent = { Text("Auto-scroll") },
                        supportingContent = { Text("Scroll to bottom on new data") },
                        trailingContent = {
                            KspSwitch(
                                checked = autoScroll,
                                onCheckedChange = {
                                    scope.launch { serialViewModel.preferencesRepo.saveAutoScroll(it) }
                                }
                            )
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Show timestamps") },
                        supportingContent = { Text("Display time for each log entry") },
                        trailingContent = {
                            KspSwitch(
                                checked = showTimestamps,
                                onCheckedChange = {
                                    scope.launch { serialViewModel.preferencesRepo.saveShowTimestamps(it) }
                                }
                            )
                        }
                    )
                }
            }

            // Theme
            SectionTitle("// appearance")
            Card(modifier = Modifier.fillMaxWidth(), shape = RectangleShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Theme", style = MaterialTheme.typography.bodyMedium)
                    val themeOptions = listOf("system", "light", "dark")
                    val themeLabels = listOf("System", "Light", "Dark")
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        themeOptions.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = themeMode == option,
                                onClick = {
                                    scope.launch { serialViewModel.preferencesRepo.saveThemeMode(option) }
                                },
                                shape = RectangleShape,
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.primary,
                                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                    inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                                    activeBorderColor = MaterialTheme.colorScheme.primary,
                                    inactiveBorderColor = MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Text(themeLabels[index])
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Dynamic colors", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Material You wallpaper colors",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            KspSwitch(
                                checked = dynamicColors,
                                onCheckedChange = {
                                    scope.launch { serialViewModel.preferencesRepo.saveDynamicColors(it) }
                                }
                            )
                        }
                    }
                }
            }

            // About
            SectionTitle("// about")
            Card(modifier = Modifier.fillMaxWidth(), shape = RectangleShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column {
                    // App name + version
                    ListItem(
                        leadingContent = {
                            Icon(Icons.Default.Cable, null, tint = MaterialTheme.colorScheme.primary)
                        },
                        headlineContent = { Text("KSerialPort", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("v${BuildConfig.VERSION_NAME}") }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Library") },
                        supportingContent = { Text("kserialport v1.1.9") }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Developer") },
                        supportingContent = { Text("Onat Akduman") }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("License") },
                        supportingContent = { Text("Apache 2.0") }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Source Code") },
                        supportingContent = { Text("github.com/onatakduman/kserialport") }
                    )
                }
            }

            // DEBUG: Activate Pro toggle (only in debug builds)
            if (BuildConfig.DEBUG) {
                SectionTitle("// debug")
                Card(modifier = Modifier.fillMaxWidth(), shape = RectangleShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    ListItem(
                        headlineContent = { Text("Activate Pro (debug)") },
                        supportingContent = { Text("Toggle pro features for testing") },
                        trailingContent = {
                            KspSwitch(
                                checked = isProUser,
                                onCheckedChange = {
                                    scope.launch { serialViewModel.preferencesRepo.saveProPurchased(it) }
                                }
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SectionTitleWithAction(
    text: String,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector,
    actionLabel: String,
    enabled: Boolean,
    showProBadge: Boolean,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            if (showProBadge) {
                ProBadge()
            }
        }
        if (enabled) {
            TextButton(onClick = onAction, shape = RectangleShape) {
                Text(actionLabel, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
