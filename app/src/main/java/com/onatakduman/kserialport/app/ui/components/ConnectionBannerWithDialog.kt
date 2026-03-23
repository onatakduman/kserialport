package com.onatakduman.kserialport.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.onatakduman.kserialport.app.viewmodel.SerialViewModel

@Composable
fun ConnectionBannerWithDialog(
    serialViewModel: SerialViewModel,
    modifier: Modifier = Modifier
) {
    val connectionState by serialViewModel.connectionState.collectAsState()
    val config by serialViewModel.serialConfig.collectAsState()
    val devices by serialViewModel.devices.collectAsState()
    val isScanning by serialViewModel.isScanning.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    ConnectionBanner(
        connectionState = connectionState,
        onClick = { showDialog = true },
        modifier = modifier
    )

    AdBanner(serialViewModel = serialViewModel)

    if (showDialog) {
        ConnectionDialog(
            connectionState = connectionState,
            config = config,
            devices = devices,
            isScanning = isScanning,
            onDismiss = { showDialog = false },
            onScan = { serialViewModel.scanDevices() },
            onSelectDevice = { serialViewModel.updatePath(it) },
            onSelectBaudRate = { serialViewModel.updateConfig(config.copy(baudRate = it)) },
            onConnect = { serialViewModel.connect() },
            onDisconnect = { serialViewModel.disconnect() }
        )
    }
}
