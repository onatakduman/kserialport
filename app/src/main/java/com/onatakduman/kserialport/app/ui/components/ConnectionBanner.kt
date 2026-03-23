package com.onatakduman.kserialport.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onatakduman.kserialport.app.data.model.ConnectionState
import com.onatakduman.kserialport.app.ui.theme.StatusConnected
import com.onatakduman.kserialport.app.ui.theme.StatusConnecting
import com.onatakduman.kserialport.app.ui.theme.StatusDisconnected

@Composable
fun ConnectionBanner(
    connectionState: ConnectionState,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
    val devicePath = when (connectionState) {
        is ConnectionState.Connected -> connectionState.path
        else -> "no device"
    }
    val baudRate = when (connectionState) {
        is ConnectionState.Connected -> "@ ${connectionState.baudRate}"
        else -> ""
    }
    val borderColor = MaterialTheme.colorScheme.outline

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Text(statusText, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = statusColor)
        Text("·", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(devicePath, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.weight(1f))
        if (baudRate.isNotEmpty()) {
            Text(baudRate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "Connection settings",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
