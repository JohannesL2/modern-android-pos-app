package com.example.test_design.components.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun MenuShortcutButton(
    icon: Any,
    onClick: () -> Unit,
    containerColor: Color = Color.Black.copy(0.04f)
) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (icon) {
            is ImageVector -> Icon(icon, null, modifier = Modifier.size(24.dp), tint = Color.Black)
            is Int -> Icon(painterResource(icon), null, modifier = Modifier.size(24.dp), tint = Color.Black)
        }
    }
}