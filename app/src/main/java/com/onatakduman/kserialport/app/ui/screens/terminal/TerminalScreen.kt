package com.onatakduman.kserialport.app.ui.screens.terminal

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.onatakduman.kserialport.app.data.model.ConnectionState
import com.onatakduman.kserialport.app.data.model.DisplayMode
import com.onatakduman.kserialport.app.data.model.LogEntry
import com.onatakduman.kserialport.app.data.model.LogType
import com.onatakduman.kserialport.app.ui.components.AdBanner
import com.onatakduman.kserialport.app.ui.components.ConnectionBannerWithDialog
import com.onatakduman.kserialport.app.ui.components.ConnectionIndicator
import com.onatakduman.kserialport.app.ui.components.ProBadge
import com.onatakduman.kserialport.app.ui.theme.LocalExtendedColors
import com.onatakduman.kserialport.app.ui.theme.TerminalError
import com.onatakduman.kserialport.app.ui.theme.TerminalRx
import com.onatakduman.kserialport.app.ui.theme.TerminalSystem
import com.onatakduman.kserialport.app.ui.theme.TerminalTimestamp
import com.onatakduman.kserialport.app.ui.theme.TerminalTx
import com.onatakduman.kserialport.app.viewmodel.SerialEvent
import com.onatakduman.kserialport.app.viewmodel.SerialViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(serialViewModel: SerialViewModel) {
    val context = LocalContext.current
    val connectionState by serialViewModel.connectionState.collectAsState()
    val config by serialViewModel.serialConfig.collectAsState()
    val macros by serialViewModel.preferencesRepo.macros.collectAsState(initial = listOf("AT", "AT+RST", "PING", "TEST"))
    val isProUser by serialViewModel.isProUser.collectAsState()
    val autoReplyRules by serialViewModel.preferencesRepo.autoReplyRules.collectAsState(initial = emptyList())

    val logs by serialViewModel.logs.collectAsState()
    var writeData by remember { mutableStateOf("") }
    var displayMode by remember { mutableStateOf(DisplayMode.ASCII) }
    val rxBytes by serialViewModel.rxBytes.collectAsState()
    val txBytes by serialViewModel.txBytes.collectAsState()
    val errors by serialViewModel.errors.collectAsState()

    val showToolbar by serialViewModel.preferencesRepo.showToolbar.collectAsState(initial = true)
    val showMacros by serialViewModel.preferencesRepo.showMacros.collectAsState(initial = true)

    // Periodic macro state
    var showPeriodicDialog by remember { mutableStateOf(false) }
    var periodicMacro by remember { mutableStateOf("") }
    var periodicInterval by remember { mutableStateOf("1000") }
    var isPeriodicActive by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }
    val isConnected = connectionState is ConnectionState.Connected

    fun addLog(type: LogType, message: String) {
        val entry = LogEntry(timeFormat.format(Date()), type, message)
        serialViewModel.addLog(entry)
        scope.launch { if (logs.isNotEmpty()) listState.animateScrollToItem(logs.size - 1) }
    }

    fun sendData(data: String) {
        if (data.isBlank() || !isConnected) return
        val lineEnding = config.lineEnding.bytes
        val bytes = data.toByteArray() + lineEnding
        serialViewModel.write(bytes)
        serialViewModel.addTxBytes(bytes.size)
        addLog(LogType.TX, data)
    }

    fun exportLogs() {
        if (logs.isEmpty()) return
        val logText = logs.joinToString("\n") { "[${it.timestamp}] [${it.type.name}] ${it.message}" }
        val fileName = "kserialport_log_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.txt"
        val logsDir = File(context.cacheDir, "logs")
        logsDir.mkdirs()
        val file = File(logsDir, fileName)
        file.writeText(logText)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export Logs"))
    }

    // Collect incoming data + auto-reply
    LaunchedEffect(Unit) {
        serialViewModel.incomingData.collect { bytes ->
            serialViewModel.addRxBytes(bytes.size)
            val asciiString = String(bytes)
            val display = when (displayMode) {
                DisplayMode.ASCII -> asciiString
                DisplayMode.HEX -> bytes.joinToString(" ") { "%02X".format(it) }
                DisplayMode.SPLIT -> "$asciiString | ${bytes.joinToString(" ") { "%02X".format(it) }}"
            }
            addLog(LogType.RX, display)

            // Auto-reply check (Pro only)
            if (isProUser) {
                autoReplyRules.filter { it.enabled }.forEach { rule ->
                    if (asciiString.contains(rule.trigger)) {
                        val responseBytes = rule.response.toByteArray() + config.lineEnding.bytes
                        serialViewModel.write(responseBytes)
                        serialViewModel.addTxBytes(responseBytes.size)
                        addLog(LogType.TX, "[Auto] ${rule.response}")
                    }
                }
            }
        }
    }

    // Collect events
    LaunchedEffect(Unit) {
        serialViewModel.events.collect { event ->
            when (event) {
                is SerialEvent.SystemMessage -> addLog(LogType.SYSTEM, event.message)
                is SerialEvent.Error -> {
                    addLog(LogType.ERROR, event.message)
                    serialViewModel.incrementErrors()
                }
                is SerialEvent.DataSent -> { /* already handled locally */ }
            }
        }
    }

    // Stop periodic send on disconnect
    LaunchedEffect(isConnected) {
        if (!isConnected && isPeriodicActive) {
            serialViewModel.stopPeriodicSend()
            isPeriodicActive = false
        }
    }

    // Periodic macro dialog
    if (showPeriodicDialog) {
        AlertDialog(
            onDismissRequest = { showPeriodicDialog = false },
            shape = RectangleShape,
            title = { Text("Periodic Send") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = periodicMacro,
                        onValueChange = { periodicMacro = it },
                        label = { Text("Command") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = periodicInterval,
                        onValueChange = { periodicInterval = it.filter { c -> c.isDigit() } },
                        label = { Text("Interval (ms)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val interval = periodicInterval.toLongOrNull() ?: 1000
                        if (periodicMacro.isNotBlank() && interval >= 50) {
                            val bytes = periodicMacro.toByteArray() + config.lineEnding.bytes
                            serialViewModel.startPeriodicSend(bytes, interval)
                            isPeriodicActive = true
                            addLog(LogType.SYSTEM, "Periodic send started: '$periodicMacro' every ${interval}ms")
                        }
                        showPeriodicDialog = false
                    },
                    shape = RectangleShape
                ) { Text("Start") }
            },
            dismissButton = {
                TextButton(onClick = { showPeriodicDialog = false }, shape = RectangleShape) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LocalExtendedColors.current.headerBg
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Terminal")
                        Spacer(Modifier.width(8.dp))
                        ConnectionIndicator(state = connectionState, size = 8.dp)
                    }
                },
                actions = {
                    // Toggle stats + chips
                    IconButton(onClick = {
                        scope.launch { serialViewModel.preferencesRepo.saveShowToolbar(!showToolbar) }
                    }) {
                        Icon(
                            if (showToolbar) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showToolbar) "Hide toolbar" else "Show toolbar"
                        )
                    }
                    // Toggle macros
                    IconButton(onClick = {
                        scope.launch { serialViewModel.preferencesRepo.saveShowMacros(!showMacros) }
                    }) {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = if (showMacros) "Hide macros" else "Show macros",
                            tint = if (showMacros) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Export logs (Pro)
                    if (isProUser) {
                        IconButton(onClick = { exportLogs() }, enabled = logs.isNotEmpty()) {
                            Icon(Icons.Default.FileDownload, "Export Logs")
                        }
                    }
                    // Clear
                    IconButton(onClick = {
                        serialViewModel.clearTerminal()
                    }) {
                        Icon(Icons.Default.DeleteSweep, "Clear")
                    }
                }
            )
        }
    ) { padding ->
        val density = LocalDensity.current
        val imeBottom = WindowInsets.ime.getBottom(density)
        val isKeyboardVisible = imeBottom > 0
        val focusManager = LocalFocusManager.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                }
        ) {
            // Hide top sections when keyboard is open to maximize terminal space
            if (!isKeyboardVisible) {
                // Connection Banner
                ConnectionBannerWithDialog(serialViewModel = serialViewModel)
            }

            if (!isKeyboardVisible && showToolbar) {
                // Stats Row
                Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("RX: ${formatBytes(rxBytes)}", color = TerminalRx, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text("TX: ${formatBytes(txBytes)}", color = TerminalTx, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text("ERR: $errors", color = TerminalError, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            // Quick Actions — display mode chips matching design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DisplayMode.entries.forEach { mode ->
                    val selected = displayMode == mode
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RectangleShape)
                            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .then(
                                if (!selected) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
                                else Modifier
                            )
                            .clickable { displayMode = mode }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            mode.name,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
            } // end if (!isKeyboardVisible && showToolbar)

            // Terminal View — colored badge pills per design
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(LocalExtendedColors.current.terminalBg),
                state = listState,
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(logs, key = { "${it.timestamp}_${it.hashCode()}" }) { log ->
                    val badgeColor = when (log.type) {
                        LogType.SYSTEM -> TerminalSystem
                        LogType.TX -> TerminalTx
                        LogType.RX -> TerminalRx
                        LogType.ERROR -> TerminalError
                    }
                    val msgColor = if (log.type == LogType.ERROR) TerminalError else LocalExtendedColors.current.terminalText

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            log.timestamp,
                            color = TerminalTimestamp,
                            fontSize = 14.sp
                        )
                        // Colored badge pill
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(40.dp)
                                .clip(RectangleShape)
                                .background(badgeColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                log.type.name,
                                color = badgeColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            log.message,
                            color = msgColor,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Macro Row — toggle with persist
            if (showMacros) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(macros) { macro ->
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RectangleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
                            .clickable(enabled = isConnected) { sendData(macro) }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            macro,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isConnected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Periodic send button (Pro)
                item {
                    if (isProUser) {
                        if (isPeriodicActive) {
                            AssistChip(
                                onClick = {
                                    serialViewModel.stopPeriodicSend()
                                    isPeriodicActive = false
                                    addLog(LogType.SYSTEM, "Periodic send stopped")
                                },
                                label = { Text("Stop", fontSize = 10.sp) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Stop,
                                        null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        } else {
                            AssistChip(
                                onClick = { showPeriodicDialog = true },
                                label = { Text("Repeat", fontSize = 10.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Repeat, null, modifier = Modifier.size(16.dp))
                                },
                                enabled = isConnected
                            )
                        }
                    } else {
                        AssistChip(
                            onClick = { /* show upgrade prompt */ },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Repeat", fontSize = 10.sp)
                                    ProBadge()
                                }
                            },
                            enabled = false
                        )
                    }
                }
            }
            } // end if (showMacros)

            // Input Bar — design: dark field + green send square
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RectangleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = writeData,
                        onValueChange = { writeData = it },
                        enabled = isConnected,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            sendData(writeData)
                            writeData = ""
                        }),
                        decorationBox = { innerTextField ->
                            Box {
                                if (writeData.isEmpty()) {
                                    Text(
                                        "Enter command...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RectangleShape)
                        .background(
                            if (isConnected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable(enabled = isConnected) {
                            sendData(writeData)
                            writeData = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        "Send",
                        modifier = Modifier.size(20.dp),
                        tint = if (isConnected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Ad Banner — hide when keyboard is open
            if (!isKeyboardVisible) {
            }
        }
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${bytes / (1024 * 1024)} MB"
}
