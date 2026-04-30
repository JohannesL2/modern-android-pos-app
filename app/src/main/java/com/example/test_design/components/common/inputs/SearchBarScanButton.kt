package com.example.test_design.components.common.inputs

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource

@Suppress("SpellCheckingInspection", "SpellCheckingInspection")
@Composable
fun SearchBarScanButton(
    onClick: () -> Unit,
    tint: Color = Color(0xFF42278D)
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "laserScan")

    val cycleDuration = 6400
    val sweepDuration = 1200

    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = cycleDuration
                0.1f at 0 with LinearEasing

                0.9f at sweepDuration with LinearEasing
                0.1f at sweepDuration * 2 with LinearEasing

                0.9f at sweepDuration * 3 with LinearEasing
                0.1f at sweepDuration * 4 with LinearEasing

                0.9f at sweepDuration * 5 with LinearEasing
                0.1f at sweepDuration * 6 with LinearEasing

                0.1f at cycleDuration
            }
        ),
        label = "yOffset"
    )

    val laserAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = cycleDuration
                1f at 0
                1f at sweepDuration * 6
                0f at sweepDuration * 6 + 100
                0f at cycleDuration - 100
                1f at cycleDuration
            }
        ),
        label = "laserAlpha"
    )

    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .size(44.dp)
    ) {
        Box(
            modifier = Modifier.size(30.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cornerLength = size.width * 0.25f
                val strokeWidth = 2.2.dp.toPx()
                val cap = StrokeCap.Round

                drawLine(
                    tint,
                    Offset(0f, 0f),
                    Offset(cornerLength, 0f),
                    strokeWidth, cap
                )

                drawLine(
                    tint,
                    Offset(0f, 0f),
                    Offset(0f, cornerLength),
                    strokeWidth, cap
                )

                drawLine(
                    tint,
                    Offset(size.width, 0f),
                    Offset(size.width - cornerLength, 0f),
                    strokeWidth, cap
                )
                drawLine(
                    tint,
                    Offset(size.width, 0f),
                    Offset(size.width, cornerLength),
                    strokeWidth, cap
                )

                drawLine(
                    tint,
                    Offset(0f, size.height),
                    Offset(cornerLength, size.height),
                    strokeWidth, cap
                )
                drawLine(
                    tint,
                    Offset(0f, size.height),
                    Offset(0f, size.height - cornerLength),
                    strokeWidth, cap
                )

                drawLine(
                    tint,
                    Offset(size.width, size.height),
                    Offset(size.width - cornerLength, size.height),
                    strokeWidth, cap
                )
                drawLine(
                    tint,
                    Offset(size.width, size.height),
                    Offset(size.width, size.height - cornerLength),
                    strokeWidth, cap
                )
            }


            Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                Icon(
                    painter = painterResource(com.example.test_design.R.drawable.ic_qr_scanner),
                    contentDescription = "Skanna streckkod",
                    modifier = Modifier.fillMaxSize(),
                    tint = tint.copy(alpha = 0.47f)
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (laserAlpha > 0f) {
                        val scanLineY = size.height * yOffset

                        drawLine(
                            brush = Brush.horizontalGradient(
                                0f to Color.Transparent,
                                0.4f to Color.Red.copy(alpha = laserAlpha),
                                0.5f to Color.White.copy(alpha = laserAlpha),
                                0.6f to Color.Red.copy(alpha = laserAlpha),
                                1f to Color.Transparent
                            ),
                            start = Offset(-2.dp.toPx(), scanLineY),
                            end = Offset(size.width + 2.dp.toPx(), scanLineY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }
        }
    }
}