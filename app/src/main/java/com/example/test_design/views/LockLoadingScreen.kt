package com.example.test_design.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import com.example.test_design.R

@Composable
fun LockLoadingScreen(
    visible: Boolean,
    contentDescription: String = stringResource(R.string.desc_security_processing)
) {
    if (!visible) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.9f))
            .pointerInput(Unit) {
                detectTapGestures { } //blockerar clicks utan ripple effect
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(96.dp),
            strokeWidth = 4.dp,
            color = Color.Black
        )

        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = contentDescription,
            tint = Color.Black,
            modifier = Modifier
                .size(32.dp)
        )
    }
}
