package com.onatakduman.kserialport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onatakduman.kserialport.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidkserialportTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars),
                    color = DarkBackground
                ) {
                    SerialTerminal()
                }
            }
        }
    }
}

data class LogEntry(
    val timestamp: String,
    val type: LogType,
    val message: String
)

enum class LogType { SYSTEM, TX, RX, ERROR }

enum class DisplayMode { ASCII, HEX, SPLIT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SerialTerminal() {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var path by remember { mutableStateOf("/dev/ttyUSB0") }
    var baudRate by remember { mutableStateOf(115200) }
    var dataBits by remember { mutableStateOf(8) }
    var stopBits by remember { mutableStateOf(1) }
    var parity by remember { mutableStateOf(0) }
    var connection by remember { mutableStateOf<SerialPortConnection?>(null) }
    var logs by remember { mutableStateOf(listOf<LogEntry>()) }
    var writeData by remember { mutableStateOf("") }
    var availableDevices by remember { mutableStateOf(listOf<String>()) }
    var displayMode by remember { mutableStateOf(DisplayMode.ASCII) }
    var rxBytes by remember { mutableLongStateOf(0L) }
    var txBytes by remember { mutableLongStateOf(0L) }
    var errors by remember { mutableIntStateOf(0) }
    var isScanning by remember { mutableStateOf(false) }
    var readJob by remember { mutableStateOf<Job?>(null) }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

    val isConnected = connection != null

    fun addLog(type: LogType, message: String) {
        val entry = LogEntry(timeFormat.format(Date()), type, message)
        logs = logs + entry
        scope.launch { listState.animateScrollToItem(logs.size - 1) }
    }

    fun scanDevices() {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { isScanning = true }
            try {
                val finder = SerialPortFinder()
                val devices = finder.getAllDevices()
                val paths = finder.getAllDevicesPath()
                withContext(Dispatchers.Main) {
                    availableDevices = paths.toList()
                    addLog(LogType.SYSTEM, "Found ${devices.size} device(s)")
                    devices.forEach { addLog(LogType.SYSTEM, "  $it") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addLog(LogType.ERROR, "Scan failed: ${e.message}")
                    errors++
                }
            } finally {
                withContext(Dispatchers.Main) { isScanning = false }
            }
        }
    }

    fun connect() {
        scope.launch(Dispatchers.IO) {
            try {
                val port = SerialPort(path, baudRate, dataBits, stopBits, parity)
                val conn = port.open()
                withContext(Dispatchers.Main) {
                    connection = conn
                    addLog(LogType.SYSTEM, "Connected to $path @ $baudRate baud")
                }
                readJob = launch {
                    try {
                        conn.readFlow.collect { bytes ->
                            withContext(Dispatchers.Main) {
                                rxBytes += bytes.size
                                val display = when (displayMode) {
                                    DisplayMode.ASCII -> String(bytes)
                                    DisplayMode.HEX -> bytes.joinToString(" ") { "%02X".format(it) }
                                    DisplayMode.SPLIT -> "${String(bytes)} | ${bytes.joinToString(" ") { "%02X".format(it) }}"
                                }
                                addLog(LogType.RX, display)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            addLog(LogType.SYSTEM, "Connection closed")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addLog(LogType.ERROR, "Connection failed: ${e.message}")
                    errors++
                }
            }
        }
    }

    fun disconnect() {
        readJob?.cancel()
        readJob = null
        try {
            connection?.close()
            connection = null
            addLog(LogType.SYSTEM, "Disconnected")
        } catch (e: Exception) {
            addLog(LogType.ERROR, "Disconnect error: ${e.message}")
            errors++
        }
    }

    fun sendData(data: String) {
        if (data.isBlank()) return
        scope.launch {
            try {
                val bytes = (data + "\r\n").toByteArray()  // Add CRLF
                connection?.write(bytes)
                txBytes += bytes.size
                addLog(LogType.TX, data)
            } catch (e: Exception) {
                addLog(LogType.ERROR, "Send failed: ${e.message}")
                errors++
            }
        }
    }

    if (isTablet) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Panel - Configuration
            ConfigurationPanel(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
                    .background(DarkSurface)
                    .padding(16.dp),
                path = path,
                onPathChange = { path = it },
                baudRate = baudRate,
                onBaudRateChange = { baudRate = it },
                dataBits = dataBits,
                onDataBitsChange = { dataBits = it },
                stopBits = stopBits,
                onStopBitsChange = { stopBits = it },
                parity = parity,
                onParityChange = { parity = it },
                isConnected = isConnected,
                isScanning = isScanning,
                availableDevices = availableDevices,
                onScan = { scanDevices() },
                onConnect = { connect() },
                onDisconnect = { disconnect() }
            )

            // Right Panel - Terminal + Stats
            Column(modifier = Modifier.fillMaxSize()) {
                // Stats Bar
                StatsBar(
                    modifier = Modifier.fillMaxWidth(),
                    isConnected = isConnected,
                    rxBytes = rxBytes,
                    txBytes = txBytes,
                    errors = errors,
                    displayMode = displayMode,
                    onDisplayModeChange = { displayMode = it },
                    onClear = { logs = emptyList(); rxBytes = 0; txBytes = 0; errors = 0 }
                )

                // Terminal
                TerminalView(
                    modifier = Modifier.weight(1f),
                    logs = logs,
                    listState = listState
                )

                // Input Bar
                InputBar(
                    modifier = Modifier.fillMaxWidth(),
                    value = writeData,
                    onValueChange = { writeData = it },
                    onSend = { sendData(writeData); writeData = "" },
                    isConnected = isConnected,
                    onMacro = { sendData(it) }
                )
            }
        }
    } else {
        // Phone Layout - Vertical
        Column(modifier = Modifier.fillMaxSize()) {
            // Connection Bar
            PhoneConnectionBar(
                modifier = Modifier.fillMaxWidth(),
                path = path,
                onPathChange = { path = it },
                baudRate = baudRate,
                isConnected = isConnected,
                isScanning = isScanning,
                availableDevices = availableDevices,
                onScan = { scanDevices() },
                onConnect = { connect() },
                onDisconnect = { disconnect() },
                onDeviceSelect = { path = it }
            )

            // Stats Row
            CompactStats(
                modifier = Modifier.fillMaxWidth(),
                rxBytes = rxBytes,
                txBytes = txBytes,
                displayMode = displayMode,
                onDisplayModeChange = { displayMode = it },
                onClear = { logs = emptyList(); rxBytes = 0; txBytes = 0; errors = 0 }
            )

            // Terminal
            TerminalView(
                modifier = Modifier.weight(1f),
                logs = logs,
                listState = listState
            )

            // Input Bar
            InputBar(
                modifier = Modifier.fillMaxWidth(),
                value = writeData,
                onValueChange = { writeData = it },
                onSend = { sendData(writeData); writeData = "" },
                isConnected = isConnected,
                onMacro = { sendData(it) }
            )
        }
    }
}

@Composable
fun ConfigurationPanel(
    modifier: Modifier = Modifier,
    path: String,
    onPathChange: (String) -> Unit,
    baudRate: Int,
    onBaudRateChange: (Int) -> Unit,
    dataBits: Int,
    onDataBitsChange: (Int) -> Unit,
    stopBits: Int,
    onStopBitsChange: (Int) -> Unit,
    parity: Int,
    onParityChange: (Int) -> Unit,
    isConnected: Boolean,
    isScanning: Boolean,
    availableDevices: List<String>,
    onScan: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(modifier = modifier) {
        // Scrollable Configuration Area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "CONFIGURATION",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Connection Status Indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) StatusConnected else StatusDisconnected)
                )
            }

