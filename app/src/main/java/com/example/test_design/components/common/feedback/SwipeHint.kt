package com.example.test_design.components.common.feedback

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun SwipeMenuHint() {
    val infiniteTransition = rememberInfiniteTransition(label = "swipeHint")

    val totalDuration = 4000
    val animDuration = 1000

    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = totalDuration
                0f at 0 with FastOutSlowInEasing
                16f at animDuration / 2 with FastOutSlowInEasing
                0f at animDuration with FastOutSlowInEasing
                0f at totalDuration
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "xOffset"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = totalDuration
                0f at 0
                0.4f at animDuration / 2
                0f at animDuration
                0f at totalDuration
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(60.dp)
            .zIndex(10f),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .graphicsLayer {
                    translationX = xOffset.dp.toPx()
                    this.alpha = alpha
                }
                .padding(start = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(0.20f)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF6200EE),
                                Color.Transparent
                            )
                        )
                    )
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = null,
                tint = Color(0xFF6200EE),
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 4.dp)
                    .offset(x = (-4).dp)
            )
        }
    }
}