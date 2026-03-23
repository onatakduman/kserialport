package com.onatakduman.kserialport.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onatakduman.kserialport.app.data.model.ConnectionState
import com.onatakduman.kserialport.app.data.model.DeviceInfo
import com.onatakduman.kserialport.app.ui.components.AdBanner
import com.onatakduman.kserialport.app.ui.components.ConnectionBannerWithDialog
import com.onatakduman.kserialport.app.ui.components.ConnectionIndicator
import com.onatakduman.kserialport.app.ui.theme.LocalExtendedColors
import com.onatakduman.kserialport.app.viewmodel.SerialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    serialViewModel: SerialViewModel,
    onNavigateToTerminal: () -> Unit
) {
    val connectionState by serialViewModel.connectionState.collectAsState()
    val config by serialViewModel.serialConfig.collectAsState()
    val recentConnections by serialViewModel.recentConnections.collectAsState()
    val devices by serialViewModel.devices.collectAsState()
    val isScanning by serialViewModel.isScanning.collectAsState()
    val isConnected = connectionState is ConnectionState.Connected

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KSerialPort") },
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
            // Connection Banner — taps open Connection Dialog
            ConnectionBannerWithDialog(serialViewModel = serialViewModel)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // // connection_status
                SectionHeader("// connection_status")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RectangleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ConnectionIndicator(state = connectionState, size = 10.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            when (connectionState) {
                                is ConnectionState.Connected -> "Connected"
                                is ConnectionState.Connecting -> "Connecting..."
                                is ConnectionState.Disconnected -> "Disconnected"
                                is ConnectionState.Error -> "Error"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (connectionState) {
                                is ConnectionState.Connected -> MaterialTheme.colorScheme.primary
                                is ConnectionState.Error -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    if (isConnected) {
                        val connected = connectionState as ConnectionState.Connected
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(connected.path, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Baud Rate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${connected.baudRate}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    if (isConnected) {
                        Button(
                            onClick = { serialViewModel.disconnect() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RectangleShape,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Disconnect")
                        }
                    }
                }

                // // quick_connect
                SectionHeader("// quick_connect")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RectangleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Usb, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(config.path, style = MaterialTheme.typography.bodyMedium)
                            Text("${config.baudRate} baud", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!isConnected) {
                            Button(
                                onClick = {
                                    serialViewModel.connect()
                                    onNavigateToTerminal()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RectangleShape
                            ) {
                                Text("Connect")
                            }
                        } else {
                            FilledTonalButton(onClick = onNavigateToTerminal, shape = RectangleShape) {
                                Text("Open Terminal")
                            }
                        }
                    }

                    // Device scanner inline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { serialViewModel.scanDevices() },
                            enabled = !isScanning && !isConnected,
                            shape = RectangleShape,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Scanning...")
                            } else {
                                Icon(Icons.Default.Sensors, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Scan Devices")
                            }
                        }
                    }

                    if (devices.isNotEmpty()) {
                        devices.forEach { device ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RectangleShape)
                                    .background(
                                        if (device.path == config.path) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (device.path == config.path) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline,
                                        RectangleShape
                                    )
                                    .clickable { serialViewModel.updatePath(device.path) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Usb, null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (device.path == config.path) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    device.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    color = if (device.path == config.path) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (device.driver.isNotBlank()) {
                                    Text("[${device.driver}]", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // // recent_connections
                if (recentConnections.isNotEmpty()) {
                    SectionHeader("// recent_connections")
                    recentConnections.forEach { connectionString ->
                        val parts = connectionString.split("@")
                        val path = parts.getOrNull(0) ?: ""
                        val baud = parts.getOrNull(1) ?: ""

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RectangleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
                                .clickable {
                                    serialViewModel.updateConfig(
                                        config.copy(
                                            path = path,
                                            baudRate = baud.toIntOrNull() ?: 115200
                                        )
                                    )
                                    onNavigateToTerminal()
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Usb, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(path, style = MaterialTheme.typography.bodyMedium)
                                Text("$baud baud", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 8.dp)
    )
}