            HorizontalDivider(color = DarkSurfaceVariant)

            // Active Port
            Text("Active Port", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            OutlinedTextField(
                value = path,
                onValueChange = onPathChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isConnected,
                singleLine = true,
                colors = terminalTextFieldColors(),
                leadingIcon = { Icon(Icons.Default.Usb, null, tint = TextSecondary) }
            )

            // Scan Button
            Button(
                onClick = onScan,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isConnected && !isScanning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurfaceVariant,
                    contentColor = TextPrimary
                )
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = AccentGreen
                    )
                } else {
                    Icon(Icons.Default.Search, null)
                }
                Spacer(Modifier.width(8.dp))
                Text(if (isScanning) "Scanning..." else "Scan Devices")
            }

            // Available Devices
            if (availableDevices.isNotEmpty() && !isConnected) {
                Text("Available Devices", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    availableDevices.forEach { device ->
                        Text(
                            device,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPathChange(device) }
                                .padding(8.dp),
                            color = if (device == path) AccentGreen else TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            HorizontalDivider(color = DarkSurfaceVariant)

            // Baud Rate
            Text("Baud Rate", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            DropdownSelector(
                options = listOf(9600, 19200, 38400, 57600, 115200, 230400, 460800, 921600),
                selected = baudRate,
                onSelect = onBaudRateChange,
                enabled = !isConnected
            )

            // Data Bits & Stop Bits
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Data Bits", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    DropdownSelector(
                        options = listOf(5, 6, 7, 8),
                        selected = dataBits,
                        onSelect = onDataBitsChange,
                        enabled = !isConnected
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Stop Bits", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    DropdownSelector(
                        options = listOf(1, 2),
                        selected = stopBits,
                        onSelect = onStopBitsChange,
                        enabled = !isConnected
                    )
                }
            }

            // Parity
            Text("Parity", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            DropdownSelector(
                options = listOf("None" to 0, "Odd" to 1, "Even" to 2),
                selected = parity,
                onSelect = onParityChange,
                enabled = !isConnected
            )
        }

        // Fixed Connect Button at bottom
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { if (isConnected) onDisconnect() else onConnect() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isConnected) AccentRed else AccentGreen
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                if (isConnected) Icons.Default.Close else Icons.Default.PowerSettingsNew,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (isConnected) "Disconnect" else "Connect",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun StatsBar(
    modifier: Modifier = Modifier,
    isConnected: Boolean,
    rxBytes: Long,
    txBytes: Long,
    errors: Int,
    displayMode: DisplayMode,
    onDisplayModeChange: (DisplayMode) -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = modifier
            .background(DarkSurface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display Mode Toggles
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DisplayMode.entries.forEach { mode ->
                FilterChip(
                    selected = displayMode == mode,
                    onClick = { onDisplayModeChange(mode) },
                    label = { Text(mode.name, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Stats
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatItem("RX", formatBytes(rxBytes), AccentGreen)
            StatItem("TX", formatBytes(txBytes), AccentBlue)
            StatItem("ERR", errors.toString(), AccentRed)

            // Clear Button
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Delete, "Clear", tint = TextSecondary)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TerminalView(
    modifier: Modifier = Modifier,
    logs: List<LogEntry>,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(8.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(logs) { log ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    log.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                Text(
                    "[${log.type.name}]",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (log.type) {
                        LogType.SYSTEM -> AccentYellow
                        LogType.TX -> AccentBlue
                        LogType.RX -> AccentGreen
                        LogType.ERROR -> AccentRed
                    },
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    log.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun InputBar(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isConnected: Boolean,
    onMacro: (String) -> Unit
) {
    Column(
        modifier = modifier
            .background(DarkSurface)
            .padding(8.dp)
    ) {
        // Quick Macros
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Quick Macros:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            listOf("AT", "AT+RST", "PING", "TEST").forEach { macro ->
                AssistChip(
                    onClick = { if (isConnected) onMacro(macro) },
                    label = { Text(macro, fontSize = 10.sp) },
                    enabled = isConnected,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = DarkSurfaceVariant
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter data to send...") },
                enabled = isConnected,
                singleLine = true,
                colors = terminalTextFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                leadingIcon = {
                    Text("ASCII", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            )

            FloatingActionButton(
                onClick = onSend,
                containerColor = if (isConnected) AccentGreen else DarkSurfaceVariant,
                contentColor = if (isConnected) DarkBackground else TextMuted
            ) {
                Icon(Icons.Default.Send, "Send")
            }
        }
    }
}

@Composable
fun PhoneConnectionBar(
    modifier: Modifier = Modifier,
    path: String,
    onPathChange: (String) -> Unit,
    baudRate: Int,
    isConnected: Boolean,
    isScanning: Boolean,
    availableDevices: List<String>,
    onScan: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onDeviceSelect: (String) -> Unit
) {
    Column(
        modifier = modifier
            .background(DarkSurface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status + Connect Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) StatusConnected else StatusDisconnected)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isConnected) "Connected @ $baudRate" else "Disconnected",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isConnected) StatusConnected else TextSecondary
                )
            }

            Button(
                onClick = { if (isConnected) onDisconnect() else onConnect() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) AccentRed else AccentGreen
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    if (isConnected) Icons.Default.Close else Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(if (isConnected) "Disconnect" else "Connect")
            }
        }

        // Path input + Scan
        if (!isConnected) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = path,
                    onValueChange = onPathChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Device Path") },
                    colors = terminalTextFieldColors(),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                IconButton(
                    onClick = onScan,
                    enabled = !isScanning
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = AccentGreen)
                    } else {
                        Icon(Icons.Default.Search, "Scan", tint = AccentGreen)
                    }
                }
            }

            // Available devices
            if (availableDevices.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(availableDevices.size) { index ->
                        val device = availableDevices[index]
                        AssistChip(
                            onClick = { onDeviceSelect(device) },
                            label = { Text(device.removePrefix("/dev/"), fontSize = 11.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (device == path) AccentGreen.copy(alpha = 0.3f) else DarkSurfaceVariant,
                                labelColor = if (device == path) AccentGreen else TextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactStats(
    modifier: Modifier = Modifier,
    rxBytes: Long,
    txBytes: Long,
    displayMode: DisplayMode,
    onDisplayModeChange: (DisplayMode) -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = modifier
            .background(DarkSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("RX: ${formatBytes(rxBytes)}", color = AccentGreen, fontSize = 12.sp)
            Text("TX: ${formatBytes(txBytes)}", color = AccentBlue, fontSize = 12.sp)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DisplayMode.entries.forEach { mode ->
                Text(
                    mode.name,
                    modifier = Modifier
                        .clickable { onDisplayModeChange(mode) }
                        .background(
                            if (displayMode == mode) AccentBlue else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    color = if (displayMode == mode) Color.White else TextMuted,
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Clear", tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownSelector(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selected.toString(),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = terminalTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString()) },
                    onClick = {
                        @Suppress("UNCHECKED_CAST")
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    options: List<Pair<String, Int>>,
    selected: Int,
    onSelect: (Int) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.second == selected }?.first ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = terminalTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (label, value) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun terminalTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentGreen,
    unfocusedBorderColor = DarkSurfaceVariant,
    disabledBorderColor = DarkSurfaceVariant,
    focusedContainerColor = DarkBackground,
    unfocusedContainerColor = DarkBackground,
    disabledContainerColor = DarkSurfaceVariant.copy(alpha = 0.5f),
    cursorColor = AccentGreen,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    disabledTextColor = TextMuted
)

fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${bytes / (1024 * 1024)} MB"
}
