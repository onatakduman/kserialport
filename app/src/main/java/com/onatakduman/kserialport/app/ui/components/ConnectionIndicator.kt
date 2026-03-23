package com.onatakduman.kserialport.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.onatakduman.kserialport.app.data.model.ConnectionState
import com.onatakduman.kserialport.app.ui.theme.StatusConnected
import com.onatakduman.kserialport.app.ui.theme.StatusConnecting
import com.onatakduman.kserialport.app.ui.theme.StatusDisconnected
import com.onatakduman.kserialport.app.ui.theme.StatusIdle

@Composable
fun ConnectionIndicator(
    state: ConnectionState,
    size: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    val color = when (state) {
        is ConnectionState.Connected -> StatusConnected
        is ConnectionState.Connecting -> StatusConnecting
        is ConnectionState.Disconnected -> StatusDisconnected
        is ConnectionState.Error -> StatusDisconnected
    }

    val shouldPulse = state is ConnectionState.Connected || state is ConnectionState.Connecting

    if (shouldPulse) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )

        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            // Pulse ring
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha * 0.3f))
            )
            // Solid dot
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}
