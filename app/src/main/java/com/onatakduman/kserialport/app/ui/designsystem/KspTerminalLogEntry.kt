package com.onatakduman.kserialport.app.ui.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onatakduman.kserialport.app.data.model.LogType
import com.onatakduman.kserialport.app.ui.theme.KSerialPortTheme
import com.onatakduman.kserialport.app.ui.theme.LocalExtendedColors
import com.onatakduman.kserialport.app.ui.theme.TerminalError
import com.onatakduman.kserialport.app.ui.theme.TerminalRx
import com.onatakduman.kserialport.app.ui.theme.TerminalSystem
import com.onatakduman.kserialport.app.ui.theme.TerminalTimestamp
import com.onatakduman.kserialport.app.ui.theme.TerminalTx

@Composable
fun KspTerminalLogEntry(
    timestamp: String,
    type: LogType,
    message: String,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (type) {
        LogType.SYSTEM -> TerminalSystem
        LogType.TX -> TerminalTx
        LogType.RX -> TerminalRx
        LogType.ERROR -> TerminalError
    }

    val badgeLabel = when (type) {
        LogType.SYSTEM -> "SYS"
        LogType.TX -> "TX"
        LogType.RX -> "RX"
        LogType.ERROR -> "ERR"
    }

    val messageColor = if (type == LogType.ERROR) TerminalError else LocalExtendedColors.current.terminalText

    Row(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = timestamp,
            color = TerminalTimestamp,
            fontSize = 14.sp
        )

        Box(
            modifier = Modifier
                .width(40.dp)
                .height(20.dp)
                .background(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RectangleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badgeLabel,
                color = badgeColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = message,
            color = messageColor,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DarkPreview() {
    KSerialPortTheme(darkTheme = true, dynamicColor = false) {
        Column(
            modifier = Modifier
                .background(LocalExtendedColors.current.terminalBg)
                .padding(8.dp)
        ) {
            KspTerminalLogEntry(
                timestamp = "12:34:56",
                type = LogType.SYSTEM,
                message = "Port opened on /dev/ttyUSB0"
            )
            KspTerminalLogEntry(
                timestamp = "12:34:57",
                type = LogType.TX,
                message = "AT+GMR"
            )
            KspTerminalLogEntry(
                timestamp = "12:34:58",
                type = LogType.RX,
                message = "OK v1.2.3"
            )
            KspTerminalLogEntry(
                timestamp = "12:34:59",
                type = LogType.ERROR,
                message = "Connection lost: timeout"
            )
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun LightPreview() {
    KSerialPortTheme(darkTheme = false, dynamicColor = false) {
        Column(
            modifier = Modifier
                .background(LocalExtendedColors.current.terminalBg)
                .padding(8.dp)
        ) {
            KspTerminalLogEntry(
                timestamp = "12:34:56",
                type = LogType.SYSTEM,
                message = "Port opened on /dev/ttyUSB0"
            )
            KspTerminalLogEntry(
                timestamp = "12:34:57",
                type = LogType.TX,
                message = "AT+GMR"
            )
            KspTerminalLogEntry(
                timestamp = "12:34:58",
                type = LogType.RX,
                message = "OK v1.2.3"
            )
            KspTerminalLogEntry(
                timestamp = "12:34:59",
                type = LogType.ERROR,
                message = "Connection lost: timeout"
            )
        }
    }
}
