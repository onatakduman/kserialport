package com.onatakduman.kserialport.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.onatakduman.kserialport.app.data.model.ConnectionState
import com.onatakduman.kserialport.app.data.model.DeviceInfo
import com.onatakduman.kserialport.app.data.model.SerialConfig
import com.onatakduman.kserialport.app.ui.theme.StatusConnected
import com.onatakduman.kserialport.app.ui.theme.StatusConnecting
import com.onatakduman.kserialport.app.ui.theme.StatusDisconnected

@Composable
fun ConnectionDialog(
    connectionState: ConnectionState,
    config: SerialConfig,
    devices: List<DeviceInfo>,
    isScanning: Boolean,
    onDismiss: () -> Unit,
    onScan: () -> Unit,
    onSelectDevice: (String) -> Unit,
    onSelectBaudRate: (Int) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val isConnected = connectionState is ConnectionState.Connected
    val statusColor = when (connectionState) {
        is ConnectionState.Connected -> StatusConnected
        is ConnectionState.Connecting -> StatusConnecting
        else -> StatusDisconnected
    }
    val statusText = when (connectionState) {
        is ConnectionState.Connected -> "connected"
        is ConnectionState.Connecting -> "connecting"
        is ConnectionState.Disconnected -> "disconnected"
        is ConnectionState.Error -> "error"
    }
    val statusDesc = when (connectionState) {
        is ConnectionState.Connected -> connectionState.path
        else -> "no device"
    }
    val baudRates = listOf(9600, 19200, 38400, 57600, 115200)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Bottom sheet — scrollable
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .verticalScroll(rememberScrollState())
                    .clickable(enabled = false, onClick = {}) // prevent dismiss on sheet click
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RectangleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "// connection",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                SectionDivider()

                // Status section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("// status", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                        Text(statusText, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = statusColor)
                        Text("·", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(statusDesc, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                SectionDivider()

                // Device selection
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("// select_device", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (isScanning) "scanning..." else "scan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable(enabled = !isScanning && !isConnected) { onScan() }
                        )
                    }

                    if (devices.isEmpty() && !isScanning) {
                        Text(
                            "Tap 'scan' to find devices",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    if (isScanning) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    devices.forEach { device ->
                        val isSelected = device.path == config.path
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RectangleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    RectangleShape
                                )
                                .clickable { onSelectDevice(device.path) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Usb, null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(device.path, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                if (device.driver.isNotBlank()) {
                                    Text(device.driver, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (device.driver.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RectangleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        device.driver.take(4).uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                SectionDivider()

                // Baud rate
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("// baud_rate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        baudRates.forEach { baud ->
                            val isSelected = config.baudRate == baud
                            Box(
                                modifier = Modifier
                                    .clip(RectangleShape)
                                    .then(
                                        if (isSelected) Modifier.background(MaterialTheme.colorScheme.primary)
                                        else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
                                    )
                                    .clickable(enabled = !isConnected) { onSelectBaudRate(baud) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$baud",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                SectionDivider()

                // Parameters
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("// parameters", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("data_bits: ${config.dataBits}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("stop_bits: ${config.stopBits}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("parity: ${if (config.parity == 0) "none" else if (config.parity == 1) "odd" else "even"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                SectionDivider()

                // Connect/Disconnect button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RectangleShape)
                            .background(
                                if (isConnected) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                            .clickable {
                                if (isConnected) onDisconnect() else onConnect()
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isConnected) "$ disconnect" else "$ connect",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        thickness = 1.dp
    )
}
