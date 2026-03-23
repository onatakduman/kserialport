package com.onatakduman.kserialport.app.ui.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onatakduman.kserialport.app.ui.components.ProBadge
import com.onatakduman.kserialport.app.ui.theme.KSerialPortTheme

@Composable
fun KspSectionHeader(
    title: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace,
        color = color,
        modifier = modifier.padding(top = 8.dp)
    )
}

@Composable
fun KspSectionHeaderWithAction(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    showProBadge: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )
            if (showProBadge) {
                ProBadge(modifier = Modifier.padding(start = 8.dp))
            }
        }
        TextButton(
            onClick = onAction,
            enabled = enabled
        ) {
            Text(
                text = actionLabel,
                fontFamily = FontFamily.Monospace,
                color = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DarkPreview() {
    KSerialPortTheme(darkTheme = true, dynamicColor = false) {
        KspSectionHeader(title = "// quick_connect")
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun LightPreview() {
    KSerialPortTheme(darkTheme = false, dynamicColor = false) {
        KspSectionHeader(title = "// quick_connect")
    }
}

@Preview(name = "Dark - WithAction", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun WithActionDarkPreview() {
    KSerialPortTheme(darkTheme = true, dynamicColor = false) {
        KspSectionHeaderWithAction(
            title = "// macros",
            actionLabel = "Edit",
            onAction = {},
            showProBadge = true
        )
    }
}

@Preview(name = "Light - WithAction", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun WithActionLightPreview() {
    KSerialPortTheme(darkTheme = false, dynamicColor = false) {
        KspSectionHeaderWithAction(
            title = "// macros",
            actionLabel = "Edit",
            onAction = {},
            showProBadge = true
        )
    }
}
