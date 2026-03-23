package com.onatakduman.kserialport.app.ui.designsystem

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onatakduman.kserialport.app.ui.theme.KSerialPortTheme

@Composable
fun KspSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val trackWidth = 44.dp
    val trackHeight = 24.dp
    val thumbSize = 18.dp
    val thumbPadding = 3.dp

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - thumbPadding * 2 else 0.dp,
        animationSpec = tween(150),
        label = "thumbOffset"
    )

    val trackColor = if (checked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val thumbColor = if (checked) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(RectangleShape)
            .background(trackColor)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(thumbPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .background(thumbColor)
        )
    }
}

@Preview(name = "Dark - On", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DarkOnPreview() {
    KSerialPortTheme(darkTheme = true, dynamicColor = false) {
        Row { KspSwitch(checked = true, onCheckedChange = {}) }
    }
}

@Preview(name = "Dark - Off", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DarkOffPreview() {
    KSerialPortTheme(darkTheme = true, dynamicColor = false) {
        Row { KspSwitch(checked = false, onCheckedChange = {}) }
    }
}

@Preview(name = "Light - On", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun LightOnPreview() {
    KSerialPortTheme(darkTheme = false, dynamicColor = false) {
        Row { KspSwitch(checked = true, onCheckedChange = {}) }
    }
}

@Preview(name = "Light - Off", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun LightOffPreview() {
    KSerialPortTheme(darkTheme = false, dynamicColor = false) {
        Row { KspSwitch(checked = false, onCheckedChange = {}) }
    }
}
