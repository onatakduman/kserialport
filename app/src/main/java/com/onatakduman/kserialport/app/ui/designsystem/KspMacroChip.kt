package com.onatakduman.kserialport.app.ui.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onatakduman.kserialport.app.ui.theme.KSerialPortTheme

@Composable
fun KspMacroChip(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val textColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .height(32.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RectangleShape
            )
            .clickable(enabled = enabled, onClick = onClick),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = textColor
            )
        }
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DarkPreview() {
    KSerialPortTheme(darkTheme = true, dynamicColor = false) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KspMacroChip(label = "AT+RST", onClick = {})
            KspMacroChip(label = "AT+GMR", onClick = {}, enabled = false)
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun LightPreview() {
    KSerialPortTheme(darkTheme = false, dynamicColor = false) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KspMacroChip(label = "AT+RST", onClick = {})
            KspMacroChip(label = "AT+GMR", onClick = {}, enabled = false)
        }
    }
}
