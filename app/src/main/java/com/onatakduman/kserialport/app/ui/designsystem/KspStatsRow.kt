package com.onatakduman.kserialport.app.ui.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onatakduman.kserialport.app.ui.theme.KSerialPortTheme
import com.onatakduman.kserialport.app.ui.theme.TerminalError
import com.onatakduman.kserialport.app.ui.theme.TerminalRx
import com.onatakduman.kserialport.app.ui.theme.TerminalTx

@Composable
fun KspStatsRow(
    rxBytes: Long,
    txBytes: Long,
    errors: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "RX: ${formatBytes(rxBytes)}",
            color = TerminalRx,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "TX: ${formatBytes(txBytes)}",
            color = TerminalTx,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "ERR: $errors",
            color = TerminalError,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${bytes / (1024 * 1024)} MB"
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DarkPreview() {
    KSerialPortTheme(darkTheme = true, dynamicColor = false) {
        KspStatsRow(
            rxBytes = 2048,
            txBytes = 512,
            errors = 3
        )
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun LightPreview() {
    KSerialPortTheme(darkTheme = false, dynamicColor = false) {
        KspStatsRow(
            rxBytes = 2048,
            txBytes = 512,
            errors = 3
        )
    }
}
